package dish.com;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
    private int id;
    private String references;
    private Instant creation_Datetime;
    private List<DishOrder> dishOrders;


    public Double getTotalAmountWithoutAT(){
        double total = 0.0;
        for (for (int i = 0; i < ; i++) {
            
        }) {
            
        }
        return total;
    }

    public Double getTotalAmountWithAt(){
        double total = 0.0;
        return total;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", references='" + references + '\'' +
                ", creation_Datetime=" + creation_Datetime +
                ", dishOrders=" + dishOrders +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id && Objects.equals(references, order.references) && Objects.equals(creation_Datetime, order.creation_Datetime) && Objects.equals(dishOrders, order.dishOrders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, references, creation_Datetime, dishOrders);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public Instant getCreation_Datetime() {
        return creation_Datetime;
    }

    public void setCreation_Datetime(Instant creation_Datetime) {
        this.creation_Datetime = creation_Datetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public Order(int id, String references, Instant creation_Datetime, List<DishOrder> dishOrders) {
        this.id = id;
        this.references = references;
        this.creation_Datetime = creation_Datetime;
        this.dishOrders = dishOrders;
    }
}
