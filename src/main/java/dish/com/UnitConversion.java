package dish.com;

import java.util.Map;

public class UnitConversion {

    private static final Map<String, double[]> CONVERSIONS = Map.of(
            "Tomate",   new double[]{10, -1},
            "Laitue",   new double[]{2,  -1},
            "Chocolat", new double[]{10, 2.5},
            "Poulet",   new double[]{8,  -1},
            "Beurre",   new double[]{4,  5}
    );

    public static double convert(
            String ingredient,
            double quantity,
            UnitTypeEnum fromUnit,
            UnitTypeEnum toUnit
    ) {
        return convertQuantity(ingredient, quantity, fromUnit, toUnit);
    }

    public static double convertStockMovement(
            StockMovement movement,
            String ingredient,
            UnitTypeEnum fromUnit,
            UnitTypeEnum toUnit
    ) {

        double result = convertQuantity(
                ingredient,
                movement.getQuantity(),
                fromUnit,
                toUnit
        );

        if (result == -1) {
            return -1;
        }

        return applyMovementType(result, movement.getType());
    }

    private static double convertQuantity(
            String ingredient,
            double quantity,
            UnitTypeEnum fromUnit,
            UnitTypeEnum toUnit
    ) {

        if (fromUnit == toUnit) {
            return quantity;
        }

        double[] factors = CONVERSIONS.get(ingredient);
        if (factors == null) {
            return -1;
        }

        double quantityInKg;

        if (fromUnit == UnitTypeEnum.KG) {
            quantityInKg = quantity;
        } else if (fromUnit == UnitTypeEnum.PCS) {
            if (factors[0] == -1) return -1;
            quantityInKg = quantity / factors[0];
        } else if (fromUnit == UnitTypeEnum.L) {
            if (factors[1] == -1) return -1;
            quantityInKg = quantity / factors[1];
        } else {
            return -1;
        }

        if (toUnit == UnitTypeEnum.KG) {
            return quantityInKg;
        } else if (toUnit == UnitTypeEnum.PCS) {
            if (factors[0] == -1) return -1;
            return quantityInKg * factors[0];
        } else if (toUnit == UnitTypeEnum.L) {
            if (factors[1] == -1) return -1;
            return quantityInKg * factors[1];
        }

        return -1;
    }

    private static double applyMovementType(double quantity, MovementTypeEnum type) {
        return type == MovementTypeEnum.OUT ? -quantity : quantity;
    }
}
