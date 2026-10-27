package com.example.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Strict recipe matching - the core business rule of Smart Pantry Manager.
 *
 * A recipe is suggested ONLY when EVERY required ingredient is in the pantry
 * AND the pantry holds at least the required quantity of each one.
 * Partial matches are never returned.
 *
 * To cope with real-world messiness the matcher:
 *  1. normalises names ("Tomatoes" == "tomato", "EGGS" == "egg",
 *     "Olive oil" == "oil"); water is treated as always available,
 *  2. converts units before comparing (1 kg satisfies 500 g, and known
 *     counted items such as eggs are compared by weight: 1 egg = about 50 g),
 *  3. adds up several pantry rows of the same ingredient,
 *  4. fails conservatively when units cannot be compared safely
 *     (e.g. grams vs pieces, or an unknown unit) - the recipe is excluded.
 *
 * This class has no Android dependencies, so it is unit-tested on the JVM.
 */
public final class RecipeMatcher {

    private static final double EPSILON = 1e-9;

    /** Words where simply removing a trailing "s" would be wrong. */
    private static final Set<String> KEEP_AS_IS = new HashSet<>();
    static {
        KEEP_AS_IS.add("hummus");
        KEEP_AS_IS.add("couscous");
        KEEP_AS_IS.add("asparagus");
        KEEP_AS_IS.add("molasses");
        KEEP_AS_IS.add("watercress");
    }

    private RecipeMatcher() { }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /** Returns only the recipes that can be cooked right now with this pantry. */
    public static List<Recipe> findMakeableRecipes(
            List<Recipe> recipes, List<Ingredient> pantry) {

        List<Recipe> makeable = new ArrayList<>();
        if (recipes == null || pantry == null) {
            return makeable;
        }
        Map<String, Double> pantryTotals = buildPantryTotals(pantry);
        for (Recipe recipe : recipes) {
            if (canMake(recipe.getIngredients(), pantryTotals)) {
                makeable.add(recipe);
            }
        }
        return makeable;
    }

    /** True only if every ingredient of the recipe is available in enough quantity. */
    public static boolean canMake(Recipe recipe, List<Ingredient> pantry) {
        if (recipe == null || pantry == null) {
            return false;
        }
        return canMake(recipe.getIngredients(), buildPantryTotals(pantry));
    }

    /** Lower-cases, strips punctuation and reduces each word to a singular form. */
    public static String normalizeName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String cleaned = rawName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .trim()
                .replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            return "";
        }
        String[] words = cleaned.split(" ");
        StringBuilder result = new StringBuilder();
        StringBuilder withDescriptors = new StringBuilder();
        for (String word : words) {
            String singular = singularize(word);
            if (withDescriptors.length() > 0) {
                withDescriptors.append(' ');
            }
            withDescriptors.append(singular);

            // "olive oil" -> "oil", "whole milk" -> "milk"
            if (IngredientKnowledge.isDescriptor(singular)) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(singular);
        }
        // A name made only of descriptor words keeps its full form.
        return result.length() > 0 ? result.toString() : withDescriptors.toString();
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    /**
     * Totals the pantry per (normalised name + unit dimension), in base units.
     * Rows with an unknown unit or a non-positive quantity are ignored.
     */
    private static Map<String, Double> buildPantryTotals(List<Ingredient> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (Ingredient item : pantry) {
            String name = normalizeName(item.getName());
            UnitConverter.Amount amount = toComparable(
                    name, item.getQuantity(), item.getUnit());
            if (amount == null || name.isEmpty() || amount.baseValue <= 0) {
                continue;
            }
            String key = name + "|" + amount.dimension.name();
            Double current = totals.get(key);
            totals.put(key, (current == null ? 0.0 : current) + amount.baseValue);
        }
        return totals;
    }

    /**
     * Base-unit amount used for comparing. Counted units (pieces, cloves, slices)
     * are turned into grams when the weight of one item is known (e.g. 1 egg = 50 g).
     */
    private static UnitConverter.Amount toComparable(
            String normalizedName, double quantity, String unit) {

        UnitConverter.Amount amount = UnitConverter.toBase(quantity, unit);
        if (amount == null) {
            return null;
        }
        return IngredientKnowledge.toComparable(normalizedName, amount);
    }

    private static boolean canMake(
            List<RecipeIngredient> required, Map<String, Double> pantryTotals) {

        if (required == null || required.isEmpty()) {
            return false; // a recipe with no ingredients is never "makeable"
        }

        // Add up duplicates inside the recipe itself (same ingredient listed twice).
        Map<String, Double> needed = new HashMap<>();
        for (RecipeIngredient need : required) {
            String name = normalizeName(need.getName());
            if (IngredientKnowledge.isStaple(name)) {
                continue; // water is always available
            }
            UnitConverter.Amount amount = toComparable(
                    name, need.getQuantity(), need.getUnit());
            if (amount == null || name.isEmpty()) {
                return false; // cannot compare safely -> exclude
            }
            String key = name + "|" + amount.dimension.name();
            Double current = needed.get(key);
            needed.put(key, (current == null ? 0.0 : current) + amount.baseValue);
        }

        for (Map.Entry<String, Double> entry : needed.entrySet()) {
            Double have = pantryTotals.get(entry.getKey());
            if (have == null || have + EPSILON < entry.getValue()) {
                return false; // missing ingredient, wrong unit type or too little
            }
        }
        return true;
    }

    /** Reduces one lower-case word to a singular form (consistent on both sides). */
    static String singularize(String word) {
        if (KEEP_AS_IS.contains(word)) {
            return word;
        }
        if (word.length() >= 3 && word.endsWith("ie")) {   // cookie -> cooky
            return word.substring(0, word.length() - 2) + "y";
        }
        if (word.length() <= 3) {
            return word;
        }
        if (word.endsWith("ies")) {                         // berries -> berry
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes")) {                         // tomatoes -> tomato
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("sses") || word.endsWith("ches")
                || word.endsWith("shes") || word.endsWith("xes")
                || word.endsWith("zes")) {                  // peaches -> peach
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;
        }
        if (word.endsWith("s")) {                           // eggs -> egg
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
