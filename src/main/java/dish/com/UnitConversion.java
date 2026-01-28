package dish.com;

import java.util.HashMap;
import java.util.Map;

public class UnitConversion {

    public static double convert(String ingredient,
                                 double quantity,
                                 UnitTypeEnum fromUnit,
                                 UnitTypeEnum toUnit) {

        Map<String, double[]> conversions = new HashMap<>();
        conversions.put("Tomate",   new double[]{10, -1});
        conversions.put("Laitue",   new double[]{2,  -1});
        conversions.put("Chocolat", new double[]{10, 2.5});
        conversions.put("Poulet",   new double[]{8,  -1});
        conversions.put("Beurre",   new double[]{4,  5});


        if (fromUnit == toUnit) {
            return quantity;
        }

        double[] factors = conversions.get(ingredient);
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
}
