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

        app.testGetDishCost();

        app.testGetGrossMarginAllDishes();

    }

    public void testFindId() {
        Dish dish = dataRetriever.findDishById(1);

        if (dish != null) {
            System.out.println("Plat : " + dish.getName());

            System.out.println("Ingrédients :");
            for (Ingredient ingredient : dish.getIngredients()) {
                System.out.println("- " + ingredient.getName());
            }

            try {
                System.out.println("Coût du plat : " + dish.getDishCost());
            } catch (RuntimeException e) {
                System.out.println("Exception getDishCost : " + e.getMessage());
            }

            try {
                System.out.println("Marge brute : " + dish.getGrossMargin());
            } catch (RuntimeException e) {
                System.out.println("Exception getGrossMargin : " + e.getMessage());
            }
        }

        try {
            Dish dish2 = dataRetriever.findDishById(999);
            if (dish2 == null) {
                System.out.println("Aucun plat trouvé avec l'ID 999");
            }
        } catch (RuntimeException e) {
            System.out.println("Exception attendue : " + e.getMessage());
        }
    }

    public void testGrossMargin() {
        Dish dishWithPrice = dataRetriever.findDishById(1);
        try {
            System.out.println(
                    dishWithPrice.getName() + " → marge = " + dishWithPrice.getGrossMargin()
            );
        } catch (RuntimeException e) {
            System.out.println("Erreur : " + e.getMessage());
        }

        Dish dishWithoutPrice = dataRetriever.findDishById(3);
        try {
            System.out.println(
                    dishWithoutPrice.getName() + " → marge = " + dishWithoutPrice.getGrossMargin()
            );
        } catch (RuntimeException e) {
            System.out.println(
                    dishWithoutPrice.getName() + " → Exception attendue : " + e.getMessage()
            );
        }
    }

    public void testGetDishCost() {
        System.out.println("Pour la méthode getDishCost() :");
        System.out.println(String.format("%-25s %15s", "Plat", "Coût attendu"));
        System.out.println("-".repeat(45));

        for (int dishId = 1; dishId <= 5; dishId++) {
            try {
                Dish dish = dataRetriever.findDishById(dishId);
                Double cost = dish.getDishCost();
                System.out.println(String.format("%-25s %15.2f", dish.getName(), cost));
            } catch (RuntimeException e) {
                try {
                    Dish dish = dataRetriever.findDishById(dishId);
                    System.out.println(String.format("%-25s %15s",
                            dish.getName(), "❌ Exception (" + e.getMessage() + ")"));
                } catch (RuntimeException ex) {
                    System.out.println(String.format("Plat ID %d %15s",
                            dishId, "❌ Plat non trouvé"));
                }
            }
        }
    }

    public void testGetGrossMarginAllDishes() {
        System.out.println("Pour la méthode getGrossMargin() :");
        System.out.println(String.format("%-25s %20s", "Plat", "Marge attendue"));
        System.out.println("-".repeat(50));

        for (int dishId = 1; dishId <= 5; dishId++) {
            try {
                Dish dish = dataRetriever.findDishById(dishId);
                Double margin = dish.getGrossMargin();
                System.out.println(String.format(dish.getName(), margin));
            } catch (RuntimeException e) {
                try {
                    Dish dish = dataRetriever.findDishById(dishId);
                    System.out.println(String.format(dish.getName(), "❌ Exception (prix NULL)"));
                } catch (RuntimeException ex) {
                    System.out.println(String.format("Plat ID %d %20s",
                            dishId, "❌ Plat non trouvé"));
                }
            }
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
        newDish.setDishType(DishTypeEnum.START);
        newDish.setId(1);

        List<Ingredient> ingredients = dataRetriever.findIngredientsByCriteria(
                null, null, "Salade exotique", 1, 100
        );
        newDish.setIngredients(ingredients);

        Dish savedDish = dataRetriever.saveDish(newDish);
        System.out.println("Plat sauvegardé : " + savedDish);

        try {
            System.out.println("Marge brute après save : " + savedDish.getGrossMargin());
        } catch (RuntimeException e) {
            System.out.println("Exception getGrossMargin : " + e.getMessage());
        }

        savedDish.setPrice(3500.0);
        Dish updatedDish = dataRetriever.saveDish(savedDish);
        System.out.println("Plat mis à jour : " + updatedDish);
        System.out.println("Marge brute après mise à jour : " + updatedDish.getGrossMargin());
    }
}
        }*/