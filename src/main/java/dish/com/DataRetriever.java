package dish.com;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class DataRetriever {

    public List<StockMovement> findStockMovementsByIngredientId(Integer ingredientId) {
        List<StockMovement> movements = new ArrayList<>();

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT id, quantity, type, creation_datetime
                FROM stock_movement
                WHERE id_ingredient = ?
                ORDER BY creation_datetime
             """)) {

            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                movements.add(new StockMovement(
                        rs.getInt("id"),
                        MovementTypeEnum.valueOf(rs.getString("type")),
                        rs.getTimestamp("creation_datetime").toInstant(),
                        rs.getDouble("quantity")
                ));
            }
            return movements;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish findDishById(Integer id) {
        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT id, name, dish_type, selling_price
                FROM dish
                WHERE id = ?
             """)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("Dish not found: " + id);
            }

            Dish dish = new Dish();
            dish.setId(rs.getInt("id"));
            dish.setName(rs.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
            dish.setPrice(rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"));
            dish.setDishIngredients(findDishIngredientsByDishId(id));
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dish> findAllDishes() {
        List<Dish> dishes = new ArrayList<>();

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT id, name, dish_type, selling_price
                FROM dish
             """)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                dish.setPrice(rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"));
                dish.setDishIngredients(findDishIngredientsByDishId(dish.getId()));
                dishes.add(dish);
            }
            return dishes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient findIngredientById(Integer id) {
        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT id, name, price, category
                FROM ingredient
                WHERE id = ?
             """)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("Ingredient not found: " + id);
            }

            Ingredient ingredient = new Ingredient();
            ingredient.setId(rs.getInt("id"));
            ingredient.setName(rs.getString("name"));
            ingredient.setPrice(rs.getDouble("price"));
            ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
            ingredient.setStockMovementList(findStockMovementsByIngredientId(id));
            return ingredient;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT id, name, price, category
                FROM ingredient
             """)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredients.add(ingredient);
            }
            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order saveOrder(Order order) {
        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            if (order.getRestaurantTable() == null) {
                throw new RuntimeException("La table doit être spécifiée pour créer une commande.");
            }

            checkTableAvailability(conn, order.getRestaurantTable().getId());

            checkStock(order.getDishOrders());

            Integer orderId;
            String reference;

            try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO "order"(id, reference, total_amount_ht, total_amount_ttc,
                                   creation_datetime, id_table, arrival_datetime)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id, reference
            """)) {

                ps.setInt(1,
                        order.getId() > 0
                                ? order.getId()
                                : getNextSerialValue(conn, "order", "id")
                );

                if (order.getReference() != null)
                    ps.setString(2, order.getReference());
                else
                    ps.setNull(2, Types.VARCHAR);

                ps.setDouble(3, order.getTotalAmountWithoutVAT());
                ps.setDouble(4, order.getTotalAmountWithVAT());

                ps.setTimestamp(5, Timestamp.from(
                        order.getCreationDatetime() != null
                                ? order.getCreationDatetime()
                                : Instant.now()
                ));

                ps.setInt(6, order.getRestaurantTable().getId());

                ps.setTimestamp(7, Timestamp.from(
                        order.getArrivalDatetime() != null
                                ? order.getArrivalDatetime()
                                : Instant.now()
                ));

                ResultSet rs = ps.executeQuery();
                rs.next();
                orderId = rs.getInt("id");
                reference = rs.getString("reference");
            }

            saveDishOrders(conn, orderId, order.getDishOrders());
            createStockMovementsForOrder(conn, order.getDishOrders());

            conn.commit();
            return findOrderByReference(reference);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order findOrderByReference(String reference) {
        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT o.id, o.reference, o.total_amount_ht, o.total_amount_ttc,
                       o.creation_datetime, o.arrival_datetime, o.departure_datetime,
                       rt.id AS table_id, rt.table_number
                FROM "order" o
                LEFT JOIN restaurant_table rt ON o.id_table = rt.id
                WHERE o.reference = ?
             """)) {

            ps.setString(1, reference);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("Order not found: " + reference);
            }

            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setReference(rs.getString("reference"));
            order.setTotalAmountHT(rs.getDouble("total_amount_ht"));
            order.setTotalAmountTTC(rs.getDouble("total_amount_ttc"));
            order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

            if (rs.getTimestamp("arrival_datetime") != null)
                order.setArrivalDatetime(rs.getTimestamp("arrival_datetime").toInstant());

            if (rs.getTimestamp("departure_datetime") != null)
                order.setDepartureDatetime(rs.getTimestamp("departure_datetime").toInstant());

            // Table
            if (rs.getObject("table_id") != null) {
                RestaurantTable table = new RestaurantTable();
                table.setId(rs.getInt("table_id"));
                table.setTableNumber(rs.getInt("table_number"));
                order.setRestaurantTable(table);
            }

            order.setDishOrders(findDishOrdersByOrderId(order.getId()));
            return order;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Vérifie qu'une table est libre (aucune commande ouverte = sans departure_datetime).
     * Si occupée, indique les tables disponibles dans le message d'erreur.
     */
    private void checkTableAvailability(Connection conn, Integer tableId) throws SQLException {
        // Cherche si la table a une commande en cours (sans departure_datetime)
        try (PreparedStatement ps = conn.prepareStatement("""
            SELECT COUNT(*) FROM "order"
            WHERE id_table = ?
              AND departure_datetime IS NULL
        """)) {
            ps.setInt(1, tableId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                // Récupère les tables disponibles pour enrichir le message
                List<Integer> available = findAvailableTableNumbers(conn);
                String availableStr = available.isEmpty()
                        ? "aucune"
                        : available.toString();
                throw new RuntimeException(
                        "La table " + tableId + " n'est pas disponible. " +
                                "Tables disponibles : " + availableStr
                );
            }
        }
    }

    private List<Integer> findAvailableTableNumbers(Connection conn) throws SQLException {
        List<Integer> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("""
            SELECT rt.table_number
            FROM restaurant_table rt
            WHERE rt.id NOT IN (
                SELECT id_table FROM "order"
                WHERE departure_datetime IS NULL
            )
            ORDER BY rt.table_number
        """)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("table_number"));
            }
        }
        return list;
    }

    private void checkStock(List<DishOrder> dishOrders) {
        Map<Integer, Double> required = new HashMap<>();

        for (DishOrder d : dishOrders) {
            for (DishIngredient di : d.getDish().getDishIngredients()) {
                required.merge(
                        di.getIngredient().getId(),
                        di.getQuantityRequired() * d.getQuantity(),
                        Double::sum
                );
            }
        }

        for (Map.Entry<Integer, Double> entry : required.entrySet()) {
            Ingredient ing = findIngredientById(entry.getKey());
            StockValue sv = ing.getStockValueAt(Instant.now());
            if (sv == null || sv.getQuantity() < entry.getValue()) {
                throw new RuntimeException("Insufficient stock for " + ing.getName());
            }
        }
    }

    private List<DishOrder> findDishOrdersByOrderId(Integer orderId) {
        List<DishOrder> list = new ArrayList<>();

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT id, id_dish, quantity
                FROM dish_order
                WHERE id_order = ?
             """)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DishOrder d = new DishOrder();
                d.setId(rs.getInt("id"));
                d.setDish(findDishById(rs.getInt("id_dish")));
                d.setQuantity(rs.getInt("quantity"));
                list.add(d);
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishIngredient> findDishIngredientsByDishId(Integer dishId) {
        List<DishIngredient> list = new ArrayList<>();

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT di.id, di.quantity_required, di.unit,
                       i.id AS ing_id, i.name, i.price, i.category
                FROM dish_ingredient di
                JOIN ingredient i ON di.id_ingredient = i.id
                WHERE di.id_dish = ?
             """)) {

            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DishIngredient di = new DishIngredient();
                di.setId(rs.getInt("id"));
                di.setQuantityRequired(rs.getDouble("quantity_required"));
                di.setUnitType(UnitTypeEnum.valueOf(rs.getString("unit")));

                Ingredient i = new Ingredient();
                i.setId(rs.getInt("ing_id"));
                i.setName(rs.getString("name"));
                i.setPrice(rs.getDouble("price"));
                i.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                i.setStockMovementList(findStockMovementsByIngredientId(i.getId()));

                di.setIngredient(i);
                list.add(di);
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveDishOrders(Connection conn, Integer orderId, List<DishOrder> dishOrders) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO dish_order(id, id_order, id_dish, quantity)
            VALUES (?, ?, ?, ?)
        """)) {

            for (DishOrder d : dishOrders) {
                ps.setInt(1,
                        d.getId() > 0
                                ? d.getId()
                                : getNextSerialValue(conn, "dish_order", "id")
                );
                ps.setInt(2, orderId);
                ps.setInt(3, d.getDish().getId());
                ps.setInt(4, d.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void createStockMovementsForOrder(Connection conn, List<DishOrder> dishOrders) throws SQLException {
        Map<Integer, Double> used = new HashMap<>();

        for (DishOrder d : dishOrders) {
            for (DishIngredient di : d.getDish().getDishIngredients()) {
                used.merge(
                        di.getIngredient().getId(),
                        di.getQuantityRequired() * d.getQuantity(),
                        Double::sum
                );
            }
        }

        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO stock_movement(id, id_ingredient, quantity, type, unit, creation_datetime)
            VALUES (?, ?, ?, 'OUT'::movement_type, 'KG'::unit_type, ?)
        """)) {

            Instant now = Instant.now();
            for (Map.Entry<Integer, Double> e : used.entrySet()) {
                ps.setInt(1, getNextSerialValue(conn, "stock_movement", "id"));
                ps.setInt(2, e.getKey());
                ps.setDouble(3, e.getValue());
                ps.setTimestamp(4, Timestamp.from(now));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private int getNextSerialValue(Connection conn, String table, String column) throws SQLException {
        String seq;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT pg_get_serial_sequence(?, ?)")) {
            ps.setString(1, table);
            ps.setString(2, column);
            ResultSet rs = ps.executeQuery();
            rs.next();
            seq = rs.getString(1);
        }

        try (PreparedStatement ps = conn.prepareStatement("SELECT nextval(?)")) {
            ps.setString(1, seq);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public StockValue getStockValueAt(Instant t, Integer ingredientIdentifier) {

        String sql = """
        SELECT
            UPPER(sm.unit::text) AS unit,
            COALESCE(
                SUM(
                    CASE
                        WHEN sm.type = 'OUT' THEN -sm.quantity
                        ELSE sm.quantity
                    END
                ),
                0
            ) AS actual_quantity
        FROM stock_movement sm
        WHERE sm.id_ingredient = ?
          AND sm.creation_datetime <= ?
        GROUP BY sm.unit
        """;

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ingredientIdentifier);
            ps.setTimestamp(2, Timestamp.from(t));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double quantity = rs.getDouble("actual_quantity");
                    UnitTypeEnum unit = UnitTypeEnum.valueOf(rs.getString("unit"));
                    return new StockValue(quantity, unit);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getStockValueAt", e);
        }

        return new StockValue(0, null);
    }


    public Double getDishCost(Integer dishId) {

        String sql = """
        SELECT
            COALESCE(
                SUM(i.price * di.quantity_required),
                0.0
            ) AS dish_cost
        FROM dish_ingredient di
        JOIN ingredient i ON di.id_ingredient = i.id
        WHERE di.id_dish = ?
        """;

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dishId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("dish_cost");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getDishCost", e);
        }

        return 0.0;
    }

    public Double getGrossMargin(Integer dishId) {

        String sql = """
        SELECT
            d.selling_price,
            COALESCE(
                SUM(i.price * di.quantity_required),
                0.0
            ) AS dish_cost
        FROM dish d
        LEFT JOIN dish_ingredient di ON di.id_dish = d.id
        LEFT JOIN ingredient i       ON di.id_ingredient = i.id
        WHERE d.id = ?
        GROUP BY d.id, d.selling_price
        """;

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dishId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Dish not found: " + dishId);
                }

                Object sellingPrice = rs.getObject("selling_price");
                if (sellingPrice == null) {
                    throw new RuntimeException(
                            "Le prix de vente du plat " + dishId + " est NULL – marge impossible.");
                }

                double price = rs.getDouble("selling_price");
                double cost  = rs.getDouble("dish_cost");
                return price - cost;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getGrossMargin", e);
        }
    }
}