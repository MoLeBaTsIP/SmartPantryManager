package com.example.smartpantry;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Parent of every screen. It provides the app-wide navigation element:
 * a toolbar (action bar) menu with links to Pantry, Suggested Recipes and Settings,
 * plus an Up arrow on the screens that call enableUpButton().
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Show the Up (back) arrow in the action bar.
    protected void enableUpButton() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_pantry) {
            // Explicit Intent back to the pantry; clear the screens above it.
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_recipes) {
            if (!(this instanceof SuggestedRecipesActivity)) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
            }
            return true;
        }
        if (id == R.id.action_settings) {
            if (!(this instanceof SettingsActivity)) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
