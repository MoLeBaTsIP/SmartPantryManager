package com.example.smartpantry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Small built-in "common sense" about everyday ingredients. It has no Android
 * dependencies, so it is unit-tested on the JVM together with RecipeMatcher.
 *
 * It exists so that realistic pantries still match the recipes:
 *  - "Olive oil" or "Whole milk" count as "oil" and "milk",
 *  - 300 g of eggs can satisfy "2 pieces" (and the other way round),
 *  - water from the tap is always available,
 *  - the Add Ingredient screen can pre-select a sensible unit.
 */
public final class IngredientKnowledge {

    /** Always available, so a recipe never needs them in the pantry. */
    private static final Set<String> STAPLES = new HashSet<>();

    /** Describing words that do not change what the ingredient is. */
    private static final Set<String> DESCRIPTORS = new HashSet<>();

    /** Average weight in grams of ONE piece (normalised ingredient name). */
    private static final Map<String, Double> GRAMS_PER_PIECE = new HashMap<>();

    /** Average weight in grams of ONE clove. */
    private static final Map<String, Double> GRAMS_PER_CLOVE = new HashMap<>();

    /** Average weight in grams of ONE slice. */
    private static final Map<String, Double> GRAMS_PER_SLICE = new HashMap<>();

    /** Unit that is most natural for an ingredient (normalised name). */
    private static final Map<String, String> SUGGESTED_UNIT = new HashMap<>();

    static {
        STAPLES.add("water");

        String[] descriptors = {
                "olive", "sunflower", "vegetable", "canola", "cooking", "extra",
                "virgin", "fresh", "large", "small", "medium", "ripe", "raw",
                "whole", "skim", "skimmed", "organic", "white", "brown",
                "red", "yellow", "wheat", "plain", "basmati", "jasmine"
        };
        for (String word : descriptors) {
            DESCRIPTORS.add(word);
        }

        GRAMS_PER_PIECE.put("egg", 50.0);
        GRAMS_PER_PIECE.put("tomato", 120.0);
        GRAMS_PER_PIECE.put("onion", 110.0);
        GRAMS_PER_PIECE.put("banana", 120.0);
        GRAMS_PER_PIECE.put("apple", 180.0);
        GRAMS_PER_PIECE.put("potato", 170.0);
        GRAMS_PER_PIECE.put("carrot", 70.0);
        GRAMS_PER_PIECE.put("garlic", 40.0);   // one whole bulb

        GRAMS_PER_CLOVE.put("garlic", 5.0);

        GRAMS_PER_SLICE.put("bread", 30.0);

        String[] pieces = {"egg", "tomato", "onion", "banana", "apple"};
        for (String name : pieces) {
            SUGGESTED_UNIT.put(name, "piece");
        }
        SUGGESTED_UNIT.put("garlic", "clove");
        SUGGESTED_UNIT.put("bread", "slice");
        SUGGESTED_UNIT.put("milk", "ml");
        SUGGESTED_UNIT.put("oil", "ml");
        SUGGESTED_UNIT.put("water", "ml");
    }

    private IngredientKnowledge() { }

    /** True for ingredients everyone has (water), which recipes never need to find in the pantry. */
    public static boolean isStaple(String normalizedName) {
        return STAPLES.contains(normalizedName);
    }

    /** True for words like "olive" or "whole" that can be dropped from an ingredient name. */
    public static boolean isDescriptor(String singularWord) {
        return DESCRIPTORS.contains(singularWord);
    }

    /**
     * Converts a counted amount to grams when the weight of one item is known,
     * otherwise returns the amount unchanged (so it can only match the same unit type).
     */
    public static UnitConverter.Amount toComparable(
            String normalizedName, UnitConverter.Amount amount) {

        Double grams = null;
        switch (amount.dimension) {
            case PIECE:
                grams = GRAMS_PER_PIECE.get(normalizedName);
                break;
            case CLOVE:
                grams = GRAMS_PER_CLOVE.get(normalizedName);
                break;
            case SLICE:
                grams = GRAMS_PER_SLICE.get(normalizedName);
                break;
            default:
                break;
        }
        if (grams == null) {
            return amount;
        }
        return new UnitConverter.Amount(
                UnitConverter.Dimension.MASS, amount.baseValue * grams);
    }

    /** The unit to pre-select for this ingredient name ("g" when unknown). */
    public static String suggestUnit(String rawName) {
        String unit = SUGGESTED_UNIT.get(RecipeMatcher.normalizeName(rawName));
        return unit == null ? "g" : unit;
    }
}
