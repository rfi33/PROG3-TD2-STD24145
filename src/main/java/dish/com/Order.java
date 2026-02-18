package dish.com;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
    private int id;
    private String reference;
    private Double totalAmountHT;
    private Double totalAmountTTC;
    private Instant creationDatetime;
    private Instant arrivalDatetime;
    private Instant departureDatetime;
    private RestaurantTable restaurantTable;
    private List<DishOrder> dishOrders;

    public Order() {
    }

    public Order(int id, String reference, Instant creationDatetime, List<DishOrder> dishOrders) {
        this.id = id;
        this.reference = reference;
        this.creationDatetime = creationDatetime;
        this.dishOrders = dishOrders;
    }

    public Double getTotalAmountWithoutVAT() {
        if (dishOrders == null || dishOrders.isEmpty()) {
            return 0.0;
        }

        double totalHT = 0.0;
        for (DishOrder dishOrder : dishOrders) {
            if (dishOrder.getDish() != null && dishOrder.getDish().getPrice() != null) {
                totalHT += dishOrder.getDish().getPrice() * dishOrder.getQuantity();
            }
        }
        return totalHT;
    }

    public Double getTotalAmountWithVAT() {
        return getTotalAmountWithoutVAT() * 1.20;
    }

    // ---- Getters / Setters ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setTotalAmountHT(Double totalAmountHT) {
        this.totalAmountHT = totalAmountHT;
    }

    public void setTotalAmountTTC(Double totalAmountTTC) {
        this.totalAmountTTC = totalAmountTTC;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public Instant getArrivalDatetime() {
        return arrivalDatetime;
    }

    public void setArrivalDatetime(Instant arrivalDatetime) {
        this.arrivalDatetime = arrivalDatetime;
    }

    public Instant getDepartureDatetime() {
        return departureDatetime;
    }

    public void setDepartureDatetime(Instant departureDatetime) {
        this.departureDatetime = departureDatetime;
    }

    public RestaurantTable getRestaurantTable() {
        return restaurantTable;
    }

    public void setRestaurantTable(RestaurantTable restaurantTable) {
        this.restaurantTable = restaurantTable;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", totalAmountHT=" + totalAmountHT +
                ", totalAmountTTC=" + totalAmountTTC +
                ", creationDatetime=" + creationDatetime +
                ", arrivalDatetime=" + arrivalDatetime +
                ", departureDatetime=" + departureDatetime +
                ", restaurantTable=" + restaurantTable +
                ", dishOrders=" + dishOrders +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id &&
                Objects.equals(reference, order.reference) &&
                Objects.equals(totalAmountHT, order.totalAmountHT) &&
                Objects.equals(totalAmountTTC, order.totalAmountTTC) &&
                Objects.equals(creationDatetime, order.creationDatetime) &&
                Objects.equals(arrivalDatetime, order.arrivalDatetime) &&
                Objects.equals(departureDatetime, order.departureDatetime) &&
                Objects.equals(restaurantTable, order.restaurantTable) &&
                Objects.equals(dishOrders, order.dishOrders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, totalAmountHT, totalAmountTTC,
                creationDatetime, arrivalDatetime, departureDatetime,
                restaurantTable, dishOrders);
    }
}