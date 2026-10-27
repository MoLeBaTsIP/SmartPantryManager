package com.example.smartpantry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Recipe Detail screen.
 * Receives only a recipe id through the Intent and loads the full recipe
 * (ingredients + method) from SQLite.
 */
public class RecipeDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        setTitle(R.string.title_recipe_detail);
        enableUpButton();

        TextView tvName = findViewById(R.id.tvRecipeDetailName);
        TextView tvStatus = findViewById(R.id.tvRecipeDetailStatus);
        TextView tvIngredients = findViewById(R.id.tvRecipeDetailIngredients);
        TextView tvMethod = findViewById(R.id.tvRecipeDetailMethod);
        Button back = findViewById(R.id.btnRecipeDetailBack);
        back.setOnClickListener(v -> finish());

        int recipeId = getIntent().getIntExtra(
                SuggestedRecipesActivity.EXTRA_RECIPE_ID, -1);

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        Recipe recipe = recipeId == -1 ? null : databaseHelper.getRecipeById(recipeId);

        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvName.setText(recipe.getName());

        // Ingredient list, one bullet per line.
        StringBuilder ingredientText = new StringBuilder();
        for (RecipeIngredient item : recipe.getIngredients()) {
            if (ingredientText.length() > 0) {
                ingredientText.append('\n');
            }
            ingredientText.append("\u2022 ")
                    .append(Ingredient.formatQuantity(item.getQuantity()))
                    .append(' ')
                    .append(item.getUnit())
                    .append(' ')
                    .append(item.getName());
        }
        tvIngredients.setText(ingredientText.toString());

        // Put each numbered step ("1. ... 2. ...") on its own line.
        tvMethod.setText(recipe.getInstructions().replaceAll("\\s(?=\\d+\\.\\s)", "\n"));

        // Tell the user whether the pantry still covers this recipe.
        List<Ingredient> pantry = databaseHelper.getAllIngredients();
        tvStatus.setText(RecipeMatcher.canMake(recipe, pantry)
                ? R.string.recipe_status_ready
                : R.string.recipe_status_missing);
    }
}
