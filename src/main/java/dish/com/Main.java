package dish.com;

import java.sql.SQLException;
import java.util.List;

public class Main {

    private final DataRetriever dataRetriever;

    public Main() {
        this.dataRetriever = new DataRetriever();
    }

    public static void main(String[] args) {
        Main app = new Main();

        app.testFindId();
    }

    public void testFindId() {
        Dish dish = dataRetriever.findDishById(1);

        if (dish != null) {
            System.out.println("Les ingrédients du plat '" + dish.getName() + "' :");
            for (Ingredient ingredient : dish.getIngredients()) {
                System.out.println("- " + ingredient.getName());
            }

            try {
                Double margin = dish.getGrossMargin();
                System.out.println("Marge brute : " + margin);
            } catch (IllegalStateException e) {
                System.out.println("Exception getGrossMargin : " + e.getMessage());
            }
        }

        Dish dish2 = dataRetriever.findDishById(999);
        if (dish2 != null && dish2.getIngredients() != null) {
            System.out.println("Les ingrédients du plat '" + dish2.getName() + "' :");
            for (Ingredient ingredient : dish2.getIngredients()) {
                System.out.println("- " + ingredient.getName());
            }
        } else {
            System.out.println("Aucun plat trouvé avec l'ID 999");
        }
    }
}
/*
    public void testFindIngredient() {
        List<Ingredient> ingredients = dataRetriever.findIngredient(2, 2);

        System.out.println("Résultat de findIngredient :");
        for (Ingredient ingredient : ingredients) {
            System.out.println("- " + ingredient.getName());
        }
    }

    public void testSaveDish() throws SQLException {
        Dish newDish = new Dish();
        newDish.setName("Salade exotique");
        newDish.setPrice(3000.0);
        newDish.setDishType(Dish.DishType.START);
        newDish.setId(1);

        List<Ingredient> ingredients = dataRetriever.findIngredientsByCriteria(
                null, null, "Salade exotique", 1, 100
        );
        newDish.setIngredients(ingredients);

        Dish savedDish = dataRetriever.saveDish(newDish);
        System.out.println("Plat sauvegardé : " + savedDish);

        try {
            System.out.println("Marge brute après save : " + savedDish.getGrossMargin());
        } catch (IllegalStateException e) {
            System.out.println("Exception getGrossMargin : " + e.getMessage());
        }

        savedDish.setPrice(3500.0);
        Dish updatedDish = dataRetriever.saveDish(savedDish);
        System.out.println("Plat mis à jour : " + updatedDish);
        System.out.println("Marge brute après mise à jour : " + updatedDish.getGrossMargin());
    }
}
*/