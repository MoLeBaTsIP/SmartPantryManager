package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Suggested Recipes screen.
 * Loads the pantry and all recipes from SQLite and shows ONLY the recipes
 * that RecipeMatcher says can be cooked right now (strict matching).
 */
public class SuggestedRecipesActivity extends BaseActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private RecyclerView recyclerViewRecipes;
    private TextView tvNoRecipes;
    private TextView tvRecipeSummary;
    private RecipeAdapter adapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle(R.string.title_suggested);
        enableUpButton();

        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);
        tvRecipeSummary = findViewById(R.id.tvRecipeSummary);
        Button back = findViewById(R.id.btnRecipesBack);

        databaseHelper = DatabaseHelper.getInstance(this);

        adapter = new RecipeAdapter(recipe -> {
            // Pass the recipe id to the detail screen with an explicit Intent.
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecipes.setAdapter(adapter);

        back.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        try {
            List<Recipe> allRecipes = databaseHelper.getAllRecipes();
            List<Ingredient> pantry = databaseHelper.getAllIngredients();

            // The strict rule: every ingredient present, in enough quantity.
            List<Recipe> makeable = RecipeMatcher.findMakeableRecipes(allRecipes, pantry);

            adapter.setData(makeable);
            tvNoRecipes.setText(R.string.recipes_none);

            if (makeable.isEmpty()) {
                // Helpful message instead of a blank screen.
                tvNoRecipes.setVisibility(View.VISIBLE);
                recyclerViewRecipes.setVisibility(View.GONE);
                tvRecipeSummary.setText(R.string.recipes_summary_none);
            } else {
                tvNoRecipes.setVisibility(View.GONE);
                recyclerViewRecipes.setVisibility(View.VISIBLE);
                tvRecipeSummary.setText(getString(R.string.recipes_summary_format,
                        makeable.size(), allRecipes.size()));
            }
        } catch (Exception e) {
            tvNoRecipes.setText(R.string.recipes_load_error);
            tvNoRecipes.setVisibility(View.VISIBLE);
            recyclerViewRecipes.setVisibility(View.GONE);
            Toast.makeText(this, R.string.recipes_load_error, Toast.LENGTH_SHORT).show();
        }
    }
}
