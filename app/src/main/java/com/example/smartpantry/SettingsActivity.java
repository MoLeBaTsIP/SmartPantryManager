package com.example.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Settings screen. Two working preferences are saved with SharedPreferences:
 *  - expiry alerts: highlight items that expire soon on the pantry list
 *  - confirm before delete: ask "Are you sure?" before removing an ingredient
 */
public class SettingsActivity extends BaseActivity {

    private static final String PREFS_NAME = "smart_pantry_settings";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_CONFIRM_DELETE = "confirm_delete";

    // Read by other screens so they follow the user's choices.
    public static boolean isExpiryAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static boolean isConfirmDeleteEnabled(Context context) {
        return prefs(context).getBoolean(KEY_CONFIRM_DELETE, true);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.title_settings);
        enableUpButton();

        SwitchMaterial switchExpiry = findViewById(R.id.switchExpiryAlerts);
        SwitchMaterial switchConfirm = findViewById(R.id.switchConfirmDelete);
        Button back = findViewById(R.id.btnSettingsBack);

        // Show the saved values.
        switchExpiry.setChecked(isExpiryAlertsEnabled(this));
        switchConfirm.setChecked(isConfirmDeleteEnabled(this));

        // Save immediately whenever a switch is toggled.
        switchExpiry.setOnCheckedChangeListener((button, checked) ->
                prefs(this).edit().putBoolean(KEY_EXPIRY_ALERTS, checked).apply());

        switchConfirm.setOnCheckedChangeListener((button, checked) ->
                prefs(this).edit().putBoolean(KEY_CONFIRM_DELETE, checked).apply());

        back.setOnClickListener(v -> finish());
    }
}
