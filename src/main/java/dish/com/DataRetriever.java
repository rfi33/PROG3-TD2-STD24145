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

    public RestaurantTable findRestaurantTableById(Integer id) {
        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT id, table_number
                FROM restaurant_table
                WHERE id = ?
             """)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("Restaurant table not found: " + id);
            }

            RestaurantTable table = new RestaurantTable();
            table.setId(rs.getInt("id"));
            table.setTableNumber(rs.getInt("table_number"));
            return table;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isTableAvailable(Integer tableId, Instant checkTime) {
        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT COUNT(*)
                FROM "order"
                WHERE id_table = ?
                  AND arrival_datetime <= ?
                  AND (departure_datetime IS NULL OR departure_datetime > ?)
             """)) {

            ps.setInt(1, tableId);
            ps.setTimestamp(2, Timestamp.from(checkTime));
            ps.setTimestamp(3, Timestamp.from(checkTime));

            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) == 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<RestaurantTable> findAvailableTables(Instant checkTime) {
        List<RestaurantTable> tables = new ArrayList<>();

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT t.id, t.table_number
                FROM restaurant_table t
                WHERE t.id NOT IN (
                    SELECT o.id_table
                    FROM "order" o
                    WHERE o.arrival_datetime <= ?
                      AND (o.departure_datetime IS NULL OR o.departure_datetime > ?)
                )
             """)) {

            ps.setTimestamp(1, Timestamp.from(checkTime));
            ps.setTimestamp(2, Timestamp.from(checkTime));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RestaurantTable t = new RestaurantTable();
                t.setId(rs.getInt("id"));
                t.setTableNumber(rs.getInt("table_number"));
                tables.add(t);
            }
            return tables;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order saveOrder(Order order) {
        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            if (order.getRestaurantTable() == null || order.getRestaurantTable().getId() == null) {
                throw new RuntimeException("Une table doit être spécifiée pour la commande");
            }

            RestaurantTable table = findRestaurantTableById(order.getRestaurantTable().getId());

            Instant arrivalTime = order.getArrivalDatetime() != null
                    ? order.getArrivalDatetime()
                    : Instant.now();

            order.setArrivalDatetime(arrivalTime);

            if (!isTableAvailable(table.getId(), arrivalTime)) {

                List<RestaurantTable> availableTables = findAvailableTables(arrivalTime);

                if (availableTables.isEmpty()) {
                    throw new RuntimeException(
                            "La table " + table.getTableNumber()
                                    + " n'est pas disponible. Aucune autre table n'est actuellement disponible"
                    );
                }

                String available = availableTables.stream()
                        .map(t -> String.valueOf(t.getTableNumber()))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");

                throw new RuntimeException(
                        "La table " + table.getTableNumber()
                                + " n'est pas disponible. Tables disponibles : " + available
                );
            }

            for (DishOrder d : order.getDishOrders()) {
                for (DishIngredient di : d.getDish().getDishIngredients()) {
                    double converted = UnitConversion.convert(
                            di.getIngredient().getName(),
                            di.getQuantityRequired(),
                            di.getUnitType(),
                            UnitTypeEnum.KG
                    );

                    if (converted == -1) {
                        throw new RuntimeException(
                                "Conversion impossible pour l'ingrédient : "
                                        + di.getIngredient().getName()
                        );
                    }

                    di.setQuantityRequired(converted);
                    di.setUnitType(UnitTypeEnum.KG);
                }
            }

            checkStock(order.getDishOrders());

            Integer orderId;
            String reference;

            try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO "order" (
                id,
                reference,
                total_amount_ht,
                total_amount_ttc,
                creation_datetime,
                id_table,
                arrival_datetime,
                departure_datetime
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, reference
        """)) {

                ps.setInt(1,
                        order.getId() > 0
                                ? order.getId()
                                : getNextSerialValue(conn, "order", "id")
                );

                ps.setString(2, order.getReference());
                ps.setDouble(3, order.getTotalAmountWithoutVAT());
                ps.setDouble(4, order.getTotalAmountWithVAT());

                ps.setTimestamp(5, Timestamp.from(
                        order.getCreationDatetime() != null
                                ? order.getCreationDatetime()
                                : Instant.now()
                ));

                ps.setInt(6, table.getId());
                ps.setTimestamp(7, Timestamp.from(arrivalTime));
                ps.setTimestamp(8,
                        order.getDepartureDatetime() != null
                                ? Timestamp.from(order.getDepartureDatetime())
                                : null
                );

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
                       o.creation_datetime, o.id_table, o.arrival_datetime, o.departure_datetime,
                       t.table_number
                FROM "order" o
                JOIN restaurant_table t ON o.id_table = t.id
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

            RestaurantTable table = new RestaurantTable();
            table.setId(rs.getInt("id_table"));
            table.setTableNumber(rs.getInt("table_number"));
            order.setRestaurantTable(table);

            order.setArrivalDatetime(rs.getTimestamp("arrival_datetime").toInstant());

            Timestamp dep = rs.getTimestamp("departure_datetime");
            if (dep != null) order.setDepartureDatetime(dep.toInstant());

            order.setDishOrders(findDishOrdersByOrderId(order.getId()));
            return order;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        for (Map.Entry<Integer, Double> e : required.entrySet()) {
            Ingredient ing = findIngredientById(e.getKey());
            StockValue sv = ing.getStockValueAt(Instant.now());
            if (sv == null || sv.getQuantity() < e.getValue()) {
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
                ps.setInt(1, d.getId() > 0 ? d.getId() : getNextSerialValue(conn, "dish_order", "id"));
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

        try (PreparedStatement ps = conn.prepareStatement("SELECT pg_get_serial_sequence(?, ?)")) {
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
}