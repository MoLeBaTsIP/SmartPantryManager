package com.example.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** JVM unit tests for the strict matching rule (run: right-click > Run 'RecipeMatcherTest'). */
public class RecipeMatcherTest {

    private static int nextId = 1;

    private static Ingredient pantry(String name, double qty, String unit) {
        return new Ingredient(nextId++, name, qty, unit, null);
    }

    private static Recipe recipe(String name, RecipeIngredient... needs) {
        Recipe r = new Recipe(nextId++, name, "1. Cook.");
        for (RecipeIngredient need : needs) {
            r.addIngredient(need);
        }
        return r;
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(nextId++, 1, name, qty, unit);
    }

    private final Recipe omelette = recipe("Omelette",
            need("egg", 2, "pieces"), need("cheese", 30, "g"), need("oil", 5, "ml"));

    @Test
    public void recipeAppearsWhenEveryIngredientAndQuantityIsAvailable() {
        List<Ingredient> p = Arrays.asList(
                pantry("Egg", 6, "piece"), pantry("Cheese", 100, "g"), pantry("Oil", 500, "ml"));
        assertTrue(RecipeMatcher.canMake(omelette, p));
        assertEquals(1, RecipeMatcher.findMakeableRecipes(Arrays.asList(omelette), p).size());
    }

    @Test
    public void recipeExcludedWhenOneIngredientIsMissing() {
        List<Ingredient> p = Arrays.asList(pantry("Egg", 6, "piece"), pantry("Cheese", 100, "g"));
        assertFalse(RecipeMatcher.canMake(omelette, p));
        assertTrue(RecipeMatcher.findMakeableRecipes(Arrays.asList(omelette), p).isEmpty());
    }

    @Test
    public void recipeExcludedWhenQuantityIsInsufficient() {
        List<Ingredient> p = Arrays.asList(
                pantry("Egg", 1, "piece"), pantry("Cheese", 100, "g"), pantry("Oil", 500, "ml"));
        assertFalse(RecipeMatcher.canMake(omelette, p));
    }

    @Test
    public void exactQuantityIsEnough() {
        List<Ingredient> p = Arrays.asList(
                pantry("egg", 2, "pieces"), pantry("cheese", 30, "g"), pantry("oil", 5, "ml"));
        assertTrue(RecipeMatcher.canMake(omelette, p));
    }

    @Test
    public void singularAndPluralNamesMatch() {
        Recipe soup = recipe("Soup", need("tomato", 2, "pieces"), need("potato", 1, "piece"));
        List<Ingredient> p = Arrays.asList(
                pantry("Tomatoes", 3, "pieces"), pantry("POTATOES", 2, "piece"));
        assertTrue(RecipeMatcher.canMake(soup, p));
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
        assertEquals("egg", RecipeMatcher.normalizeName("  EGGS "));
        assertEquals("berry", RecipeMatcher.normalizeName("berries"));
        assertEquals(RecipeMatcher.normalizeName("cookie"), RecipeMatcher.normalizeName("cookies"));
        assertEquals("peanut butter", RecipeMatcher.normalizeName("Peanut Butter"));
    }

    @Test
    public void unitConversionKilogramsSatisfyGrams() {
        Recipe pasta = recipe("Pasta", need("pasta", 100, "g"));
        assertTrue(RecipeMatcher.canMake(pasta, Arrays.asList(pantry("Pasta", 0.5, "kg"))));
        assertFalse(RecipeMatcher.canMake(pasta, Arrays.asList(pantry("Pasta", 0.05, "kg"))));
    }

    @Test
    public void unitConversionVolume() {
        Recipe milk = recipe("Milk drink", need("milk", 200, "ml"));
        assertTrue(RecipeMatcher.canMake(milk, Arrays.asList(pantry("Milk", 1, "l"))));
        assertTrue(RecipeMatcher.canMake(milk, Arrays.asList(pantry("Milk", 1, "cup"))));  // 240 ml
        assertFalse(RecipeMatcher.canMake(milk, Arrays.asList(pantry("Milk", 3, "tbsp")))); // 45 ml
    }

    @Test
    public void massVolumeAndCountsAreNeverInterchangeable() {
        Recipe flour = recipe("Flour thing", need("flour", 50, "g"));
        assertFalse(RecipeMatcher.canMake(flour, Arrays.asList(pantry("Flour", 500, "ml"))));
        assertFalse(RecipeMatcher.canMake(flour, Arrays.asList(pantry("Flour", 5, "piece"))));

        Recipe toast = recipe("Toast", need("bread", 2, "slices"));
        assertFalse(RecipeMatcher.canMake(toast, Arrays.asList(pantry("Bread", 10, "piece"))));
        assertTrue(RecipeMatcher.canMake(toast, Arrays.asList(pantry("Bread", 4, "slice"))));
    }

    @Test
    public void unknownUnitFailsConservatively() {
        Recipe r = recipe("Mystery", need("rice", 100, "g"));
        assertFalse(RecipeMatcher.canMake(r, Arrays.asList(pantry("Rice", 5, "handfuls"))));
        Recipe weird = recipe("Weird", need("rice", 1, "bucket"));
        assertFalse(RecipeMatcher.canMake(weird, Arrays.asList(pantry("Rice", 1, "bucket"))));
    }

    @Test
    public void severalPantryRowsOfTheSameIngredientAreAddedUp() {
        Recipe rice = recipe("Rice", need("rice", 300, "g"));
        List<Ingredient> p = Arrays.asList(pantry("Rice", 200, "g"), pantry("rice", 0.2, "kg"));
        assertTrue(RecipeMatcher.canMake(rice, p));
    }

    @Test
    public void emptyPantryOrEmptyRecipeNeverMatches() {
        assertFalse(RecipeMatcher.canMake(omelette, new ArrayList<Ingredient>()));
        assertFalse(RecipeMatcher.canMake(recipe("Nothing"), Arrays.asList(pantry("Egg", 5, "piece"))));
        assertTrue(RecipeMatcher.findMakeableRecipes(
                Arrays.asList(omelette), new ArrayList<Ingredient>()).isEmpty());
    }

    @Test
    public void recipeDisappearsWhenIngredientIsRemoved() {
        List<Ingredient> full = new ArrayList<>(Arrays.asList(
                pantry("egg", 2, "piece"), pantry("cheese", 30, "g"), pantry("oil", 5, "ml")));
        assertTrue(RecipeMatcher.canMake(omelette, full));
        full.remove(1); // remove the cheese
        assertFalse(RecipeMatcher.canMake(omelette, full));
    }

    // ------------------------------------------------------------------
    // Realistic pantries: these are the cases that used to show no recipes.
    // ------------------------------------------------------------------

    @Test
    public void eggsInGramsSatisfyEggsInPieces() {
        // 1 egg is about 50 g, so 150 g of eggs covers "2 pieces".
        List<Ingredient> p = Arrays.asList(
                pantry("Egg", 150, "g"), pantry("Cheese", 100, "g"), pantry("Oil", 500, "ml"));
        assertTrue(RecipeMatcher.canMake(omelette, p));
        // ...but 50 g (one egg) is not enough.
        List<Ingredient> tooLittle = Arrays.asList(
                pantry("Egg", 50, "g"), pantry("Cheese", 100, "g"), pantry("Oil", 500, "ml"));
        assertFalse(RecipeMatcher.canMake(omelette, tooLittle));
    }

    @Test
    public void tomatoesInKilogramsSatisfyPieces() {
        Recipe soup = recipe("Soup", need("tomato", 2, "pieces"));
        assertTrue(RecipeMatcher.canMake(soup, Arrays.asList(pantry("Tomatoes", 1, "kg"))));
    }

    @Test
    public void descriptiveWordsAreIgnored() {
        assertEquals("oil", RecipeMatcher.normalizeName("Olive Oil"));
        assertEquals("milk", RecipeMatcher.normalizeName("Whole milk"));
        assertEquals("onion", RecipeMatcher.normalizeName("Red onions"));
        // A name made only of descriptors is kept as it is.
        assertEquals("white", RecipeMatcher.normalizeName("white"));

        List<Ingredient> p = Arrays.asList(
                pantry("Eggs", 6, "piece"), pantry("Cheddar Cheese", 100, "g"),
                pantry("Olive oil", 1, "l"));
        // "Cheddar Cheese" is not "cheese", so the omelette stays excluded.
        assertFalse(RecipeMatcher.canMake(omelette, p));

        List<Ingredient> p2 = Arrays.asList(
                pantry("Eggs", 6, "piece"), pantry("Cheese", 100, "g"),
                pantry("Olive oil", 1, "l"));
        assertTrue(RecipeMatcher.canMake(omelette, p2));
    }

    @Test
    public void peanutButterIsNotButter() {
        Recipe garlicBread = recipe("Garlic bread",
                need("bread", 2, "slices"), need("butter", 15, "g"), need("garlic", 1, "clove"));
        List<Ingredient> p = Arrays.asList(
                pantry("Bread", 4, "slice"), pantry("Peanut Butter", 200, "g"),
                pantry("Garlic", 1, "piece"));
        assertFalse(RecipeMatcher.canMake(garlicBread, p));
    }

    @Test
    public void waterIsAlwaysAvailable() {
        Recipe soup = recipe("Carrot soup",
                need("carrot", 150, "g"), need("onion", 1, "piece"), need("water", 300, "ml"));
        List<Ingredient> p = Arrays.asList(pantry("Carrots", 500, "g"), pantry("Onion", 2, "piece"));
        assertTrue(RecipeMatcher.canMake(soup, p));
    }

    @Test
    public void garlicBulbsAndClovesCompareByWeight() {
        Recipe r = recipe("Garlic thing", need("garlic", 2, "cloves"));
        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(pantry("Garlic", 1, "piece"))));
        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(pantry("Garlic", 3, "clove"))));
        assertFalse(RecipeMatcher.canMake(r, Arrays.asList(pantry("Garlic", 1, "clove"))));
    }

    @Test
    public void unitIsSuggestedFromTheName() {
        assertEquals("piece", IngredientKnowledge.suggestUnit("Eggs"));
        assertEquals("piece", IngredientKnowledge.suggestUnit("tomato"));
        assertEquals("clove", IngredientKnowledge.suggestUnit("Garlic"));
        assertEquals("slice", IngredientKnowledge.suggestUnit("Bread"));
        assertEquals("ml", IngredientKnowledge.suggestUnit("Olive Oil"));
        assertEquals("ml", IngredientKnowledge.suggestUnit("Milk"));
        assertEquals("g", IngredientKnowledge.suggestUnit("Flour"));
        assertEquals("g", IngredientKnowledge.suggestUnit(""));
    }
}
