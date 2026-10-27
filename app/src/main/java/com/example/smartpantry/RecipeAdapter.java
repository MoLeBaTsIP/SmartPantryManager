package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Custom RecyclerView adapter for the Suggested Recipes list. */
public class RecipeAdapter
        extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.tvName.setText(recipe.getName());

        // One-line summary of the ingredients, e.g. "egg, tomato, oil".
        StringBuilder summary = new StringBuilder();
        for (RecipeIngredient item : recipe.getIngredients()) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(item.getName());
        }
        holder.tvIngredients.setText(holder.itemView.getContext()
                .getString(R.string.recipe_uses_format, summary.toString()));

        View.OnClickListener open = v -> listener.onRecipeClick(recipe);
        holder.btnView.setOnClickListener(open);
        holder.itemView.setOnClickListener(open);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvIngredients;
        final Button btnView;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvIngredients = itemView.findViewById(R.id.tvRecipeIngredients);
            btnView = itemView.findViewById(R.id.btnViewRecipe);
        }
    }
}
