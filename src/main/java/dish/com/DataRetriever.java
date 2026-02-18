package dish.com;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

public class DataRetriever {

    // =========================================================================
    // MÉTHODES EXISTANTES (inchangées)
    // =========================================================================

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

            for (DishOrder d : order.getDishOrders()) {
                for (DishIngredient di : d.getDish().getDishIngredients()) {
                    double convertedQuantity = UnitConversion.convert(
                            di.getIngredient().getName(),
                            di.getQuantityRequired(),
                            di.getUnitType(),
                            UnitTypeEnum.KG
                    );

                    if (convertedQuantity == -1) {
                        throw new RuntimeException(
                                "Conversion impossible pour l'ingrédient : " + di.getIngredient().getName()
                        );
                    }

                    di.setQuantityRequired(convertedQuantity);
                    di.setUnitType(UnitTypeEnum.KG);
                }
            }

            checkStock(order.getDishOrders());

            Integer orderId;
            String reference;

            try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO "order"(id, reference, total_amount_ht, total_amount_ttc, creation_datetime)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, reference
            """)) {
                ps.setInt(1, order.getId() > 0 ? order.getId() : getNextSerialValue(conn, "order", "id"));

                if (order.getReference() != null)
                    ps.setString(2, order.getReference());
                else
                    ps.setNull(2, Types.VARCHAR);

                ps.setDouble(3, order.getTotalAmountWithoutVAT());
                ps.setDouble(4, order.getTotalAmountWithVAT());
                ps.setTimestamp(5, Timestamp.from(
                        order.getCreationDatetime() != null ? order.getCreationDatetime() : Instant.now()
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
                SELECT id, reference, total_amount_ht, total_amount_ttc, creation_datetime
                FROM "order"
                WHERE reference = ?
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
            order.setDishOrders(findDishOrdersByOrderId(order.getId()));
            return order;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================================
    // TD5 - QUESTION 1 : Push-down processing pour l'état de stock
    // =========================================================================

    /**
     * Calcule l'état de stock d'un ingrédient à un instant donné
     * directement au niveau de la base de données (push-down processing).
     *
     * La requête SQL :
     *  - filtre les mouvements <= t (clause WHERE)
     *  - applique CASE WHEN type='OUT' THEN -quantity ELSE quantity END
     *  - fait la SUM() par ingrédient (GROUP BY id_ingredient)
     *  - retourne l'unité et la quantité nette calculée
     */
    public StockValue getStockValueAt(Instant t, Integer ingredientId) {
        String sql = """
            SELECT
                sm.unit,
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
            GROUP BY sm.id_ingredient, sm.unit
        """;

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ingredientId);
            ps.setTimestamp(2, Timestamp.from(t));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double quantity = rs.getDouble("actual_quantity");
                UnitTypeEnum unit = UnitTypeEnum.valueOf(rs.getString("unit"));
                return new StockValue(quantity, unit);
            }

            // Aucun mouvement avant cet instant → stock = 0
            return new StockValue(0.0, null);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getStockValueAt (push-down)", e);
        }
    }

    // =========================================================================
    // TD5 - QUESTION 2a : Coût d'un plat (push-down)
    // =========================================================================

    /**
     * Calcule le coût total des ingrédients d'un plat directement en SQL :
     *   SUM(ingredient.price * dish_ingredient.quantity_required)
     */
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

    // =========================================================================
    // TD5 - QUESTION 2b : Marge brute d'un plat (push-down)
    // =========================================================================

    /**
     * Calcule la marge brute = selling_price - SUM(ingredient.price * quantity_required)
     * directement en SQL.
     */
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

    // =========================================================================
    // TD5 - QUESTION 3 : Statistiques de stock par période (push-down)
    // =========================================================================

    /**
     * Enum pour la périodicité supportée.
     */
    public enum Periodicity {
        DAY, WEEK, MONTH
    }

    /**
     * Retourne l'évolution de l'état de stock pour TOUS les ingrédients,
     * par période (DAY / WEEK / MONTH), dans un intervalle de dates donné.
     *
     * Structure du résultat :
     *   Map<String ingredientName, Map<String periode, Double stockCumulé>>
     *
     * La clé de la période est formatée selon la périodicité :
     *   DAY   → "2026-01-01"
     *   WEEK  → "2026-W01"
     *   MONTH → "2026-01"
     *
     * L'état de stock retourné est CUMULATIF : c'est la somme de TOUS les
     * mouvements depuis le début jusqu'à la fin de chaque période.
     *
     * @param periodicity  DAY, WEEK ou MONTH
     * @param intervalMin  début de l'intervalle (inclus)
     * @param intervalMax  fin de l'intervalle (inclus)
     */
    public Map<String, Map<String, Double>> getStockStatsByPeriod(
            Periodicity periodicity,
            LocalDate intervalMin,
            LocalDate intervalMax
    ) {
        // Choisit le format de troncature PostgreSQL selon la périodicité
        String truncUnit = switch (periodicity) {
            case DAY   -> "day";
            case WEEK  -> "week";
            case MONTH -> "month";
        };

        /*
         * On construit la requête dynamiquement car DATE_TRUNC n'accepte pas
         * de paramètre PreparedStatement pour l'unité de troncature.
         *
         * La sous-requête "grouped" calcule le stock net PAR période (delta).
         * La fenêtre SUM(...) OVER (...) calcule ensuite le cumul.
         */
        String sql = String.format(
                "SELECT" +
                        "    ingredient_id," +
                        "    ingredient_name," +
                        "    period_start," +
                        "    SUM(period_delta) OVER (" +
                        "        PARTITION BY ingredient_id" +
                        "        ORDER BY period_start" +
                        "    ) AS cumulative_stock" +
                        " FROM (" +
                        "    SELECT" +
                        "        i.id                                         AS ingredient_id," +
                        "        i.name                                       AS ingredient_name," +
                        "        DATE_TRUNC('%s', sm.creation_datetime)::date AS period_start," +
                        "        SUM(" +
                        "            CASE" +
                        "                WHEN sm.type = 'OUT' THEN -sm.quantity" +
                        "                ELSE sm.quantity" +
                        "            END" +
                        "        ) AS period_delta" +
                        "    FROM stock_movement sm" +
                        "    JOIN ingredient i ON sm.id_ingredient = i.id" +
                        "    WHERE sm.creation_datetime >= ?" +
                        "      AND sm.creation_datetime < ?" +
                        "    GROUP BY i.id, i.name, DATE_TRUNC('%s', sm.creation_datetime)" +
                        " ) grouped" +
                        " ORDER BY ingredient_id, period_start",
                truncUnit, truncUnit
        );

        // intervalMax inclus → on avance d'un jour pour le filtre strict <
        Instant from = intervalMin.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to   = intervalMax.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<String, Map<String, Double>> result = new LinkedHashMap<>();

        try (Connection conn = new DBConnection().getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.from(from));
            ps.setTimestamp(2, Timestamp.from(to));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String ingredientName = rs.getString("ingredient_name");
                String periodKey      = rs.getDate("period_start").toString();
                double stock          = rs.getDouble("cumulative_stock");

                result
                        .computeIfAbsent(ingredientName, k -> new LinkedHashMap<>())
                        .put(periodKey, stock);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getStockStatsByPeriod", e);
        }

        return result;
    }

    /**
     * Affiche les statistiques de stock sous forme de tableau dans la console.
     * Colonnes : Ingrédient | période1 | période2 | ...
     */
    public void printStockStatsByPeriod(
            Periodicity periodicity,
            LocalDate intervalMin,
            LocalDate intervalMax
    ) {
        Map<String, Map<String, Double>> stats =
                getStockStatsByPeriod(periodicity, intervalMin, intervalMax);

        if (stats.isEmpty()) {
            System.out.println("Aucune donnée pour la période sélectionnée.");
            return;
        }

        // Collecte toutes les périodes (colonnes) dans l'ordre
        Set<String> allPeriods = new LinkedHashSet<>();
        for (Map<String, Double> periodMap : stats.values()) {
            allPeriods.addAll(periodMap.keySet());
        }

        // En-tête
        StringBuilder header = new StringBuilder(String.format("%-20s", "Ingrédient"));
        for (String period : allPeriods) {
            header.append(String.format(" | %12s", period));
        }
        System.out.println(header);
        System.out.println("-".repeat(header.length()));

        // Lignes
        for (Map.Entry<String, Map<String, Double>> entry : stats.entrySet()) {
            StringBuilder row = new StringBuilder(String.format("%-20s", entry.getKey()));
            for (String period : allPeriods) {
                Double val = entry.getValue().get(period);
                if (val != null) {
                    row.append(String.format(" | %12.2f", val));
                } else {
                    row.append(String.format(" | %12s", "N/A"));
                }
            }
            System.out.println(row);
        }
    }

    // =========================================================================
    // MÉTHODES PRIVÉES (inchangées)
    // =========================================================================

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
}