package com.sello.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Shared behaviour for all screens:
 *  - draws the app edge-to-edge and keeps content clear of the status/navigation bars;
 *  - wires up the bottom navigation bar (Pantry / Recipes / Settings) using Intents.
 *
 * Navigation rule: the Pantry screen (MainActivity) is the root of the back stack.
 * Recipes and Settings are opened on top of it, and they replace each other.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private int selectedNavId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Let the app draw behind the system bars; applyInsets() adds the padding back.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    /** Pads the root view so content is not hidden behind the system bars. */
    protected void applyInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
    }

    /** Connects the bottom navigation bar and marks the current screen as selected. */
    protected void setupBottomNav(BottomNavigationView nav, int selectedId) {
        bottomNav = nav;
        selectedNavId = selectedId;
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int targetId = item.getItemId();
            if (targetId != selectedNavId) {
                navigateTo(targetId);
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When we come back to this screen, make sure its own tab is highlighted.
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(selectedNavId).setChecked(true);
        }
    }

    private void navigateTo(int targetId) {
        if (targetId == R.id.nav_pantry) {
            // The Pantry screen is already underneath us in the back stack.
            finish();
            return;
        }
        Class<?> target = (targetId == R.id.nav_suggested)
                ? SuggestedRecipesActivity.class
                : SettingsActivity.class;
        startActivity(new Intent(this, target));
        if (selectedNavId != R.id.nav_pantry) {
            finish(); // Recipes <-> Settings replace each other instead of stacking up
        }
    }
}