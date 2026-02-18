package dish.com;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    private final DataRetriever dataRetriever;

    public Main() {
        this.dataRetriever = new DataRetriever();
    }

    public static void main(String[] args) {
        Main app = new Main();

        // ── Tests existants ────────────────────────────────────────────────────
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

        // ── TD5 - Question 1 : push-down stock ────────────────────────────────
        app.testPushDownStockValueAt();

        // ── TD5 - Question 2 : push-down coût et marge ────────────────────────
        app.testPushDownDishCost();
        app.testPushDownGrossMargin();

        // ── TD5 - Question 3 : statistiques par période ───────────────────────
        try {
            app.testStockStatsByPeriod();
        } catch (Exception e) {
            System.out.println("Erreur Q3 : " + e.getMessage());
        }
    }

    // ==========================================================================
    // TD5 - Question 1 : comparaison OO vs push-down pour l'état de stock
    // ==========================================================================

    public void testPushDownStockValueAt() {
        System.out.println("\n=== TD5 Q1 – Push-down : état de stock à une date donnée ===");

        // Même instant que testStockValueAt() pour comparer les résultats
        Instant t = LocalDateTime.of(2024, 1, 6, 12, 0).toInstant(ZoneOffset.UTC);

        System.out.println(String.format("%-5s %-20s %15s %15s %10s",
                "ID", "Ingrédient", "Stock OO", "Stock DB", "Égaux ?"));
        System.out.println("-".repeat(70));

        for (int i = 1; i <= 5; i++) {
            try {
                // Approche OO (existante)
                Ingredient ingredient = dataRetriever.findIngredientById(i);
                double stockOO = ingredient.getStockValueAt(t).getQuantity();

                // Approche push-down (TD5)
                StockValue stockDB = dataRetriever.getStockValueAt(t, i);
                double stockDBQty = stockDB.getQuantity();

                boolean equal = Math.abs(stockOO - stockDBQty) < 0.0001;

                System.out.println(String.format("%-5d %-20s %15.4f %15.4f %10s",
                        i, ingredient.getName(), stockOO, stockDBQty, equal ? "✓" : "✗ DIFFÉRENT"));

            } catch (Exception e) {
                System.out.println("Ingrédient ID " + i + " : ERREUR – " + e.getMessage());
            }
        }
    }

    // ==========================================================================
    // TD5 - Question 2a : push-down coût d'un plat
    // ==========================================================================

    public void testPushDownDishCost() {
        System.out.println("\n=== TD5 Q2a – Push-down : coût d'un plat ===");
        System.out.println(String.format("%-5s %-25s %15s %15s %10s",
                "ID", "Plat", "Coût OO", "Coût DB", "Égaux ?"));
        System.out.println("-".repeat(75));

        for (int dishId = 1; dishId <= 5; dishId++) {
            try {
                Dish dish = dataRetriever.findDishById(dishId);

                // Approche OO
                double costOO = dish.getDishCost();

                // Approche push-down
                double costDB = dataRetriever.getDishCost(dishId);

                boolean equal = Math.abs(costOO - costDB) < 0.01;

                System.out.println(String.format("%-5d %-25s %15.2f %15.2f %10s",
                        dishId, dish.getName(), costOO, costDB, equal ? "✓" : "✗ DIFFÉRENT"));

            } catch (Exception e) {
                System.out.println(String.format("%-5d %-25s %s",
                        dishId, "???", "ERREUR – " + e.getMessage()));
            }
        }
    }

    // ==========================================================================
    // TD5 - Question 2b : push-down marge brute
    // ==========================================================================

    public void testPushDownGrossMargin() {
        System.out.println("\n=== TD5 Q2b – Push-down : marge brute d'un plat ===");
        System.out.println(String.format("%-5s %-25s %15s %15s %10s",
                "ID", "Plat", "Marge OO", "Marge DB", "Égaux ?"));
        System.out.println("-".repeat(75));

        for (int dishId = 1; dishId <= 5; dishId++) {
            try {
                Dish dish = dataRetriever.findDishById(dishId);

                // Approche OO
                double marginOO = dish.getGrossMargin();

                // Approche push-down
                double marginDB = dataRetriever.getGrossMargin(dishId);

                boolean equal = Math.abs(marginOO - marginDB) < 0.01;

                System.out.println(String.format("%-5d %-25s %15.2f %15.2f %10s",
                        dishId, dish.getName(), marginOO, marginDB, equal ? "✓" : "✗ DIFFÉRENT"));

            } catch (RuntimeException e) {
                // Ex : prix de vente NULL → marge impossible
                String reason = e.getMessage().contains("NULL") || e.getMessage().contains("null")
                        ? "Prix NULL (attendu)"
                        : e.getMessage();
                System.out.println(String.format("%-5d %-25s %s",
                        dishId,
                        dataRetriever.findDishById(dishId).getName(),
                        "⚠ " + reason));
            }
        }
    }

    // ==========================================================================
    // TD5 - Question 3 : statistiques de stock par période
    // ==========================================================================

    public void testStockStatsByPeriod() {
        System.out.println("\n=== TD5 Q3 – Statistiques de stock par période ===");

        // Exemple : DAY, du 05/01/2024 au 07/01/2024
        LocalDate from = LocalDate.of(2024, 1, 4);
        LocalDate to   = LocalDate.of(2024, 1, 7);

        System.out.println("\n[ Périodicité = JOUR | " + from + " → " + to + " ]");
        dataRetriever.printStockStatsByPeriod(DataRetriever.Periodicity.DAY, from, to);

        // Exemple : WEEK
        LocalDate fromW = LocalDate.of(2024, 1, 1);
        LocalDate toW   = LocalDate.of(2024, 1, 31);

        System.out.println("\n[ Périodicité = SEMAINE | " + fromW + " → " + toW + " ]");
        dataRetriever.printStockStatsByPeriod(DataRetriever.Periodicity.WEEK, fromW, toW);

        // Exemple : MONTH
        LocalDate fromM = LocalDate.of(2024, 1, 1);
        LocalDate toM   = LocalDate.of(2024, 3, 31);

        System.out.println("\n[ Périodicité = MOIS | " + fromM + " → " + toM + " ]");
        dataRetriever.printStockStatsByPeriod(DataRetriever.Periodicity.MONTH, fromM, toM);
    }

    // ==========================================================================
    // Tests existants (inchangés)
    // ==========================================================================

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
            dataRetriever.findDishById(999);
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
                System.out.println(String.format("Plat ID %d %15s", dishId, "❌ " + e.getMessage()));
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

        for (int i = 1; i <= 5; i++) {
            try {
                Ingredient ingredient = dataRetriever.findIngredientById(i);
                StockValue stockValue = ingredient.getStockValueAt(t);
                System.out.println("Ingredient ID " + i + " : " + ingredient.getName());
                System.out.println("Stock : " + stockValue.getQuantity() + " KG");
                System.out.println();
            } catch (Exception e) {
                System.out.println("Ingredient ID " + i + " : ERREUR – " + e.getMessage());
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

    public void testUnitConversion() {
        System.out.println("\n=== Test UnitConversion ===");
        String[] ingredients = {"Tomate", "Laitue", "Chocolat", "Poulet", "Beurre"};
        double[] quantitiesKg = {1.0, 1.0, 2.0, 3.0, 0.5};

        for (int i = 0; i < ingredients.length; i++) {
            String ing = ingredients[i];
            double qtyKg = quantitiesKg[i];

            double qtyPcs = UnitConversion.convert(ing, qtyKg, UnitTypeEnum.KG, UnitTypeEnum.PCS);
            double qtyL   = UnitConversion.convert(ing, qtyKg, UnitTypeEnum.KG, UnitTypeEnum.L);

            System.out.println("Ingrédient : " + ing);
            System.out.println("  " + qtyKg + " KG -> PCS : " + (qtyPcs != -1 ? qtyPcs : "❌ Conversion impossible"));
            System.out.println("  " + qtyKg + " KG -> L   : " + (qtyL   != -1 ? qtyL   : "❌ Conversion impossible"));
            System.out.println();
        }
    }
}