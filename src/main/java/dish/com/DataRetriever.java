package dish.com;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever {

    public List<StockMovement> findStockMovementsByIngredientId(Integer ingredientId) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<StockMovement> movements = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT id, id_ingredient, quantity, type, creation_datetime
                    FROM stock_movement
                    WHERE id_ingredient = ?
                    ORDER BY creation_datetime ASC
                    """
            );
            preparedStatement.setInt(1, ingredientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                double quantity = resultSet.getDouble("quantity");
                MovementTypeEnum type = MovementTypeEnum.valueOf(resultSet.getString("type"));
                Timestamp timestamp = resultSet.getTimestamp("creation_datetime");
                Instant creationDatetime = timestamp.toInstant();

                StockMovement movement = new StockMovement(id, type, creationDatetime, quantity);
                movements.add(movement);
            }

            dbConnection.closeConnection(connection);
            return movements;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des mouvements de stock", e);
        }
    }

    Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select dish.id as dish_id, dish.name as dish_name, dish_type, dish.selling_price as dish_price
                            from dish
                            where dish.id = ?;
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getObject("dish_price") == null
                        ? null : resultSet.getDouble("dish_price"));
                List<Ingredient> ingredients = findIngredientByDishId(id);
                for (Ingredient ingredient : ingredients) {
                    ingredient.setStockMovementList(findStockMovementsByIngredientId(ingredient.getId()));
                }
                dish.setIngredients(ingredients);
                return dish;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<Dish> findAllDishes() {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Dish> dishes = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select dish.id as dish_id, dish.name as dish_name, dish_type, dish.selling_price as dish_price
                            from dish;
                            """);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getObject("dish_price") == null
                        ? null : resultSet.getDouble("dish_price"));
                List<Ingredient> ingredients = findIngredientByDishId(resultSet.getInt("dish_id"));
                for (Ingredient ingredient : ingredients) {
                    ingredient.setStockMovementList(findStockMovementsByIngredientId(ingredient.getId()));
                }
                dish.setIngredients(ingredients);
                dishes.add(dish);
            }
            dbConnection.closeConnection(connection);
            return dishes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Dish saveDish(Dish toSave) {
        String upsertDishSql = """
                    INSERT INTO dish (id, selling_price, name, dish_type)
                    VALUES (?, ?, ?, ?::dish_type)
                    ON CONFLICT (id) DO UPDATE
                    SET name = EXCLUDED.name,
                        dish_type = EXCLUDED.dish_type,
                        selling_price = EXCLUDED.selling_price
                    RETURNING id
                """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;
            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }
                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            List<Ingredient> newIngredients = toSave.getIngredients();
            detachIngredients(conn, dishId, newIngredients);
            attachIngredients(conn, dishId, newIngredients);

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient findIngredientById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT id, name, price, category
                    FROM ingredient
                    WHERE id = ?;
                    """
            );
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredient.setStockMovementList(findStockMovementsByIngredientId(id));
                return ingredient;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Ingredient not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findAllIngredients() {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT id, name, price, category
                    FROM ingredient;
                    """
            );
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<Ingredient> findIngredientsByCategory(CategoryEnum category) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT id, name, price, category
                    FROM ingredient
                    WHERE category = ?::ingredient_category;
                    """
            );
            preparedStatement.setString(1, category.name());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Ingredient saveIngredient(Ingredient toSave) {
        String upsertIngredientSql = """
        INSERT INTO ingredient (id, name, category, price)
        VALUES (?, ?, ?::ingredient_category, ?)
        ON CONFLICT (id) DO UPDATE
        SET name = EXCLUDED.name,
            category = EXCLUDED.category,
            price = EXCLUDED.price
        RETURNING id
    """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);
            Integer ingredientId;
            try (PreparedStatement ps = conn.prepareStatement(upsertIngredientSql)) {
                int ingredientIdToUse = toSave.getId() != null
                        ? toSave.getId()
                        : getNextSerialValue(conn, "ingredient", "id");
                ps.setInt(1, ingredientIdToUse);
                ps.setString(2, toSave.getName());
                ps.setString(3, toSave.getCategory().name());
                ps.setDouble(4, toSave.getPrice());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    ingredientId = rs.getInt(1);
                }
            }

            if (toSave.getStockMovementList() != null && !toSave.getStockMovementList().isEmpty()) {
                String insertStockMovementSql = """
                INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
                VALUES (?, ?, ?, ?::movement_type, 'KG'::unit_type, ?)
                ON CONFLICT (id) DO NOTHING
            """;

                try (PreparedStatement ps = conn.prepareStatement(insertStockMovementSql)) {
                    for (StockMovement movement : toSave.getStockMovementList()) {
                        ps.setInt(1, movement.getId());
                        ps.setInt(2, ingredientId);
                        ps.setDouble(3, movement.getQuantity());
                        ps.setString(4, movement.getType().name());
                        ps.setTimestamp(5, Timestamp.from(movement.getCreationDatetime()));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            conn.commit();
            return findIngredientById(ingredientId);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de l'ingrédient", e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }

        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getDBConnection();

        try {
            conn.setAutoCommit(false);

            String insertSql = """
                INSERT INTO ingredient (id, name, category, price)
                VALUES (?, ?, ?::ingredient_category, ?)
                RETURNING id
            """;

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {

                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }

                conn.commit();
                return savedIngredients;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    private void detachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }
            return;
        }

        String baseSql = """
                    UPDATE ingredient
                    SET id_dish = NULL
                    WHERE id_dish = ? AND id NOT IN (%s)
                """;

        String inClause = ingredients.stream()
                .map(i -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(baseSql, inClause);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            int index = 2;
            for (Ingredient ingredient : ingredients) {
                ps.setInt(index++, ingredient.getId());
            }
            ps.executeUpdate();
        }
    }

    private void attachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        String attachSql = """
                    UPDATE ingredient
                    SET id_dish = ?
                    WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (Ingredient ingredient : ingredients) {
                ps.setInt(1, dishId);
                ps.setInt(2, ingredient.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Ingredient> findIngredientByDishId(Integer dishId) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT i.id,
                           i.name,
                           i.price,
                           i.category
                    FROM dish_ingredient di
                    JOIN ingredient i ON di.id_ingredient = i.id
                    WHERE di.id_dish = ?
                    """
            );

            preparedStatement.setInt(1, dishId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(
                        CategoryEnum.valueOf(resultSet.getString("category"))
                );
                ingredients.add(ingredient);
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sql = "SELECT pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnName
            );
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";

        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}