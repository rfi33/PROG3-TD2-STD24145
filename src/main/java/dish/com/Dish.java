package dish.com;

import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishType dishType;
    private List<Ingredient> ingredients;


    enum DishType{
        START,
        MAIN,
        DESSERT
    }

    private Double getDishPrice(){
    return null;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(ingredients, dish.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, ingredients);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredients +
                '}';
    }

    public Dish(int id, String name, DishType dishType, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
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
