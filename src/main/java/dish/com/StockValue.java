package dish.com;

import java.util.Objects;

public class StockValue {
    private double quantity;
    private UnitTypeEnum unit;

    public StockValue(double quantity, UnitTypeEnum unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public UnitTypeEnum getUnit() {
        return unit;
    }

    public void setUnit(UnitTypeEnum unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StockValue that = (StockValue) o;
        return Double.compare(quantity, that.quantity) == 0
                && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, unit);
    }

    @Override
    public String toString() {
        return "StockValue{" +
                "quantity=" + quantity +
                ", unit=" + unit +
                '}';
    }
}