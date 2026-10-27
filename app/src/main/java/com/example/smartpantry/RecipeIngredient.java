package com.example.smartpantry;

/** One ingredient a recipe needs (a row of the "recipe_ingredients" table). */
public class RecipeIngredient {

    private final int id;
    private final int recipeId;
    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(int id, int recipeId, String name,
                            double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public int getId() { return id; }

    public int getRecipeId() { return recipeId; }

    public String getName() { return name; }

    public double getQuantity() { return quantity; }

    public String getUnit() { return unit; }
}
