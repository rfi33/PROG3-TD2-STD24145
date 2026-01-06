package dish.com;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();
        testFindId(dataRetriever);
        testFindIngredient(dataRetriever);
        test(dataRetriever);
    }

    public static void testFindId(DataRetriever dataRetriever){
        Dish dish = dataRetriever.findDishById(1);{
            System.out.println("Les ingrédients du " + dish.getName()+":");
            for (Ingredient ingredient : dish.getIngredients()) {
                System.out.println("- " + ingredient.getName());
        }

            Dish dish1 = dataRetriever.findDishById(999);{
                System.out.println("Les ingrédients du " + dish1.getName()+":");
                for (Ingredient ingredient1 : dish1.getIngredients()) {
                    System.out.println("- " + ingredient1.getName());
                }
            }

        }
    }
    public static  void testFindIngredient(DataRetriever dataRetriever){
        List<Ingredient> ingredient= dataRetriever.findIngredient(2,2);
        System.out.println("Voici le resultat");

        for (Ingredient ingredient1: ingredient){
            System.out.println(ingredient1.getName());
        }
    }
    public static void test(DataRetriever dataRetriever){

    }
}
