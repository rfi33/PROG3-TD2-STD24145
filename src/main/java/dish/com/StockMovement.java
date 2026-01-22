package dish.com;

import java.time.Instant;
import java.util.Objects;

public class StockMovement {
    private int id;
    private MovementTypeEnum type;
    private Instant creationDatetime;
    private double quantity;

    public StockMovement(int id, MovementTypeEnum type, Instant creationDatetime, double quantity) {
        this.id = id;
        this.type = type;
        this.creationDatetime = creationDatetime;
        this.quantity = quantity;
    }

    public StockMovement(int id, MovementTypeEnum type, Instant creationDatetime) {
        this(id, type, creationDatetime, 0.0);
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", type=" + type +
                ", quantity=" + quantity +
                ", creationDatetime=" + creationDatetime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StockMovement that = (StockMovement) o;
        return id == that.id &&
                Double.compare(quantity, that.quantity) == 0 &&
                type == that.type &&
                Objects.equals(creationDatetime, that.creationDatetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, quantity, creationDatetime);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MovementTypeEnum getType() {
        return type;
    }

    public void setType(MovementTypeEnum type) {
        this.type = type;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}