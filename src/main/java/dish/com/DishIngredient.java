package dish.com;

import java.util.Objects;

public class DishIngredient {
    private int id;
    private Dish dish;
    private Ingredient ingredient;
    private double quantity_required;
    private UnitTypeEnum unitType;


    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", dish=" + dish +
                ", ingredient=" + ingredient +
                ", quantity_required=" + quantity_required +
                ", unitType=" + unitType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredient that = (DishIngredient) o;
        return id == that.id && Double.compare(quantity_required, that.quantity_required) == 0 && Objects.equals(dish, that.dish) && Objects.equals(ingredient, that.ingredient) && unitType == that.unitType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dish, ingredient, quantity_required, unitType);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public double getQuantity_required() {
        return quantity_required;
    }

    public void setQuantity_required(double quantity_required) {
        this.quantity_required = quantity_required;
    }

    public UnitTypeEnum getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitTypeEnum unitType) {
        this.unitType = unitType;
    }

    public DishIngredient(int id, Dish dish, Ingredient ingredient, double quantity_required, UnitTypeEnum unitType) {
        this.id = id;
        this.dish = dish;
        this.ingredient = ingredient;
        this.quantity_required = quantity_required;
        this.unitType = unitType;
    }
}
