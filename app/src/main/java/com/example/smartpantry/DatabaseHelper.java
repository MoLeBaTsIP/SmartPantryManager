

package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * DatabaseHelper manages the Smart Pantry Manager database.
 *
 * Tables:
 * 1. ingredients
 * 2. recipes
 * 3. recipe_ingredients
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS =
            "recipe_ingredients";

    private static DatabaseHelper instance;

    // Return the single shared database helper.
    public static synchronized DatabaseHelper getInstance(
            Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(
                    context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        seedRecipes(db);
    }

    // Create the three database tables.
    private void createTables(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE ingredients (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL COLLATE NOCASE, " +
                        "quantity REAL NOT NULL CHECK(quantity >= 0), " +
                        "unit TEXT NOT NULL, " +
                        "expiry_date TEXT)"
        );

        db.execSQL(
                "CREATE TABLE recipes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "instructions TEXT NOT NULL)"
        );

        db.execSQL(
                "CREATE TABLE recipe_ingredients (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "recipe_id INTEGER NOT NULL, " +
                        "ingredient_name TEXT NOT NULL COLLATE NOCASE, " +
                        "quantity REAL NOT NULL CHECK(quantity > 0), " +
                        "unit TEXT NOT NULL, " +
                        "FOREIGN KEY(recipe_id) REFERENCES recipes(id) " +
                        "ON DELETE CASCADE)"
        );
    }

    // Enable foreign-key enforcement.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Insert a recipe and its required ingredients.
    private void insertRecipe(
            SQLiteDatabase db,
            String name,
            String instructions,
            String[][] requiredIngredients) {

        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("instructions", instructions);

        long recipeId = db.insertOrThrow(
                TABLE_RECIPES, null, recipeValues);

        for (String[] item : requiredIngredients) {
            ContentValues ingredientValues = new ContentValues();

            ingredientValues.put("recipe_id", recipeId);
            ingredientValues.put("ingredient_name", item[0]);
            ingredientValues.put(
                    "quantity", Double.parseDouble(item[1]));
            ingredientValues.put("unit", item[2]);

            db.insertOrThrow(
                    TABLE_RECIPE_INGREDIENTS,
                    null,
                    ingredientValues);
        }
    }

    // Add example recipes when the database is first created.
    private void seedRecipes(SQLiteDatabase db) {
        db.beginTransaction();

        try {
            insertRecipe(db, "Tomato Egg Scramble",
                    "1. Chop the tomato. 2. Beat the eggs. " +
                            "3. Cook the tomato in oil, add eggs and stir until fully cooked.",
                    new String[][]{
                            {"egg", "2", "pieces"},
                            {"tomato", "1", "piece"},
                            {"oil", "5", "ml"}
                    });

            insertRecipe(db, "Simple Tomato Pasta",
                    "1. Boil pasta until tender. 2. Cook chopped tomato in oil. " +
                            "3. Mix together and serve.",
                    new String[][]{
                            {"pasta", "100", "g"},
                            {"tomato", "2", "pieces"},
                            {"oil", "5", "ml"}
                    });

            insertRecipe(db, "Potato and Onion Hash",
                    "1. Dice the potato and onion. 2. Boil the potato until nearly tender. " +
                            "3. Fry both in oil.",
                    new String[][]{
                            {"potato", "200", "g"},
                            {"onion", "1", "piece"},
                            {"oil", "10", "ml"}
                    });

            insertRecipe(db, "Banana Oatmeal",
                    "1. Cook oats in milk. 2. Slice the banana. " +
                            "3. Add the banana before serving.",
                    new String[][]{
                            {"oats", "50", "g"},
                            {"milk", "200", "ml"},
                            {"banana", "1", "piece"}
                    });

            insertRecipe(db, "Cheese Omelette",
                    "1. Beat the eggs. 2. Pour into a heated pan. " +
                            "3. Add cheese and fold when the eggs set.",
                    new String[][]{
                            {"egg", "2", "pieces"},
                            {"cheese", "30", "g"},
                            {"oil", "5", "ml"}
                    });

            insertRecipe(db, "Garlic Fried Rice",
                    "1. Cook the rice. 2. Fry chopped garlic in oil. " +
                            "3. Add rice and stir-fry thoroughly.",
                    new String[][]{
                            {"rice", "100", "g"},
                            {"garlic", "2", "cloves"},
                            {"oil", "10", "ml"}
                    });

            insertRecipe(db, "Peanut Butter Toast",
                    "1. Toast the bread. 2. Spread peanut butter over each slice.",
                    new String[][]{
                            {"bread", "2", "slices"},
                            {"peanut butter", "30", "g"}
                    });

            insertRecipe(db, "Apple Oat Porridge",
                    "1. Cook oats in milk. 2. Dice the apple. " +
                            "3. Add the apple and serve.",
                    new String[][]{
                            {"oats", "50", "g"},
                            {"milk", "200", "ml"},
                            {"apple", "1", "piece"}
                    });

            insertRecipe(db, "Potato Egg Salad",
                    "1. Boil potato and egg. 2. Allow to cool. " +
                            "3. Chop and combine with mayonnaise.",
                    new String[][]{
                            {"potato", "200", "g"},
                            {"egg", "1", "piece"},
                            {"mayonnaise", "20", "g"}
                    });

            insertRecipe(db, "Tomato Cheese Sandwich",
                    "1. Slice the tomato. 2. Place tomato and cheese between bread slices.",
                    new String[][]{
                            {"bread", "2", "slices"},
                            {"tomato", "1", "piece"},
                            {"cheese", "30", "g"}
                    });

            insertRecipe(db, "Garlic Potato Wedges",
                    "1. Cut potatoes into wedges. 2. Mix with oil and chopped garlic. " +
                            "3. Bake until tender.",
                    new String[][]{
                            {"potato", "250", "g"},
                            {"garlic", "2", "cloves"},
                            {"oil", "10", "ml"}
                    });

            insertRecipe(db, "Banana Pancakes",
                    "1. Mash the banana. 2. Mix with egg and flour. " +
                            "3. Cook small pancakes in a lightly oiled pan.",
                    new String[][]{
                            {"banana", "1", "piece"},
                            {"egg", "1", "piece"},
                            {"flour", "50", "g"},
                            {"oil", "5", "ml"}
                    });

            insertRecipe(db, "Cheesy Pasta",
                    "1. Boil pasta until tender. 2. Add milk and cheese over low heat. " +
                            "3. Stir until creamy.",
                    new String[][]{
                            {"pasta", "100", "g"},
                            {"milk", "100", "ml"},
                            {"cheese", "40", "g"}
                    });

            insertRecipe(db, "Carrot Soup",
                    "1. Chop carrot and onion. 2. Boil in water until soft. " +
                            "3. Blend carefully until smooth.",
                    new String[][]{
                            {"carrot", "150", "g"},
                            {"onion", "1", "piece"},
                            {"water", "300", "ml"}
                    });

            insertRecipe(db, "Egg Fried Rice",
                    "1. Cook the rice. 2. Scramble the egg in oil. " +
                            "3. Add rice and stir until heated through.",
                    new String[][]{
                            {"rice", "100", "g"},
                            {"egg", "1", "piece"},
                            {"oil", "10", "ml"}
                    });

            insertRecipe(db, "Tomato Onion Soup",
                    "1. Chop tomato and onion. 2. Simmer in water until soft. " +
                            "3. Stir and serve warm.",
                    new String[][]{
                            {"tomato", "2", "pieces"},
                            {"onion", "1", "piece"},
                            {"water", "300", "ml"}
                    });

            insertRecipe(db, "Apple Peanut Butter Snack",
                    "1. Wash and slice the apple. 2. Serve with peanut butter.",
                    new String[][]{
                            {"apple", "1", "piece"},
                            {"peanut butter", "20", "g"}
                    });

            insertRecipe(db, "Carrot Potato Mash",
                    "1. Boil chopped carrot and potato until soft. " +
                            "2. Drain and mash together.",
                    new String[][]{
                            {"carrot", "100", "g"},
                            {"potato", "200", "g"}
                    });

            insertRecipe(db, "Garlic Butter Bread",
                    "1. Mix softened butter with chopped garlic. " +
                            "2. Spread over bread and toast until golden.",
                    new String[][]{
                            {"bread", "2", "slices"},
                            {"butter", "15", "g"},
                            {"garlic", "1", "clove"}
                    });

            insertRecipe(db, "Milk Oat Pancakes",
                    "1. Mix flour, oats, egg and milk. " +
                            "2. Cook small portions in a lightly oiled pan.",
                    new String[][]{
                            {"flour", "50", "g"},
                            {"oats", "30", "g"},
                            {"egg", "1", "piece"},
                            {"milk", "100", "ml"},
                            {"oil", "5", "ml"}
                    });

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    /**
     * Retrieves all pantry ingredients from SQLite.
     * The results are sorted alphabetically by name.
     */
    public ArrayList<Ingredient> getAllIngredients() {
        ArrayList<Ingredient> ingredientList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT id, name, quantity, unit, expiry_date "
                + "FROM " + TABLE_INGREDIENTS + " ORDER BY name ASC";

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id"));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name"));

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("quantity"));

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow("unit"));

                String expiryDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("expiry_date"));

                Ingredient ingredient = new Ingredient(
                        id, name, quantity, unit, expiryDate);

                ingredientList.add(ingredient);
            }
        }

        return ingredientList;
    }


    // ------------------------------------------------------------------
    // CRUD for pantry ingredients
    // ------------------------------------------------------------------

    // Build the column values shared by insert and update.
    private ContentValues buildIngredientValues(
            String name, double quantity, String unit, String expiryDate) {

        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit.trim());

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            values.putNull("expiry_date"); // expiry is optional
        } else {
            values.put("expiry_date", expiryDate.trim());
        }
        return values;
    }

    /** CREATE: adds a pantry ingredient. Returns the new row id, or -1 on failure. */
    public long insertIngredient(
            String name, double quantity, String unit, String expiryDate) {

        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_INGREDIENTS, null,
                buildIngredientValues(name, quantity, unit, expiryDate));
    }

    /** UPDATE: changes an existing ingredient. Returns the number of rows changed. */
    public int updateIngredient(
            int id, String name, double quantity, String unit, String expiryDate) {

        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_INGREDIENTS,
                buildIngredientValues(name, quantity, unit, expiryDate),
                "id = ?", new String[]{String.valueOf(id)});
    }

    /** DELETE: removes an ingredient. Returns the number of rows deleted. */
    public int deleteIngredient(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_INGREDIENTS,
                "id = ?", new String[]{String.valueOf(id)});
    }

    /** READ (one): returns the ingredient with this id, or null if it does not exist. */
    public Ingredient getIngredientById(int id) {
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT id, name, quantity, unit, expiry_date FROM "
                        + TABLE_INGREDIENTS + " WHERE id = ?",
                new String[]{String.valueOf(id)})) {

            if (cursor.moveToFirst()) {
                return new Ingredient(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getString(4));
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Recipes
    // ------------------------------------------------------------------

    /** Returns every recipe (sorted by name) with its required ingredients loaded. */
    public ArrayList<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Recipe> recipes = new ArrayList<>();
        Map<Integer, Recipe> recipesById = new HashMap<>();

        try (Cursor cursor = db.rawQuery(
                "SELECT id, name, instructions FROM " + TABLE_RECIPES
                        + " ORDER BY name ASC", null)) {

            while (cursor.moveToNext()) {
                Recipe recipe = new Recipe(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2));
                recipes.add(recipe);
                recipesById.put(recipe.getId(), recipe);
            }
        }

        try (Cursor cursor = db.rawQuery(
                "SELECT id, recipe_id, ingredient_name, quantity, unit FROM "
                        + TABLE_RECIPE_INGREDIENTS
                        + " ORDER BY recipe_id, id", null)) {

            while (cursor.moveToNext()) {
                Recipe recipe = recipesById.get(cursor.getInt(1));
                if (recipe != null) {
                    recipe.addIngredient(new RecipeIngredient(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getString(2),
                            cursor.getDouble(3),
                            cursor.getString(4)));
                }
            }
        }
        return recipes;
    }

    /**
     * Returns the distinct ingredient names used by the recipes (sorted, water left out).
     * The Add Ingredient screen offers them as suggestions so that what the user types
     * lines up with the recipes.
     */
    public ArrayList<String> getRecipeIngredientNames() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> names = new ArrayList<>();

        try (Cursor cursor = db.rawQuery(
                "SELECT DISTINCT ingredient_name FROM " + TABLE_RECIPE_INGREDIENTS
                        + " ORDER BY ingredient_name COLLATE NOCASE ASC", null)) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                if (name != null && !IngredientKnowledge.isStaple(
                        RecipeMatcher.normalizeName(name))) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    /** Returns one recipe with its ingredients, or null if the id is unknown. */
    public Recipe getRecipeById(int recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        Recipe recipe = null;

        try (Cursor cursor = db.rawQuery(
                "SELECT id, name, instructions FROM " + TABLE_RECIPES
                        + " WHERE id = ?",
                new String[]{String.valueOf(recipeId)})) {

            if (cursor.moveToFirst()) {
                recipe = new Recipe(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2));
            }
        }
        if (recipe == null) {
            return null;
        }

        try (Cursor cursor = db.rawQuery(
                "SELECT id, recipe_id, ingredient_name, quantity, unit FROM "
                        + TABLE_RECIPE_INGREDIENTS
                        + " WHERE recipe_id = ? ORDER BY id",
                new String[]{String.valueOf(recipeId)})) {

            while (cursor.moveToNext()) {
                recipe.addIngredient(new RecipeIngredient(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getString(4)));
            }
        }
        return recipe;
    }

    /**
     * Development-only upgrade strategy.
     * WARNING: This deletes existing data when the database version changes.
     */
    @Override
    public void onUpgrade(
            SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);

        onCreate(db);
    }
}
