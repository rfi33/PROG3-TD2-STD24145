package dish.com;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private DBConnection dbConnection;
    private Dish dish;
    private Ingredient ingredient;
    public DataRetriever() {
        this.dbConnection = new DBConnection();
    }


    public Dish findDishById(int id) {
        String sql = "SELECT id, name, dish_type, price FROM dish WHERE id = ?";

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(Dish.DishType.valueOf(rs.getString("dish_type")));
                dish.setPrice(rs.getObject("price", Double.class));
                return dish;
            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du plat", e);
        }
    }


    public List<Ingredient> findIngredient(int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;
        String sql = "SELECT ingredient.id,ingredient.name FROM ingredient" +
                " ORDER BY ingredient.id" +
                " LIMIT ? OFFSET ?";

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, size);
            preparedStatement.setInt(2, offset);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredients.add(ingredient);
            }
            dbConnection.getDBConnection().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
return ingredients;
    }


    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {

        String checkSql = "SELECT COUNT(*) FROM ingredient WHERE name = ?";
        String insertSql = "INSERT INTO ingredient(name) VALUES (?)";

        try (Connection connection = dbConnection.getDBConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql);
                 PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

                for (Ingredient ingredient : newIngredients) {

                    checkStmt.setString(1, ingredient.getName());
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        throw new RuntimeException(
                                "L'ingrédient existe déjà : " + ingredient.getName()
                        );
                    }

                    insertStmt.setString(1, ingredient.getName());
                    insertStmt.executeUpdate();
                }
                dbConnection.getDBConnection().close();
                connection.commit();
                return newIngredients;

            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Transaction annulée", e);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Dish saveDish(Dish dishToSave) {
        if (dishToSave == null) {
            throw new IllegalArgumentException("Le plat ne peut pas être null");
        }

        if (dishToSave.getDishType() == null) {
            throw new IllegalArgumentException("Le type du plat doit être défini avant la sauvegarde");
        }

        String insertDishSql =
                "INSERT INTO dish (name, dish_type, price) VALUES (?, ?::types, ?)";
        String updateDishSql =
                "UPDATE dish SET name = ?, dish_type = ?::types, price = ? WHERE id = ?";
        String updateIngredientSql =
                "UPDATE ingredient SET id_dish = ? WHERE id = ?";

        try (Connection connection = dbConnection.getDBConnection()) {
            connection.setAutoCommit(false);

            try {
                if ((dishToSave.getId() <= 0)) {

                    try (PreparedStatement ps = connection.prepareStatement(
                            insertDishSql, Statement.RETURN_GENERATED_KEYS)) {

                        ps.setString(1, dishToSave.getName());
                        ps.setString(2, dishToSave.getDishType().name());

                        if (dishToSave.getPrice() != 0) {
                            ps.setDouble(3, dishToSave.getPrice());
                        } else {
                            ps.setNull(3, Types.DOUBLE);
                        }

                        ps.executeUpdate();

                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                dishToSave.setId(rs.getInt(1));
                            }
                        }
                    }

                } else {
                    try (PreparedStatement ps = connection.prepareStatement(updateDishSql)) {
                        ps.setString(1, dishToSave.getName());
                        ps.setString(2, dishToSave.getDishType().name());

                        if (dishToSave.getPrice() != 0) {
                            ps.setDouble(3, dishToSave.getPrice());
                        } else {
                            ps.setNull(3, Types.DOUBLE);
                        }

                        ps.setInt(4, dishToSave.getId());
                        ps.executeUpdate();
                    }
                }

                if (dishToSave.getIngredients() != null && !dishToSave.getIngredients().isEmpty()) {
                    try (PreparedStatement ps = connection.prepareStatement(updateIngredientSql)) {
                        for (Ingredient ing : dishToSave.getIngredients()) {
                            ps.setInt(1, dishToSave.getId());
                            ps.setInt(2, ing.getId());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                connection.commit();
                return dishToSave;

            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Transaction annulée lors de la sauvegarde du plat", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur SQL lors de la sauvegarde du plat", e);
        }
    }

    public List<Dish> findDishByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();

        String sql =
                "SELECT DISTINCT d.id AS dish_id, d.name AS dish_name " +
                        "FROM ingredient i " +
                        "JOIN dish d ON i.id_dish = d.id " +
                        "WHERE LOWER(i.name) LIKE LOWER(?)";

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, "%" + ingredientName + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Dish dish = new Dish();
                    dish.setId(rs.getInt("dish_id"));
                    dish.setName(rs.getString("dish_name"));
                    dishes.add(dish);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des plats par ingrédient", e);
        }

        return dishes;
    }


    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            Ingredient.CategoryEnum category,
            String dishName,
            int page,
            int size) throws SQLException {

        List<Ingredient> ingredients = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT ingredient.id, ingredient.name, ingredient.category, ingredient.price, ingredient.id_dish " +
                        "FROM ingredient " +
                        "INNER JOIN dish ON ingredient.id_dish = dish.id " +
                        "WHERE 1=1 "
        );

        if (ingredientName != null && !ingredientName.isBlank()) {
            sql.append("AND LOWER(ingredient.name) LIKE LOWER(?) ");
            parameters.add("%" + ingredientName + "%");
        }
        if (category != null) {
            sql.append("AND ingredient.category = CAST(? AS categories) ");
            parameters.add(category.name());
        }
        if (dishName != null && !dishName.isBlank()) {
            sql.append("AND LOWER(dish.name) LIKE LOWER(?) ");
            parameters.add("%" + dishName + "%");
        }

        sql.append("LIMIT ? OFFSET ? ");
        parameters.add(size);
        parameters.add((page - 1) * size);

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("id"));
                    ingredient.setName(rs.getString("name"));

                    Object priceObj = rs.getObject("price");
                    ingredient.setPrice(priceObj != null ? rs.getDouble("price") : 0.0);

                    String categoryFromDb = rs.getString("category");
                    if (categoryFromDb != null && !categoryFromDb.isEmpty()) {
                        try {
                            ingredient.setCategory(Ingredient.CategoryEnum.valueOf(categoryFromDb.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            ingredient.setCategory(Ingredient.CategoryEnum.OTHER);
                        }
                    } else {
                        ingredient.setCategory(Ingredient.CategoryEnum.OTHER);
                    }

                    ingredient.setDish(null);

                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des ingrédients", e);
        }

        return ingredients;
    }
}
