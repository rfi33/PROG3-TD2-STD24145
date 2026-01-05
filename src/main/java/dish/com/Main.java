package dish.com;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();
        testFindId(dataRetriever);
        testFindIngredient(dataRetriever);
        testCreate(dataRetriever);
    }

    public static void testFindId(DataRetriever dataRetriever){
        Dish dish = dataRetriever.findDishById(1);
        System.out.println("Voici l'id : " + dish.getId() + " " + dish.getName() + " " + dish.getDishType());

        System.out.println("Ingrédients :");
        for (Ingredient ingredient : dish.getIngredients()) {
            System.out.println("- " + ingredient.getName());
        }
    }
    public static  void testFindIngredient(DataRetriever dataRetriever){
        List<Ingredient> ingredient= dataRetriever.findIngredient(2,2);
        System.out.println("Voici le resultat");

        for (Ingredient ingredient1: ingredient){
            System.out.println(ingredient1.getName());
        }
    }
    public static void testCreate(DataRetriever dataRetriever) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Sucre");

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);

        List<Ingredient> result =
                dataRetriever.createIngredients(ingredients);

        System.out.println("Ingrédients créés :");
        for (Ingredient i : result) {
            System.out.println(i.getName());
        }
    }
}
