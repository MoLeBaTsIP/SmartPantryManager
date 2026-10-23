package com.example.smartpantry;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SmartPantryDB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the shared database helper (one instance for the whole app).
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);

        try {
            // Open the database. It is left OPEN on purpose so that
            // Database Inspector can connect to it.
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Log.d(TAG, "========== DATABASE TEST ==========");
            Log.d(TAG, "Database opened successfully.");
            Log.d(TAG, "Database file: " + db.getPath());

            // TEST 1: Check whether all three tables exist.
            boolean ingredientsExists =
                    tableExists(db, "ingredients");

            boolean recipesExists =
                    tableExists(db, "recipes");

            boolean recipeIngredientsExists =
                    tableExists(db, "recipe_ingredients");

            Log.d(TAG, "ingredients table exists: "
                    + ingredientsExists);

            Log.d(TAG, "recipes table exists: "
                    + recipesExists);

            Log.d(TAG, "recipe_ingredients table exists: "
                    + recipeIngredientsExists);

            // TEST 2: Check foreign-key enforcement.
            int foreignKeyStatus = getForeignKeyStatus(db);

            Log.d(TAG, "Foreign-key enforcement status: "
                    + foreignKeyStatus);

            // Check whether recipe_ingredients references recipes.
            boolean recipeForeignKeyExists =
                    hasRecipeForeignKey(db);

            Log.d(TAG, "Recipe foreign-key constraint exists: "
                    + recipeForeignKeyExists);

            // TEST 3: Count recipes and recipe requirements.
            if (ingredientsExists
                    && recipesExists
                    && recipeIngredientsExists) {

                int recipeCount = getCount(db, "recipes");

                int requirementCount =
                        getCount(db, "recipe_ingredients");

                Log.d(TAG, "Recipes stored: " + recipeCount);

                Log.d(TAG, "Recipe requirements stored: "
                        + requirementCount);

                // Print every recipe name so the data can be seen in Logcat.
                logRecipeNames(db);

                // Report whether the expected recipe data exists.
                if (recipeCount == 20 && requirementCount > 0) {
                    Log.d(TAG, "RECIPE TEST PASSED.");
                } else {
                    Log.w(TAG, "RECIPE TEST NEEDS ATTENTION.");
                }

                // Report whether foreign keys are working as configured.
                if (foreignKeyStatus == 1
                        && recipeForeignKeyExists) {
                    Log.d(TAG, "FOREIGN-KEY CONFIGURATION PASSED.");
                } else {
                    Log.w(TAG, "FOREIGN-KEY CONFIGURATION FAILED.");
                }

                // Show the recipe count on the screen.
                Toast.makeText(
                        this,
                        "Database ready. Recipes: " + recipeCount,
                        Toast.LENGTH_LONG
                ).show();

            } else {
                Log.e(TAG, "TABLE TEST FAILED. One or more tables are missing.");

                Toast.makeText(
                        this,
                        "Database error: one or more tables are missing.",
                        Toast.LENGTH_LONG
                ).show();
            }

            Log.d(TAG, "========== END DATABASE TEST ==========");

        } catch (Exception e) {
            Log.e(TAG, "Database verification failed", e);

            Toast.makeText(
                    this,
                    "Database error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        }
        // NOTE: the helper is NOT closed here. Closing it right after
        // onCreate() made smart_pantry.db show as "(closed)" in
        // Database Inspector.
    }

    // Write every recipe name to Logcat (tag: SmartPantryDB).
    private void logRecipeNames(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery(
                "SELECT id, name FROM recipes ORDER BY id", null)) {

            while (cursor.moveToNext()) {
                Log.d(TAG, "RECIPE " + cursor.getInt(0)
                        + ": " + cursor.getString(1));
            }
        }
    }

    // Check whether a table exists in the database.
    private boolean tableExists(
            SQLiteDatabase db, String tableName) {

        try (Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master "
                        + "WHERE type = 'table' AND name = ?",
                new String[]{tableName})) {

            return cursor.moveToFirst();
        }
    }

    // Count records in a known database table.
    private int getCount(SQLiteDatabase db, String tableName) {
        int count = 0;

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + tableName, null)) {

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }

        return count;
    }

    // Check whether foreign-key enforcement is enabled.
    private int getForeignKeyStatus(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery(
                "PRAGMA foreign_keys", null)) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return -1;
    }

    // Check whether recipe_ingredients references recipes.
    private boolean hasRecipeForeignKey(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery(
                "PRAGMA foreign_key_list(recipe_ingredients)",
                null)) {

            int tableColumn = cursor.getColumnIndex("table");
            int fromColumn = cursor.getColumnIndex("from");
            int toColumn = cursor.getColumnIndex("to");

            while (cursor.moveToNext()) {
                if (tableColumn >= 0
                        && fromColumn >= 0
                        && toColumn >= 0
                        && "recipes".equalsIgnoreCase(
                        cursor.getString(tableColumn))
                        && "recipe_id".equalsIgnoreCase(
                        cursor.getString(fromColumn))
                        && "id".equalsIgnoreCase(
                        cursor.getString(toColumn))) {
                    return true;
                }
            }
        }

        return false;
    }
}


