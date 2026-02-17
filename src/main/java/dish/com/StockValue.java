package dish.com;

import kotlin.Unit;

import java.util.Objects;

public class StockValue {
    private double quantity;
    private UnitTypeEnum unit;

    @Override
    public String toString() {
        return "StockValue{" +
                "quantity=" + quantity +
                ", unit=" + unit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StockValue that = (StockValue) o;
        return Double.compare(quantity, that.quantity) == 0 && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, unit);
    }

    public StockValue(double quantity, Unit unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnit(UnitTypeEnum unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public UnitTypeEnum getUnit() {
        return unit;
    }
}
