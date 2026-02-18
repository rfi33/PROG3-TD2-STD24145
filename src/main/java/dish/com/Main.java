package dish.com;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
        app.testStockValueAt();
        app.test1StockValueAt();
        app.testFindOrderByReferenceNotFound();
    }

    private DishOrder buildDishOrder(int dishId, int qty) {
        DishOrder d = new DishOrder();
        d.setDish(dataRetriever.findDishById(dishId));
        d.setQuantity(qty);
        return d;
    }

    public void testFindId() {
        System.out.println("\n=== Test findDishById() ===");

        Dish dish = dataRetriever.findDishById(1);
        if (dish != null) {
            System.out.println("Plat : " + dish.getName());
            System.out.println("Ingrédients :");
            for (DishIngredient di : dish.getDishIngredients()) {
                System.out.println("  - " + di.getIngredient().getName());
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
            dataRetriever.findDishById(999);
            System.out.println("✗ Erreur : aucune exception levée pour ID 999");
        } catch (RuntimeException e) {
            System.out.println("✓ Exception attendue pour ID 999 : " + e.getMessage());
        }
    }

    public void testGrossMargin() {
        System.out.println("\n=== Test getGrossMargin() ===");

        Dish dishWithPrice = dataRetriever.findDishById(1);
        try {
            System.out.println(dishWithPrice.getName() + " → marge = " + dishWithPrice.getGrossMargin());
        } catch (RuntimeException e) {
            System.out.println("Erreur : " + e.getMessage());
        }

        Dish dishWithoutPrice = dataRetriever.findDishById(3);
        try {
            System.out.println(dishWithoutPrice.getName() + " → marge = " + dishWithoutPrice.getGrossMargin());
        } catch (RuntimeException e) {
            System.out.println(dishWithoutPrice.getName() + " → Exception attendue : " + e.getMessage());
        }
    }

    public void testGetDishCost() {
        System.out.println("\n=== Test getDishCost() ===");

        for (int dishId = 1; dishId <= 5; dishId++) {
            try {
                Dish dish = dataRetriever.findDishById(dishId);
                System.out.printf("%-25s %15.2f%n", dish.getName(), dish.getDishCost());
            } catch (RuntimeException e) {
                System.out.printf("Plat ID %d %15s%n", dishId, "❌ " + e.getMessage());
            }
        }
    }

    public void testGetGrossMarginAllDishes() {
        System.out.println("\n=== Test getGrossMargin() – tous les plats ===");
        System.out.printf("%-25s %20s%n", "Plat", "Marge attendue");
        System.out.println("-".repeat(50));

        for (int dishId = 1; dishId <= 5; dishId++) {
            try {
                Dish dish = dataRetriever.findDishById(dishId);
                try {
                    System.out.printf("%-25s %20.2f%n", dish.getName(), dish.getGrossMargin());
                } catch (RuntimeException e) {
                    System.out.printf("%-25s %20s%n", dish.getName(), "❌ Exception (prix NULL)");
                }
            } catch (RuntimeException e) {
                System.out.printf("Plat ID %d %20s%n", dishId, "❌ Plat non trouvé");
            }
        }
    }

    public void testStockValueAt() {
        System.out.println("\n=== Test getStockValueAt() ===");
        Instant t = LocalDateTime.of(2024, 1, 6, 12, 0).toInstant(ZoneOffset.UTC);

        for (int i = 1; i <= 5; i++) {
            try {
                Ingredient ingredient = dataRetriever.findIngredientById(i);
                StockValue sv = ingredient.getStockValueAt(t);
                System.out.println("Ingredient ID " + i + " : " + ingredient.getName());
                System.out.println("Stock : " + sv.getQuantity()
                        + (sv.getUnit() != null ? " " + sv.getUnit() : ""));
                System.out.println();
            } catch (Exception e) {
                System.out.println("Ingredient ID " + i + " : ERREUR – " + e.getMessage());
                System.out.println();
            }
        }
    }



    public void testSaveOrderWithoutTable() {
        System.out.println("\n=== Test saveOrder() - Sans table spécifiée ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();
            dishOrders.add(buildDishOrder(1, 1));
            order.setDishOrders(dishOrders);
            dataRetriever.saveOrder(order);
            System.out.println("✗ Erreur : La commande aurait dû échouer");

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("table doit être spécifiée")) {
                System.out.println("✓ Exception correctement levée : " + e.getMessage());
            } else {
                System.out.println("✗ Erreur inattendue : " + e.getMessage());
            }
        }
    }


    public void testFindOrderByReferenceNotFound() {
        System.out.println("\n=== Test findOrderByReference() - Commande inexistante ===");
        try {
            dataRetriever.findOrderByReference("ORD99999");
            System.out.println("✗ Erreur : Une exception aurait dû être levée");

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                System.out.println("✓ Exception correctement levée : " + e.getMessage());
            } else {
                System.out.println("✗ Erreur inattendue : " + e.getMessage());
            }
        }
    }

    public void test1StockValueAt() {
        Instant t = LocalDateTime.of(2024, 1, 6, 12, 0).toInstant(ZoneOffset.UTC);

        System.out.printf("%-5s %-15s %15s %15s %10s%n",
                "ID", "Ingrédient", "OO (stock)", "SQL (stock)", "Égaux ?");
        System.out.println("-".repeat(65));

        for (int i = 1; i <= 5; i++) {
            try {
                Ingredient ingredient = dataRetriever.findIngredientById(i);
                StockValue ooResult   = ingredient.getStockValueAt(t);
                StockValue sqlResult  = dataRetriever.getStockValueAt(t, i);

                boolean equal = Math.abs(ooResult.getQuantity() - sqlResult.getQuantity()) < 0.001;

                System.out.printf("%-5d %-15s %15.3f %15.3f %10s%n",
                        i,
                        ingredient.getName(),
                        ooResult.getQuantity(),
                        sqlResult.getQuantity(),
                        equal ? "✓" : "✗ DIFFÉRENT");

            } catch (Exception e) {
                System.out.printf("%-5d %-15s  ERREUR : %s%n", i, "?", e.getMessage());
            }
        }
    }
}