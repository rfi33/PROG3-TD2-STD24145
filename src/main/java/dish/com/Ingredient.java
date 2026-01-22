package dish.com;

import kotlin.Unit;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Ingredient {
    private Integer id;
    private String name;
    private CategoryEnum category;
    private Double price;
    private List<StockMovement> stockMovementList;

    public StockValue getStockValueAt(Instant instant) {
        double quantity = stockMovementList == null
                ? 0.0
                : stockMovementList.stream()
                .filter(m -> !m.getCreationDatetime().isAfter(instant))
                .mapToDouble(m -> {
                    double qty = m.getQuantity();
                    return m.getType() == MovementTypeEnum.IN ? qty : -qty;
                })
                .sum();

        return new StockValue(quantity, Unit.INSTANCE);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", stockMovementList=" + stockMovementList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && category == that.category && Objects.equals(price, that.price) && Objects.equals(stockMovementList, that.stockMovementList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, price, stockMovementList);
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public Ingredient(Integer id, String name, CategoryEnum category, Double price, List<StockMovement> stockMovementList) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockMovementList = stockMovementList;
    }

    public Ingredient() {
    }

    public Ingredient(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}