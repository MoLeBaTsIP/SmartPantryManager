package com.example.smartpantry;

import java.util.ArrayList;
import java.util.List;

/** A recipe with its preparation steps and the ingredients it requires. */
public class Recipe {

    private final int id;
    private final String name;
    private final String instructions;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(int id, String name, String instructions) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.instructions = instructions == null ? "" : instructions;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public String getInstructions() { return instructions; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }
}
