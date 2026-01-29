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

    // ✅ TABLE DISPONIBLE
    public void testSaveOrderWithAvailableTable() {
        System.out.println("\n=== Test saveOrder() - Table disponible ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            RestaurantTable table = new RestaurantTable();
            table.setId(5); // ✅ table libre
            order.setRestaurantTable(table);
            order.setArrivalDatetime(Instant.now());

            List<DishOrder> dishOrders = new ArrayList<>();
            DishOrder dishOrder = new DishOrder();
            dishOrder.setDish(dataRetriever.findDishById(1));
            dishOrder.setQuantity(1);
            dishOrders.add(dishOrder);
            order.setDishOrders(dishOrders);

            Order savedOrder = dataRetriever.saveOrder(order);

            System.out.println("✓ Commande créée avec succès");
            System.out.println("Référence : " + savedOrder.getReference());
            System.out.println("Table : " + savedOrder.getRestaurantTable().getTableNumber());

        } catch (RuntimeException e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    // ❌ TABLE NON DISPONIBLE
    public void testSaveOrderWithUnavailableTable() {
        System.out.println("\n=== Test saveOrder() - Table non disponible ===");
        try {
            RestaurantTable table = new RestaurantTable();
            table.setId(6);

            Order first = new Order();
            first.setCreationDatetime(Instant.now());
            first.setRestaurantTable(table);
            first.setArrivalDatetime(Instant.now());

            List<DishOrder> list1 = new ArrayList<>();
            DishOrder d1 = new DishOrder();
            d1.setDish(dataRetriever.findDishById(1));
            d1.setQuantity(1);
            list1.add(d1);
            first.setDishOrders(list1);

            dataRetriever.saveOrder(first);
            System.out.println("✓ Première commande créée (table 6 occupée)");

            Order second = new Order();
            second.setCreationDatetime(Instant.now());
            second.setRestaurantTable(table);
            second.setArrivalDatetime(Instant.now());
            second.setDishOrders(list1);

            dataRetriever.saveOrder(second);
            System.out.println("✗ Erreur : la commande aurait dû échouer");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("n'est pas disponible")) {
                System.out.println("✓ Exception correctement levée");
                System.out.println("Message : " + e.getMessage());
            } else {
                System.out.println("✗ Erreur inattendue : " + e.getMessage());
            }
        }
    }

    // ❌ PAS DE TABLE
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
            System.out.println("✗ Erreur : la commande aurait dû échouer");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("table doit être spécifiée")) {
                System.out.println("✓ Exception correctement levée");
                System.out.println("Message : " + e.getMessage());
            } else {
                System.out.println("✗ Erreur inattendue : " + e.getMessage());
            }
        }
    }

    // 🔎 RECHERCHE COMMANDE AVEC TABLE
    public void testFindOrderWithTableInfo() {
        System.out.println("\n=== Test findOrderByReference() ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            RestaurantTable table = new RestaurantTable();
            table.setId(7); // ✅ table libre
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

    // ❌ TABLE OCCUPÉE MAIS AUTRES DISPONIBLES
    public void testUnavailableTableWithAvailableTables() {
        System.out.println("\n=== Test table occupée + tables disponibles ===");
        try {
            RestaurantTable table = new RestaurantTable();
            table.setId(8);

            Order first = new Order();
            first.setCreationDatetime(Instant.now());
            first.setRestaurantTable(table);
            first.setArrivalDatetime(Instant.now());

            List<DishOrder> list = new ArrayList<>();
            DishOrder d = new DishOrder();
            d.setDish(dataRetriever.findDishById(1));
            d.setQuantity(1);
            list.add(d);
            first.setDishOrders(list);

            dataRetriever.saveOrder(first);
            System.out.println("✓ Table 8 occupée");

            Order second = new Order();
            second.setCreationDatetime(Instant.now());
            second.setRestaurantTable(table);
            second.setArrivalDatetime(Instant.now());
            second.setDishOrders(list);

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
