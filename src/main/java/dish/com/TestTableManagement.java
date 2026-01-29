package dish.com;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestTableManagement {

    private final DataRetriever dataRetriever;

    public TestTableManagement() {
        this.dataRetriever = new DataRetriever();
    }

    public static void main(String[] args) {
        TestTableManagement test = new TestTableManagement();

        test.testSaveOrderWithAvailableTable();
        test.testSaveOrderWithUnavailableTable();
        test.testSaveOrderWithoutTable();
        test.testFindOrderWithTableInfo();
        test.testUnavailableTableWithAvailableTables();
    }

    public void testSaveOrderWithAvailableTable() {
        System.out.println("\n=== Test saveOrder() - Table disponible ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            RestaurantTable table = new RestaurantTable();
            table.setId(1);
            order.setRestaurantTable(table);
            order.setArrivalDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();
            DishOrder dishOrder1 = new DishOrder();
            dishOrder1.setDish(dataRetriever.findDishById(1));
            dishOrder1.setQuantity(1);
            dishOrders.add(dishOrder1);

            order.setDishOrders(dishOrders);

            Order savedOrder = dataRetriever.saveOrder(order);

            System.out.println("✓ Commande créée avec succès");
            System.out.println("Référence : " + savedOrder.getReference());
            System.out.println("Table : " + savedOrder.getRestaurantTable().getTableNumber());

        } catch (RuntimeException e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    public void testSaveOrderWithUnavailableTable() {
        System.out.println("\n=== Test saveOrder() - Table non disponible ===");
        try {
            Order order1 = new Order();
            order1.setCreationDatetime(Instant.now());

            RestaurantTable table = new RestaurantTable();
            table.setId(2);
            order1.setRestaurantTable(table);
            order1.setArrivalDatetime(Instant.now());

            List<DishOrder> dishOrders1 = new ArrayList<>();
            DishOrder dishOrder1 = new DishOrder();
            dishOrder1.setDish(dataRetriever.findDishById(1));
            dishOrder1.setQuantity(1);
            dishOrders1.add(dishOrder1);
            order1.setDishOrders(dishOrders1);

            dataRetriever.saveOrder(order1);
            System.out.println("✓ Première commande créée (table 2 occupée)");

            Order order2 = new Order();
            order2.setCreationDatetime(Instant.now());
            order2.setRestaurantTable(table);
            order2.setArrivalDatetime(Instant.now());

            List<DishOrder> dishOrders2 = new ArrayList<>();
            DishOrder dishOrder2 = new DishOrder();
            dishOrder2.setDish(dataRetriever.findDishById(2));
            dishOrder2.setQuantity(1);
            dishOrders2.add(dishOrder2);
            order2.setDishOrders(dishOrders2);

            dataRetriever.saveOrder(order2);

            System.out.println("✗ Erreur : La commande aurait dû échouer");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("n'est pas disponible")) {
                System.out.println("✓ Exception correctement levée");
                System.out.println("Message : " + e.getMessage());
            } else {
                System.out.println("✗ Erreur inattendue : " + e.getMessage());
            }
        }
    }

    public void testSaveOrderWithoutTable() {
        System.out.println("\n=== Test saveOrder() - Sans table spécifiée ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();
            DishOrder dishOrder = new DishOrder();
            dishOrder.setDish(dataRetriever.findDishById(1));
            dishOrder.setQuantity(1);
            dishOrders.add(dishOrder);
            order.setDishOrders(dishOrders);

            dataRetriever.saveOrder(order);

            System.out.println("✗ Erreur : La commande aurait dû échouer");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("table doit être spécifiée")) {
                System.out.println("✓ Exception correctement levée");
                System.out.println("Message : " + e.getMessage());
            } else {
                System.out.println("✗ Erreur inattendue : " + e.getMessage());
            }
        }
    }

    public void testFindOrderWithTableInfo() {
        System.out.println("\n=== Test findOrderByReference() ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            RestaurantTable table = new RestaurantTable();
            table.setId(3);
            order.setRestaurantTable(table);
            order.setArrivalDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();
            DishOrder dishOrder = new DishOrder();
            dishOrder.setDish(dataRetriever.findDishById(1));
            dishOrder.setQuantity(2);
            dishOrders.add(dishOrder);
            order.setDishOrders(dishOrders);

            Order savedOrder = dataRetriever.saveOrder(order);
            Order foundOrder = dataRetriever.findOrderByReference(savedOrder.getReference());

            System.out.println("✓ Commande trouvée");
            System.out.println("Table : " + foundOrder.getRestaurantTable().getTableNumber());

        } catch (RuntimeException e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    // ✅ NOUVEAU TEST : table occupée MAIS autres tables disponibles
    public void testUnavailableTableWithAvailableTables() {
        System.out.println("\n=== Test table occupée + tables disponibles ===");
        try {
            Order first = new Order();
            first.setCreationDatetime(Instant.now());

            RestaurantTable table = new RestaurantTable();
            table.setId(4);
            first.setRestaurantTable(table);
            first.setArrivalDatetime(Instant.now());

            List<DishOrder> list1 = new ArrayList<>();
            DishOrder d1 = new DishOrder();
            d1.setDish(dataRetriever.findDishById(1));
            d1.setQuantity(1);
            list1.add(d1);
            first.setDishOrders(list1);

            dataRetriever.saveOrder(first);
            System.out.println("✓ Table 4 occupée");

            Order second = new Order();
            second.setCreationDatetime(Instant.now());
            second.setRestaurantTable(table);
            second.setArrivalDatetime(Instant.now());
            second.setDishOrders(list1);

            dataRetriever.saveOrder(second);

            System.out.println("✗ Erreur : la commande aurait dû échouer");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Tables disponibles")) {
                System.out.println("✓ Message avec tables disponibles OK");
                System.out.println("Message : " + e.getMessage());
            } else {
                System.out.println("✗ Message incorrect : " + e.getMessage());
            }
        }
    }
}
