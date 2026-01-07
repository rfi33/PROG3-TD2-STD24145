package dish.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishType dishType;
    private List<Ingredient> ingredients = new ArrayList<>();
    private Double price;

    enum DishType {
        START,
        MAIN,
        DESSERT
    };

        private Double getDishCost() {
            return ingredients
                    .stream()
                    .mapToDouble(Ingredient::getPrice)
                    .sum();
        }

    public Double getGrossMargin() {
        if (price == null || price == 0) {
            throw new IllegalStateException(
                    "Le prix de vente n'ayant pas encore de valeur, il est impossible de calculer la marge brute"
            );
        }

        return price - getDishCost();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setDishType(DishType dishType) {
        this.dishType = dishType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Dish() {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
    }

    public Dish(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(ingredients, dish.ingredients) && Objects.equals(price, dish.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, ingredients, price);
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredients +
                ", price=" + price +
                '}';
    }

    public DishType getDishType() {
        return dishType;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
