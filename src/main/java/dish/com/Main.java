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
        app.testGrossMargin();
    }

    public void testFindId() {

        Dish dish = dataRetriever.findDishById(1);

        if (dish != null) {
            System.out.println("Plat : " + dish.getName());

            System.out.println("Ingrédients :");
            for (Ingredient ingredient : dish.getIngredients()) {
                System.out.println("- " + ingredient.getName());
            }

            System.out.println("Coût du plat : " + dish.getDishCost());

            try {
                System.out.println("Marge brute : " + dish.getGrossMargin());
            } catch (IllegalStateException e) {
                System.out.println("Exception getGrossMargin : " + e.getMessage());
            }
        }

        Dish dish2 = dataRetriever.findDishById(999);
        if (dish2 == null) {
            System.out.println("Aucun plat trouvé avec l'ID 999");
        }
    }

    public void testGrossMargin() {

        Dish dishWithPrice = dataRetriever.findDishById(1);
        try {
            System.out.println(
                    dishWithPrice.getName() + " → marge = " + dishWithPrice.getGrossMargin()
            );
        } catch (IllegalStateException e) {
            System.out.println("Erreur : " + e.getMessage());
        }

        Dish dishWithoutPrice = dataRetriever.findDishById(3);
        try {
            System.out.println(
                    dishWithoutPrice.getName() + " → marge = " + dishWithoutPrice.getGrossMargin()
            );
        } catch (IllegalStateException e) {
            System.out.println(
                    dishWithoutPrice.getName() + " → Exception attendue : " + e.getMessage()
            );
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