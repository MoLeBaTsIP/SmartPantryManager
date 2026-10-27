package com.example.smartpantry;

import java.util.Locale;

/**
 * Converts supported units into a base unit so quantities can be compared.
 *
 * Mass is converted to grams, volume to millilitres. Pieces, slices and cloves
 * are each their own dimension: they are NEVER treated as interchangeable with
 * each other or with mass/volume. An unknown unit returns null, which makes the
 * matcher fail conservatively (the recipe is excluded).
 */
public final class UnitConverter {

    public enum Dimension { MASS, VOLUME, PIECE, SLICE, CLOVE }

    /** The units offered in the Add/Edit Ingredient drop-down. */
    public static final String[] SUPPORTED_UNITS = {
            "g", "kg", "oz", "lb", "ml", "l", "tsp", "tbsp", "cup",
            "piece", "slice", "clove"
    };

    /** A quantity expressed in the base unit of its dimension. */
    public static final class Amount {
        public final Dimension dimension;
        public final double baseValue;

        Amount(Dimension dimension, double baseValue) {
            this.dimension = dimension;
            this.baseValue = baseValue;
        }
    }

    private UnitConverter() { }

    /** Returns the amount in base units, or null if the unit is not supported. */
    public static Amount toBase(double quantity, String unit) {
        if (unit == null) {
            return null;
        }
        String u = unit.trim().toLowerCase(Locale.ROOT);
        if (u.endsWith(".")) {
            u = u.substring(0, u.length() - 1);
        }

        switch (u) {
            // ----- mass (base: grams) -----
            case "g": case "gram": case "grams": case "gm":
                return new Amount(Dimension.MASS, quantity);
            case "kg": case "kgs": case "kilogram": case "kilograms":
                return new Amount(Dimension.MASS, quantity * 1000.0);
            case "oz": case "ounce": case "ounces":
                return new Amount(Dimension.MASS, quantity * 28.3495);
            case "lb": case "lbs": case "pound": case "pounds":
                return new Amount(Dimension.MASS, quantity * 453.592);

            // ----- volume (base: millilitres) -----
            case "ml": case "millilitre": case "millilitres":
            case "milliliter": case "milliliters":
                return new Amount(Dimension.VOLUME, quantity);
            case "l": case "litre": case "litres": case "liter": case "liters":
                return new Amount(Dimension.VOLUME, quantity * 1000.0);
            case "tsp": case "teaspoon": case "teaspoons":
                return new Amount(Dimension.VOLUME, quantity * 5.0);
            case "tbsp": case "tablespoon": case "tablespoons":
                return new Amount(Dimension.VOLUME, quantity * 15.0);
            case "cup": case "cups":
                return new Amount(Dimension.VOLUME, quantity * 240.0);

            // ----- counted items: each kind is its own dimension -----
            case "piece": case "pieces": case "pc": case "pcs":
            case "whole": case "each": case "item": case "items":
                return new Amount(Dimension.PIECE, quantity);
            case "slice": case "slices":
                return new Amount(Dimension.SLICE, quantity);
            case "clove": case "cloves":
                return new Amount(Dimension.CLOVE, quantity);

            default:
                return null;
        }
    }
}
