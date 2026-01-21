package dish.com;

import java.time.Instant;
import java.util.Objects;

public class StockMovement {
    private int id;
    private MovementTypeEnum type;
    private Instant creationDatetime;

    public StockMovement(int id, MovementTypeEnum type, Instant creationDatetime) {
        this.id = id;
        this.type = type;
        this.creationDatetime = creationDatetime;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", type=" + type +
                ", creationDatetime=" + creationDatetime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StockMovement that = (StockMovement) o;
        return id == that.id && type == that.type && Objects.equals(creationDatetime, that.creationDatetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, creationDatetime);
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
}
