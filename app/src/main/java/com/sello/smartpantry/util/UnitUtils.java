package com.sello.smartpantry.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Helpers for units of measurement: normalising unit names, converting to a
 * base unit so quantities can be compared, and formatting numbers for display.
 *
 * Base units: grams for mass, millilitres for volume, and plain count for pieces.
 */
public final class UnitUtils {

    /** The units offered to the user in the Add/Edit screen. */
    public static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    public static final int TYPE_MASS = 0;
    public static final int TYPE_VOLUME = 1;
    public static final int TYPE_COUNT = 2;
    public static final int TYPE_UNKNOWN = -1;

    private UnitUtils() { }

    /** Turns spellings like "Grams" or "Litres" into one of our standard unit codes. */
    public static String normalizeUnit(String unit) {
        if (unit == null) {
            return "pcs";
        }
        String u = unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "g": case "gram": case "grams":
                return "g";
            case "kg": case "kilogram": case "kilograms":
                return "kg";
            case "ml": case "millilitre": case "millilitres": case "milliliter": case "milliliters":
                return "ml";
            case "l": case "litre": case "litres": case "liter": case "liters":
                return "l";
            case "tsp": case "teaspoon": case "teaspoons":
                return "tsp";
            case "tbsp": case "tablespoon": case "tablespoons":
                return "tbsp";
            case "cup": case "cups":
                return "cup";
            case "": case "pcs": case "pc": case "piece": case "pieces":
                return "pcs";
            default:
                return u;
        }
    }

    /** Which kind of measurement a unit is (mass, volume or count). */
    public static int typeOf(String unit) {
        switch (normalizeUnit(unit)) {
            case "g": case "kg":
                return TYPE_MASS;
            case "ml": case "l": case "tsp": case "tbsp": case "cup":
                return TYPE_VOLUME;
            case "pcs":
                return TYPE_COUNT;
            default:
                return TYPE_UNKNOWN;
        }
    }

    /** Converts a quantity to its base unit (g, ml or pcs) so two amounts can be compared. */
    public static double toBase(double quantity, String unit) {
        switch (normalizeUnit(unit)) {
            case "kg": case "l":
                return quantity * 1000;
            case "tsp":
                return quantity * 5;
            case "tbsp":
                return quantity * 15;
            case "cup":
                return quantity * 240;
            default:
                return quantity; // g, ml and pcs are already base units
        }
    }

    /** Position of a unit in the UNITS array (used to pre-select the spinner). */
    public static int indexOfUnit(String unit) {
        String normalized = normalizeUnit(unit);
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equals(normalized)) {
                return i;
            }
        }
        return 0;
    }

    /** Formats 2.0 as "2" and 2.5 as "2.5". */
    public static String formatQty(double quantity) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(quantity);
    }
}
