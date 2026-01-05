package dish.com;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private DBConnection dbConnection;
    private Dish dish;
    private Ingredient ingredient;
    public DataRetriever() {
        this.dbConnection = new DBConnection();
    }


    public Dish findDishById(Integer id) {

        String sql = "SELECT dish.id,dish.name,dish.dish_type,ingredient.name as igName FROM ingredient " +
                "INNER JOIN dish ON ingredient.id_dish = dish.id " +
                "WHERE dish.id = ?";



        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                List<Ingredient> ingredients = new ArrayList<>();

                while (rs.next()) {

                    if (dish == null) {
                        dish = new Dish();
                        dish.setId(rs.getInt("id"));
                        dish.setName(rs.getString("name"));
                        dish.setDishType(Dish.DishType.valueOf(rs.getString("dish_type")));
                    }

                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("id"));
                    ingredient.setName(rs.getString("igName"));
                    ingredients.add(ingredient);
                }

                if (dish != null) {
                    dish.setIngredients(ingredients);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dish;
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

        String insertDishSql =
                "INSERT INTO dish(name, price) VALUES (?, ?)";

        String updateDishSql =
                "UPDATE dish SET name = ?, price = ? WHERE id = ?";

        String deleteIngredientsSql =
                "DELETE FROM dish_ingredient WHERE dish_id = ?";

        String insertIngredientSql =
                "INSERT INTO dish_ingredient(dish_id, ingredient_id) VALUES (?, ?)";

        try (Connection connection = dbConnection.getDBConnection()) {

            connection.setAutoCommit(false);
            if (dishToSave == null) {
                try (PreparedStatement ps = connection.prepareStatement(
                        insertDishSql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, dishToSave.getName());
                    ps.executeUpdate();

                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        dishToSave.setId(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(updateDishSql)) {
                    ps.setString(1, dishToSave.getName());
                    ps.setInt(3, dishToSave.getId());
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(deleteIngredientsSql)) {
                ps.setInt(1, dishToSave.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(insertIngredientSql)) {
                for (Ingredient ingredient : dishToSave.getIngredients()) {
                    ps.setInt(1, dishToSave.getId());
                    ps.setInt(2, ingredient.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
            return dishToSave;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde du plat");
        }
    }
    public List<Dish> findDishsByIngredientName(String ingredientName) {

        List<Dish> dishes = new ArrayList<>();

        String sql =
                "SELECT DISTINCT d.id AS dish_id, d.name AS dish_name, d.price " +
                        "FROM dish d " +
                        "JOIN dish_ingredient di ON d.id = di.dish_id " +
                        "JOIN ingredient i ON i.id = di.ingredient_id " +
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

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche des plats par ingrédient");
        }

        return dishes;
    }
    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            Ingredient.CategoryEnum category,
            String dishName,
            int page,
            int size) {

        List<Ingredient> ingredients = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT i.id, i.name, i.category " +
                        "FROM ingredient i " +
                        "LEFT JOIN dish_ingredient di ON i.id = di.ingredient_id " +
                        "LEFT JOIN dish d ON d.id = di.dish_id " +
                        "WHERE 1=1 "
        );

        if (ingredientName != null && !ingredientName.isBlank()) {
            sql.append("AND LOWER(i.name) LIKE LOWER(?) ");
            parameters.add("%" + ingredientName + "%");
        }
        if (category != null) {
            sql.append("AND i.category = ? ");
            parameters.add(category.name());
        }

        if (dishName != null && !dishName.isBlank()) {
            sql.append("AND LOWER(d.name) LIKE LOWER(?) ");
            parameters.add("%" + dishName + "%");
        }
        sql.append("LIMIT ? OFFSET ? ");
        parameters.add(size);
        parameters.add(page * size);

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
                    ingredient.setCategory(
                            Ingredient.CategoryEnum.valueOf(rs.getString("category"))
                    );

                    ingredients.add(ingredient);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche des ingrédients");
        }

        return ingredients;
    }


}
