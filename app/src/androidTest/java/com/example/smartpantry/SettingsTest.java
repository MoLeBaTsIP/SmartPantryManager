package com.example.smartpantry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests the saved settings (expiry alerts and confirm-before-delete). */
@RunWith(AndroidJUnit4.class)
public class SettingsTest {

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        clearSettings();
    }

    @After
    public void tearDown() {
        clearSettings();
    }

    private void clearSettings() {
        context.getSharedPreferences("smart_pantry_settings", Context.MODE_PRIVATE)
                .edit().clear().commit();
    }

    @Test
    public void expiryAlerts_enabledByDefault() {
        assertTrue(SettingsActivity.isExpiryAlertsEnabled(context));
    }

    @Test
    public void confirmDelete_enabledByDefault() {
        assertTrue(SettingsActivity.isConfirmDeleteEnabled(context));
    }

    @Test
    public void expiryAlerts_savedValueIsRead() {
        context.getSharedPreferences("smart_pantry_settings", Context.MODE_PRIVATE)
                .edit().putBoolean("expiry_alerts", false).commit();
        assertFalse(SettingsActivity.isExpiryAlertsEnabled(context));
    }

    @Test
    public void confirmDelete_savedValueIsRead() {
        context.getSharedPreferences("smart_pantry_settings", Context.MODE_PRIVATE)
                .edit().putBoolean("confirm_delete", false).commit();
        assertFalse(SettingsActivity.isConfirmDeleteEnabled(context));
    }
}
