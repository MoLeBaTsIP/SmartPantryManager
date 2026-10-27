package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

/**
 * Pantry List screen (launcher screen).
 * Reads every ingredient from SQLite and shows it in a RecyclerView.
 * The list is reloaded in onResume(), so changes made on the Add/Edit screen
 * appear as soon as the user comes back.
 */
public class MainActivity extends BaseActivity
        implements IngredientAdapter.Listener {

    private static final String TAG = "SmartPantry";

    private RecyclerView recyclerViewIngredients;
    private TextView tvPantryEmpty;
    private TextView tvExpiryBanner;
    private TextView tvRecipesReady;
    private TextView tvPantryCount;
    private IngredientAdapter adapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewIngredients = findViewById(R.id.recyclerViewIngredients);
        tvPantryEmpty = findViewById(R.id.tvPantryEmpty);
        tvExpiryBanner = findViewById(R.id.tvExpiryBanner);
        tvRecipesReady = findViewById(R.id.tvRecipesReady);
        tvPantryCount = findViewById(R.id.tvPantryCount);
        View cardRecipesReady = findViewById(R.id.cardRecipesReady);
        Button btnAdd = findViewById(R.id.btnAddIngredient);
        Button btnRecipes = findViewById(R.id.btnSuggestedRecipes);
        Button btnSettings = findViewById(R.id.btnSettings);

        // Opening the database creates the tables and seeds the recipes (first run only).
        databaseHelper = DatabaseHelper.getInstance(this);

        adapter = new IngredientAdapter(this);
        recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewIngredients.setAdapter(adapter);

        // Explicit Intents to the other screens.
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        btnRecipes.setOnClickListener(v ->
                startActivity(new Intent(this, SuggestedRecipesActivity.class)));

        // The "recipes ready" card is a shortcut to the same screen.
        cardRecipesReady.setOnClickListener(v ->
                startActivity(new Intent(this, SuggestedRecipesActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    // Runs every time the screen becomes visible, so the list is always up to date.
    @Override
    protected void onResume() {
        super.onResume();
        loadIngredients();
    }

    private void loadIngredients() {
        try {
            ArrayList<Ingredient> ingredients = databaseHelper.getAllIngredients();
            boolean highlight = SettingsActivity.isExpiryAlertsEnabled(this);

            adapter.setData(ingredients, highlight);

            // Empty-state message instead of a blank screen.
            boolean empty = ingredients.isEmpty();
            tvPantryEmpty.setText(R.string.pantry_empty);
            tvPantryEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            recyclerViewIngredients.setVisibility(empty ? View.GONE : View.VISIBLE);

            // "In your pantry · 4 items" above the list (hidden while the pantry is empty).
            tvPantryCount.setText(getResources().getQuantityString(
                    R.plurals.pantry_count, ingredients.size(), ingredients.size()));
            tvPantryCount.setVisibility(empty ? View.GONE : View.VISIBLE);

            updateExpiryBanner(ingredients, highlight);
            updateRecipeCard(ingredients);
            Log.d(TAG, "Pantry ingredients loaded: " + ingredients.size());

        } catch (Exception e) {
            Log.e(TAG, "Failed to load pantry ingredients", e);
            tvPantryEmpty.setText(R.string.pantry_load_error);
            tvPantryEmpty.setVisibility(View.VISIBLE);
            recyclerViewIngredients.setVisibility(View.GONE);
            tvPantryCount.setVisibility(View.GONE);
        }
    }

    // Tell the user straight away how many recipes the current pantry can make.
    private void updateRecipeCard(ArrayList<Ingredient> ingredients) {
        int ready = RecipeMatcher.findMakeableRecipes(
                databaseHelper.getAllRecipes(), ingredients).size();
        if (ready > 0) {
            tvRecipesReady.setText(getResources().getQuantityString(
                    R.plurals.recipes_ready, ready, ready));
        } else {
            tvRecipesReady.setText(R.string.recipes_ready_none);
        }
    }

    // Warn about items that are expired or expire within a few days.
    private void updateExpiryBanner(ArrayList<Ingredient> ingredients, boolean enabled) {
        int count = 0;
        Date today = new Date();
        for (Ingredient item : ingredients) {
            if (ExpiryHelper.isExpired(item.getExpiryDate(), today)
                    || ExpiryHelper.isExpiringSoon(item.getExpiryDate(), today)) {
                count++;
            }
        }
        if (enabled && count > 0) {
            tvExpiryBanner.setText(getResources().getQuantityString(
                    R.plurals.expiry_banner, count, count));
            tvExpiryBanner.setVisibility(View.VISIBLE);
        } else {
            tvExpiryBanner.setVisibility(View.GONE);
        }
    }

    // ---- IngredientAdapter.Listener ----

    @Override
    public void onEdit(Ingredient ingredient) {
        // Pass only the record id; the Edit screen loads the row from SQLite.
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID,
                ingredient.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Ingredient ingredient) {
        if (SettingsActivity.isConfirmDeleteEnabled(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_title)
                    .setMessage(getString(R.string.delete_message, ingredient.getName()))
                    .setPositiveButton(R.string.delete, (dialog, which) ->
                            deleteIngredient(ingredient))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            deleteIngredient(ingredient);
        }
    }

    private void deleteIngredient(Ingredient ingredient) {
        int rows = databaseHelper.deleteIngredient(ingredient.getId());
        Toast.makeText(this,
                rows > 0 ? R.string.ingredient_deleted : R.string.ingredient_not_deleted,
                Toast.LENGTH_SHORT).show();
        loadIngredients();
    }
}
