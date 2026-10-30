package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Add/Edit Ingredient screen.
 * No extra in the Intent = "Add" mode. With EXTRA_INGREDIENT_ID = "Edit" mode:
 * the row is loaded from SQLite, and a Delete button is shown.
 *
 * To make sure recipes really are suggested after an ingredient is added:
 *  - the name field suggests the names the recipes use,
 *  - the unit is pre-selected from the name (egg -> piece, milk -> ml, ...),
 *  - after saving, a message says how many recipes can be cooked now.
 */
public class AddEditIngredientActivity extends BaseActivity {

    public static final String EXTRA_INGREDIENT_ID = "ingredient_id";

    private static final int NO_ID = -1;
    private static final double MAX_QUANTITY = 100000;

    private TextInputLayout tilName;
    private TextInputLayout tilQuantity;
    private TextInputLayout tilExpiry;
    private AutoCompleteTextView etName;
    private AutoCompleteTextView actUnit;
    private EditText etQuantity;
    private EditText etExpiry;
    private DatabaseHelper databaseHelper;
    private int ingredientId = NO_ID;

    // Once the user picks a unit themselves, we stop changing it automatically.
    private boolean unitChosenByUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        enableUpButton();

        databaseHelper = DatabaseHelper.getInstance(this);

        tilName = findViewById(R.id.tilIngredientName);
        tilQuantity = findViewById(R.id.tilIngredientQuantity);
        tilExpiry = findViewById(R.id.tilIngredientExpiry);
        etName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etIngredientQuantity);
        etExpiry = findViewById(R.id.etIngredientExpiry);
        actUnit = findViewById(R.id.actIngredientUnit);
        Button btnPickDate = findViewById(R.id.btnPickExpiry);
        Button btnClearDate = findViewById(R.id.btnClearExpiry);
        Button btnSave = findViewById(R.id.btnSaveIngredient);
        Button btnCancel = findViewById(R.id.btnCancelIngredient);
        Button btnDelete = findViewById(R.id.btnDeleteIngredient);

        // The unit is chosen from a list so it always matches what the matcher understands.
        List<String> units = new ArrayList<>();
        for (String unit : UnitConverter.SUPPORTED_UNITS) {
            units.add(unit);
        }

        ingredientId = getIntent().getIntExtra(EXTRA_INGREDIENT_ID, NO_ID);
        Ingredient existing = null;

        if (ingredientId != NO_ID) {
            existing = databaseHelper.getIngredientById(ingredientId);
            if (existing == null) {
                Toast.makeText(this, R.string.ingredient_not_found,
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            // Keep an old, unusual unit selectable instead of silently changing it.
            if (!units.contains(existing.getUnit())) {
                units.add(existing.getUnit());
            }
        }

        actUnit.setAdapter(new ArrayAdapter<>(
                this, R.layout.item_dropdown, units));
        actUnit.setOnItemClickListener((parent, view, position, id) ->
                unitChosenByUser = true);

        // Suggest the ingredient names that the recipes use.
        etName.setAdapter(new ArrayAdapter<>(
                this, R.layout.item_dropdown,
                capitalised(databaseHelper.getRecipeIngredientNames())));
        etName.setThreshold(1);

        if (existing == null) {
            setTitle(R.string.title_add_ingredient);
            actUnit.setText(units.get(0), false);
        } else {
            setTitle(R.string.title_edit_ingredient);
            unitChosenByUser = true;
            etName.setText(existing.getName(), false);
            etQuantity.setText(Ingredient.formatQuantity(existing.getQuantity()));
            actUnit.setText(existing.getUnit(), false);
            if (existing.getExpiryDate() != null) {
                etExpiry.setText(existing.getExpiryDate());
            }
            btnDelete.setVisibility(View.VISIBLE);
        }

        // Add mode: pick a sensible unit while the name is being typed.
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (ingredientId == NO_ID && !unitChosenByUser) {
                    actUnit.setText(IngredientKnowledge.suggestUnit(s.toString()), false);
                }
            }
        });

        etExpiry.setOnClickListener(v -> showDatePicker());
        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> {
            etExpiry.setText("");
            tilExpiry.setError(null);
        });

        btnSave.setOnClickListener(v -> saveIngredient());
        btnCancel.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    // "egg" -> "Egg", "peanut butter" -> "Peanut Butter" (for the suggestion list).
    private List<String> capitalised(List<String> names) {
        List<String> result = new ArrayList<>();
        for (String name : names) {
            StringBuilder text = new StringBuilder();
            for (String word : name.split(" ")) {
                if (word.isEmpty()) {
                    continue;
                }
                if (text.length() > 0) {
                    text.append(' ');
                }
                text.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
            }
            result.add(text.toString());
        }
        return result;
    }

    // Let the user pick the expiry date instead of typing it.
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        Date current = ExpiryHelper.parse(etExpiry.getText().toString());
        if (current != null) {
            calendar.setTime(current);
        }

        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    etExpiry.setText(String.format(
                            Locale.US, "%04d-%02d-%02d", year, month + 1, day));
                    tilExpiry.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Validate the form, then insert (Add mode) or update (Edit mode).
    private void saveIngredient() {
        String name = etName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String unit = actUnit.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();

        // --- validation: each problem is shown under the field it belongs to ---
        tilName.setError(null);
        tilQuantity.setError(null);
        tilExpiry.setError(null);

        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_name_required));
            etName.requestFocus();
            return;
        }
        if (!name.matches(".*[A-Za-z].*")) {
            tilName.setError(getString(R.string.error_name_letters));
            etName.requestFocus();
            return;
        }
        if (name.length() > 50) {
            tilName.setError(getString(R.string.error_name_long));
            etName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            tilQuantity.setError(getString(R.string.error_quantity_required));
            etQuantity.requestFocus();
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            tilQuantity.setError(getString(R.string.error_quantity_invalid));
            etQuantity.requestFocus();
            return;
        }
        if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
            tilQuantity.setError(getString(R.string.error_quantity_positive));
            etQuantity.requestFocus();
            return;
        }
        if (quantity > MAX_QUANTITY) {
            tilQuantity.setError(getString(R.string.error_quantity_large));
            etQuantity.requestFocus();
            return;
        }

        if (unit.isEmpty()) {
            unit = UnitConverter.SUPPORTED_UNITS[0];
        }

        if (!expiry.isEmpty() && !ExpiryHelper.isValidDate(expiry)) {
            tilExpiry.setError(getString(R.string.error_expiry_invalid));
            return;
        }

        // --- save to SQLite ---
        boolean saved;
        if (ingredientId == NO_ID) {
            saved = databaseHelper.insertIngredient(name, quantity, unit, expiry) != -1;
        } else {
            saved = databaseHelper.updateIngredient(
                    ingredientId, name, quantity, unit, expiry) > 0;
        }

        if (saved) {
            Toast.makeText(this, savedMessage(), Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish(); // the pantry list reloads itself in onResume()
        } else {
            Toast.makeText(this, R.string.ingredient_save_failed,
                    Toast.LENGTH_LONG).show();
        }
    }

    // After saving, tell the user how many recipes the pantry can make right now.
    private String savedMessage() {
        try {
            int ready = RecipeMatcher.findMakeableRecipes(
                    databaseHelper.getAllRecipes(),
                    databaseHelper.getAllIngredients()).size();
            if (ready > 0) {
                return getResources().getQuantityString(
                        R.plurals.saved_with_recipes, ready, ready);
            }
        } catch (Exception ignored) {
            // fall through to the plain message
        }
        return getString(R.string.ingredient_saved);
    }

    private void confirmDelete() {
        if (ingredientId == NO_ID) {
            finish();
            return;
        }
        if (SettingsActivity.isConfirmDeleteEnabled(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_title)
                    .setMessage(getString(R.string.delete_message,
                            etName.getText().toString().trim()))
                    .setPositiveButton(R.string.delete, (dialog, which) -> deleteIngredient())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            deleteIngredient();
        }
    }

    private void deleteIngredient() {
        int rows = databaseHelper.deleteIngredient(ingredientId);
        if (rows > 0) {
            Toast.makeText(this, R.string.ingredient_deleted, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.ingredient_not_deleted,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
// Unit dropdown: lets the user pick g, kg, ml, L, etc. instead of typing a unit.
