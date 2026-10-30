package com.example.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests that ingredients can be deleted from the pantry database. */
@RunWith(AndroidJUnit4.class)
public class PantryDeleteTest {

    private DatabaseHelper db;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = DatabaseHelper.getInstance(context);
    }

    @Test
    public void deleteIngredient_removesExactlyOneRow() {
        long id = db.insertIngredient("TestEggs", 12, "pcs", "2030-02-02");
        assertEquals(1, db.deleteIngredient((int) id));
    }

    @Test
    public void deleteIngredient_itemNoLongerFound() {
        long id = db.insertIngredient("TestButter", 250, "g", "2030-04-04");
        db.deleteIngredient((int) id);
        assertNull(db.getIngredientById((int) id));
    }

    @Test
    public void deleteIngredient_reducesPantrySize() {
        long id = db.insertIngredient("TestRice", 1, "kg", "2030-05-05");
        int before = db.getAllIngredients().size();
        db.deleteIngredient((int) id);
        assertEquals(before - 1, db.getAllIngredients().size());
    }

    @Test
    public void deleteIngredient_unknownIdDeletesNothing() {
        assertEquals(0, db.deleteIngredient(-999));
    }
}
