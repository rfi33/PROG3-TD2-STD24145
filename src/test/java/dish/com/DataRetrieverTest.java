package dish.com;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/*
class DataRetrieverTest {

    private DataRetriever dataRetriever = new DataRetriever();

    @Test
    void findDishById() {
        Dish dish = dataRetriever.findDishById(1);

        assertNotNull(dish);
        assertEquals("Salade exotique",dish.getName());
        assertEquals(2,dish.getIngredients().size());

        assertTrue(
                dish.getIngredients().stream()
                        .anyMatch(ingredient -> ingredient.getName().equals("Laitue"))
        );
        assertTrue(
                dish.getIngredients().stream()
                        .anyMatch(ingredient -> ingredient.getName().equals("Tomate"))
        );

        assertThrows(RuntimeException.class,()->{
            dataRetriever.findDishById(999);
        });
    }

    @Test
    void findIngredient() {
        List<Ingredient> ingredients = dataRetriever.findIngredient(2,2);

        assertEquals(2,ingredients.size());
        assertEquals("Poulet",ingredients.get(0).getName());
        assertEquals("Chocolat",ingredients.get(1).getName());

        List<Ingredient> ingredients1 = dataRetriever.findIngredient(3,5);

        assertNotNull(ingredients1);
        assertTrue(ingredients1.isEmpty());

    }

    @Test
    void findDishByIngredientName() {
        List<Dish> dishes = dataRetriever.findDishByIngredientName("eur");

        assertNotNull(dishes);
        assertEquals(1,dishes.size());
        assertEquals("Gateau au chocolat",dishes.get(0).getName());
    }

    @Test
    void findIngredientsByCriteria() throws SQLException {
        List<Ingredient> ingredients = dataRetriever.findIngredientsByCriteria(
                null,
                Ingredient.CategoryEnum.VEGETABLE,
                null,
                1,
                10
        );

        assertNotNull(ingredients);
        assertEquals(2, ingredients.size());
        assertEquals("Laitue", ingredients.get(0).getName());
        assertEquals("Tomate", ingredients.get(1).getName());

        List<Ingredient> ingredients1 = dataRetriever.findIngredientsByCriteria(
                "cho",
                null,
                "Sal",
                1,
                10
        );

        assertNotNull(ingredients1);
        assertTrue(ingredients1.isEmpty());
    }
}
*/
