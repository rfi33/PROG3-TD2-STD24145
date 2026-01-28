package dish.com;

import java.util.Objects;

public class DishIngredient {
    private Integer id;
    private Integer dishId;
    private Ingredient ingredient;
    private double quantityRequired;
    private UnitTypeEnum unitType;

    public DishIngredient() {
    }

    public DishIngredient(Integer id, Integer dishId, Ingredient ingredient, double quantityRequired, UnitTypeEnum unitType) {
        this.id = id;
        this.dishId = dishId;
        this.ingredient = ingredient;
        this.quantityRequired = quantityRequired;
        this.unitType = unitType;
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", dishId=" + dishId +
                ", ingredient=" + ingredient +
                ", quantityRequired=" + quantityRequired +
                ", unitType=" + unitType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredient that = (DishIngredient) o;
        return Double.compare(quantityRequired, that.quantityRequired) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(dishId, that.dishId) &&
                Objects.equals(ingredient, that.ingredient) &&
                unitType == that.unitType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dishId, ingredient, quantityRequired, unitType);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDishId() {
        return dishId;
    }

    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public double getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(double quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public UnitTypeEnum getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitTypeEnum unitType) {
        this.unitType = unitType;
    }
}