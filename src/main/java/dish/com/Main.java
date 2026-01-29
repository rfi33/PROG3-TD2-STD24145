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

        // =====================================================
        // Exécution de tous les tests
        // =====================================================
        app.testFindId();
        app.testGrossMargin();
        app.testGetDishCost();
        app.testGetGrossMarginAllDishes();
        app.testStockValueAt();

        app.testSaveOrderSuccess();
        app.testSaveOrderInsufficientStock();
        app.testFindOrderByReference();
        app.testFindOrderByReferenceNotFound();
        app.testUnitConversion();
        app.testStockConversionAndExpectedStock();
    }

    // =====================================================
    // TESTS DETAILLES
    // =====================================================

    public void testFindId() {
        Dish dish = dataRetriever.findDishById(1);
        if (dish != null) {
            System.out.println("Plat : " + dish.getName());
            System.out.println("Ingrédients :");
            for (DishIngredient di : dish.getDishIngredients()) {
                System.out.println("- " + di.getIngredient().getName());
            }

            try { System.out.println("Coût du plat : " + dish.getDishCost()); }
            catch (RuntimeException e) { System.out.println("Exception getDishCost : " + e.getMessage()); }

            try { System.out.println("Marge brute : " + dish.getGrossMargin()); }
            catch (RuntimeException e) { System.out.println("Exception getGrossMargin : " + e.getMessage()); }
        }

        try {
            Dish dish2 = dataRetriever.findDishById(999);
            if (dish2 == null) System.out.println("Aucun plat trouvé avec l'ID 999");
        } catch (RuntimeException e) { System.out.println("Exception attendue : " + e.getMessage()); }
    }

    public void testGrossMargin() {
        Dish dishWithPrice = dataRetriever.findDishById(1);
        try { System.out.println(dishWithPrice.getName() + " → marge = " + dishWithPrice.getGrossMargin()); }
        catch (RuntimeException e) { System.out.println("Erreur : " + e.getMessage()); }

        Dish dishWithoutPrice = dataRetriever.findDishById(3);
        try { System.out.println(dishWithoutPrice.getName() + " → marge = " + dishWithoutPrice.getGrossMargin()); }
        catch (RuntimeException e) { System.out.println(dishWithoutPrice.getName() + " → Exception attendue : " + e.getMessage()); }
    }

    public void testGetDishCost() {
        for (int dishId = 1; dishId <= 5; dishId++) {
            try {
                Dish dish = dataRetriever.findDishById(dishId);
                System.out.println(String.format("%-25s %15.2f", dish.getName(), dish.getDishCost()));
            } catch (RuntimeException e) {
                try {
                    Dish dish = dataRetriever.findDishById(dishId);
                    System.out.println(String.format("%-25s %15s", dish.getName(), "❌ Exception (" + e.getMessage() + ")"));
                } catch (RuntimeException ex) {
                    System.out.println(String.format("Plat ID %d %15s", dishId, "❌ Plat non trouvé"));
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
                    System.out.println(String.format("Plat ID %d %20s", dishId, "❌ Plat non trouvé"));
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
                System.out.println("Stock : " + quantity + " KG\n");

            } catch (Exception e) {
                System.out.println("Ingredient ID " + i + " : ERREUR");
                System.out.println("Message : " + e.getMessage() + "\n");
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
            System.out.println("Montant HT : " + savedOrder.getTotalAmountWithoutVAT() + " Ar");
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

            DishOrder dishOrder = new DishOrder();
            dishOrder.setDish(dataRetriever.findDishById(1));
            dishOrder.setQuantity(2);

            order.setDishOrders(List.of(dishOrder));
            Order savedOrder = dataRetriever.saveOrder(order);
            String reference = savedOrder.getReference();

            Order foundOrder = dataRetriever.findOrderByReference(reference);
            System.out.println("✓ Commande trouvée : " + foundOrder.getReference());

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
            System.out.println("✓ Exception correctement levée : " + e.getMessage());
        }
    }

    public void testUnitConversion() {
        System.out.println("\n=== Test UnitConversion ===");
        String[] ingredients = {"Tomate", "Laitue", "Chocolat", "Poulet", "Beurre"};
        double[] quantitiesKg = {1.0, 1.0, 2.0, 3.0, 0.5};

        for (int i = 0; i < ingredients.length; i++) {
            String ing = ingredients[i];
            double qtyKg = quantitiesKg[i];

            double qtyPcs = UnitConversion.convert(ing, qtyKg, UnitTypeEnum.KG, UnitTypeEnum.PCS);
            double qtyL = UnitConversion.convert(ing, qtyKg, UnitTypeEnum.KG, UnitTypeEnum.L);
            double qtyKgFromPcs = UnitConversion.convert(ing, qtyPcs, UnitTypeEnum.PCS, UnitTypeEnum.KG);
            double qtyKgFromL = UnitConversion.convert(ing, qtyL, UnitTypeEnum.L, UnitTypeEnum.KG);

            System.out.println("Ingrédient : " + ing);
            System.out.println("  " + qtyKg + " KG -> PCS : " + (qtyPcs != -1 ? qtyPcs : "❌ Conversion impossible"));
            System.out.println("  " + qtyKg + " KG -> L   : " + (qtyL != -1 ? qtyL : "❌ Conversion impossible"));
            if (qtyPcs != -1) System.out.println("  " + qtyPcs + " PCS -> KG : " + qtyKgFromPcs);
            if (qtyL != -1) System.out.println("  " + qtyL + " L   -> KG : " + qtyKgFromL);
            System.out.println();
        }
    }

    public void testStockConversionAndExpectedStock() {
        System.out.println("\n=== Test conversion unités et stock attendu ===");
        Instant t = LocalDateTime.of(2024, 1, 6, 15, 0).toInstant(ZoneOffset.UTC);

        String[] ingredientNames = {"Laitue", "Tomate", "Poulet", "Chocolat", "Beurre"};
        int[] ingredientIds = {1, 2, 3, 4, 5};
        double[] stocksBefore = new double[5];
        double[] outputsInKg = new double[5];
        double[] expectedFinalStocks = {4.0, 3.5, 9.5, 2.6, 2.3};

        System.out.println("- Stocks initiaux -");
        for (int i = 0; i < ingredientNames.length; i++) {
            try {
                Ingredient ing = dataRetriever.findIngredientById(ingredientIds[i]);
                stocksBefore[i] = ing.getStockValueAt(t).getQuantity();
                System.out.println(ingredientNames[i] + " : " + stocksBefore[i] + " KG");
            } catch (Exception e) {
                System.out.println(ingredientNames[i] + " : ERREUR");
                stocksBefore[i] = 0.0;
            }
        }

        System.out.println("\n- Mouvements et conversion d'unités -");
        Object[][] movements = {
                {"Tomate", 5, "PCS"}, {"Laitue", 2, "PCS"}, {"Chocolat", 1, "L"}, {"Poulet", 4, "PCS"}, {"Beurre", 1, "L"}
        };

        for (Object[] m : movements) {
            String name = (String) m[0];
            int qty = (int) m[1];
            String unit = (String) m[2];
            double outKg = UnitConversion.convert(name, (double) qty, UnitTypeEnum.valueOf(unit), UnitTypeEnum.KG);

            for (int i = 0; i < ingredientNames.length; i++) {
                if (ingredientNames[i].equals(name)) outputsInKg[i] = outKg;
            }
            System.out.println(name + " : " + qty + " " + unit + " -> " + outKg + " KG");
        }

        System.out.println("\n- Stock final attendu -");
        for (int i = 0; i < ingredientNames.length; i++) {
            double finalStock = stocksBefore[i] - outputsInKg[i];
            System.out.println(ingredientNames[i] + " : " + stocksBefore[i] + " - " + outputsInKg[i] + " = " + finalStock +
                    " (attendu: " + expectedFinalStocks[i] + ")");
        }
    }
}
