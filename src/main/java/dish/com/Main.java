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

        app.testSaveOrderSuccess();
        app.testSaveOrderInsufficientStock();
        app.testFindOrderByReference();
        app.testFindOrderByReferenceNotFound();
    }

    public void testFindId() {
        Dish dish = dataRetriever.findDishById(1);

        if (dish != null) {
            System.out.println("Plat : " + dish.getName());

            System.out.println("Ingrédients :");
            for (DishIngredient dishIngredient : dish.getDishIngredients()) {
                System.out.println("- " + dishIngredient.getIngredient().getName());
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
                System.out.println(String.format("%-25s %20.2f", dish.getName(), margin));
            } catch (RuntimeException e) {
                try {
                    Dish dish = dataRetriever.findDishById(dishId);
                    System.out.println(String.format("%-25s %20s", dish.getName(), "❌ Exception (prix NULL)"));
                } catch (RuntimeException ex) {
                    System.out.println(String.format("Plat ID %d %20s",
                            dishId, "❌ Plat non trouvé"));
                }
            }
        }
    }

    public void testStockValueAt() {
        Instant t = LocalDateTime.of(2024, 1, 6, 12, 0).toInstant(ZoneOffset.UTC);
        double[] stocksAttendus = {4.8, 3.85, 10.0, 3.0, 2.5};

        for (int i = 1; i <= 5; i++) {
            try {
                Ingredient ingredient = dataRetriever.findIngredientById(i);
                StockValue stockValue = ingredient.getStockValueAt(t);
                double quantity = stockValue.getQuantity();
                double expected = stocksAttendus[i - 1];

                System.out.println("Ingredient ID " + i + " : " + ingredient.getName());
                System.out.println("Stock : " + quantity + " KG");
                System.out.println();

            } catch (Exception e) {
                System.out.println("Ingredient ID " + i + " : ERREUR");
                System.out.println("Message : " + e.getMessage());
                System.out.println();
            }
        }
    }

    public void testSaveOrderSuccess() {
        System.out.println("\n=== Test saveOrder() - Commande avec stock suffisant ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();

            DishOrder dishOrder1 = new DishOrder();
            dishOrder1.setDish(dataRetriever.findDishById(1));
            dishOrder1.setQuantity(1);
            dishOrders.add(dishOrder1);

            DishOrder dishOrder2 = new DishOrder();
            dishOrder2.setDish(dataRetriever.findDishById(2));
            dishOrder2.setQuantity(1);
            dishOrders.add(dishOrder2);

            order.setDishOrders(dishOrders);

            Order savedOrder = dataRetriever.saveOrder(order);

            System.out.println("Référence : " + savedOrder.getReference());
            System.out.println("Montant HT : " + savedOrder.getTotalAmountWithoutVAT()+ " Ar");
            System.out.println("Montant TTC : " + savedOrder.getTotalAmountWithVAT() + " Ar");
            System.out.println("Nombre de plats : " + savedOrder.getDishOrders().size());

            for (DishOrder dishOrder : savedOrder.getDishOrders()) {
                System.out.println("  - " + dishOrder.getDish().getName()
                        + " x" + dishOrder.getQuantity()
                        + " = " + (dishOrder.getDish().getPrice() * dishOrder.getQuantity()) + " Ar");
            }

        } catch (RuntimeException e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    public void testSaveOrderInsufficientStock() {
        System.out.println("\n=== Test saveOrder() - Stock insuffisant ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();

            DishOrder dishOrder = new DishOrder();
            dishOrder.setDish(dataRetriever.findDishById(2));
            dishOrder.setQuantity(100);
            dishOrders.add(dishOrder);

            order.setDishOrders(dishOrders);

            dataRetriever.saveOrder(order);

            System.out.println("✗ Erreur : La commande aurait dû échouer");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Insufficient stock")) {
                System.out.println("✓ Exception correctement levée");
                System.out.println(e.getMessage());
            } else {
                System.out.println("✗ Erreur inattendue : " + e.getMessage());
            }
        }
    }

    public void testFindOrderByReference() {
        System.out.println("\n=== Test findOrderByReference() - Commande existante ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();
            DishOrder dishOrder = new DishOrder();
            dishOrder.setDish(dataRetriever.findDishById(1));
            dishOrder.setQuantity(2);
            dishOrders.add(dishOrder);

            order.setDishOrders(dishOrders);
            Order savedOrder = dataRetriever.saveOrder(order);
            String reference = savedOrder.getReference();

            Order foundOrder = dataRetriever.findOrderByReference(reference);

            System.out.println("✓ Commande trouvée");
            System.out.println("Référence : " + foundOrder.getReference());
            System.out.println("Montant HT : " + foundOrder.getTotalAmountWithVAT() + " Ar");
            System.out.println("Montant TTC : " + foundOrder.getTotalAmountWithVAT() + " Ar");
            System.out.println("Date : " + foundOrder.getCreationDatetime());

        } catch (RuntimeException e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    public void testFindOrderByReferenceNotFound() {
        System.out.println("\n=== Test findOrderByReference() - Commande inexistante ===");
        try {
            dataRetriever.findOrderByReference("ORD99999");
            System.out.println("✗ Erreur : Une exception aurait dû être levée");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                System.out.println("✓ Exception correctement levée");
                System.out.println(e.getMessage());
            } else {
                System.out.println("✗ Erreur inattendue : " + e.getMessage());
            }
        }
    }
}