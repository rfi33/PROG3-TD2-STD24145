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
    }

    public void testSaveOrderWithAvailableTable() {
        System.out.println("\n=== Test saveOrder() - Table disponible ===");
        try {
            Order order = new Order();
            order.setCreationDatetime(Instant.now());

            // Spécifier la table
            RestaurantTable table = new RestaurantTable();
            table.setId(1); // Table numéro 1
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
            System.out.println("Heure d'arrivée : " + savedOrder.getArrivalDatetime());
            System.out.println("Montant TTC : " + savedOrder.getTotalAmountWithVAT() + " Ar");

        } catch (RuntimeException e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    public void testSaveOrderWithUnavailableTable() {
        System.out.println("\n=== Test saveOrder() - Table non disponible ===");
        try {
            // Créer une première commande pour occuper une table
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

            // Tenter de créer une deuxième commande sur la même table
            Order order2 = new Order();
            order2.setCreationDatetime(Instant.now());
            order2.setRestaurantTable(table); // Même table
            order2.setArrivalDatetime(Instant.now());

            List<DishOrder> dishOrders2 = new ArrayList<>();
            DishOrder dishOrder2 = new DishOrder();
            dishOrder2.setDish(dataRetriever.findDishById(2));
            dishOrder2.setQuantity(1);
            dishOrders2.add(dishOrder2);
            order2.setDishOrders(dishOrders2);

            dataRetriever.saveOrder(order2);

            System.out.println("✗ Erreur : La commande aurait dû échouer (table occupée)");

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
            // Pas de table spécifiée

            List<DishOrder> dishOrders = new ArrayList<>();
            DishOrder dishOrder = new DishOrder();
            dishOrder.setDish(dataRetriever.findDishById(1));
            dishOrder.setQuantity(1);
            dishOrders.add(dishOrder);
            order.setDishOrders(dishOrders);

            dataRetriever.saveOrder(order);

            System.out.println("✗ Erreur : La commande aurait dû échouer (pas de table)");

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
        System.out.println("\n=== Test findOrderByReference() - Avec informations de table ===");
        try {
            // Créer une commande
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
            String reference = savedOrder.getReference();

            // Récupérer la commande
            Order foundOrder = dataRetriever.findOrderByReference(reference);

            System.out.println("✓ Commande trouvée");
            System.out.println("Référence : " + foundOrder.getReference());
            System.out.println("Table numéro : " + foundOrder.getRestaurantTable().getTableNumber());
            System.out.println("Heure d'arrivée : " + foundOrder.getArrivalDatetime());
            System.out.println("Heure de départ : " +
                    (foundOrder.getDepartureDatetime() != null ? foundOrder.getDepartureDatetime() : "Non définie"));
            System.out.println("Montant TTC : " + foundOrder.getTotalAmountWithVAT() + " Ar");

        } catch (RuntimeException e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }
}