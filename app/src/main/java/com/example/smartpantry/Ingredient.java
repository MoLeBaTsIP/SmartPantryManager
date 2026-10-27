package com.example.smartpantry;

import java.util.Locale;

/** One ingredient stored in the user's pantry (a row of the "ingredients" table). */
public class Ingredient {

    private final int id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final String expiryDate; // "yyyy-MM-dd", or null/empty when not recorded

    public Ingredient(int id, String name, double quantity,
                      String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public double getQuantity() { return quantity; }

    public String getUnit() { return unit; }

    public String getExpiryDate() { return expiryDate; }

    /** Shows 2.0 as "2" and 0.50 as "0.5" so quantities look natural on screen. */
    public static String formatQuantity(double quantity) {
        if (quantity == Math.rint(quantity)) {
            return String.valueOf((long) quantity);
        }
        String text = String.format(Locale.US, "%.2f", quantity);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
