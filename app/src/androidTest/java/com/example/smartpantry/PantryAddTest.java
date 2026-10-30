package com.example.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests that ingredients can be added to the pantry database. */
@RunWith(AndroidJUnit4.class)
public class PantryAddTest {

    private DatabaseHelper db;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = DatabaseHelper.getInstance(context);
    }

    @Test
    public void addIngredient_returnsValidRowId() {
        long id = db.insertIngredient("TestFlour", 2.5, "kg", "2030-01-01");
        assertTrue("Insert should return a positive row id", id > 0);
        db.deleteIngredient((int) id); // clean up
    }

    @Test
    public void addIngredient_savesAllFields() {
        long id = db.insertIngredient("TestSugar", 500, "g", "2030-06-15");
        Ingredient saved = db.getIngredientById((int) id);

        assertNotNull(saved);
        assertEquals("TestSugar", saved.getName());
        assertEquals(500, saved.getQuantity(), 0.001);
        assertEquals("g", saved.getUnit());
        assertEquals("2030-06-15", saved.getExpiryDate());
        db.deleteIngredient((int) id);
    }

    @Test
    public void addIngredient_appearsInPantryList() {
        int before = db.getAllIngredients().size();
        long id = db.insertIngredient("TestMilk", 1, "L", "2030-03-03");
        int after = db.getAllIngredients().size();

        assertEquals(before + 1, after);
        db.deleteIngredient((int) id);
    }
}
