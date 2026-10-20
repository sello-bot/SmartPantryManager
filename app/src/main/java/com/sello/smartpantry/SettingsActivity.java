package com.sello.smartpantry;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sello.smartpantry.util.Prefs;
import com.sello.smartpantry.util.UnitUtils;

/**
 * Settings screen. Two preferences, saved with SharedPreferences:
 *  - highlight items that are expiring soon in the pantry list;
 *  - the default unit pre-selected when adding a new ingredient.
 */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        applyInsets(findViewById(R.id.root));

        SwitchCompat expirySwitch = findViewById(R.id.switchExpiry);
        expirySwitch.setChecked(Prefs.expiryAlertsEnabled(this));
        expirySwitch.setOnCheckedChangeListener((button, isChecked) ->
                Prefs.setExpiryAlertsEnabled(this, isChecked));

        Spinner unitSpinner = findViewById(R.id.defaultUnitSpinner);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, UnitUtils.UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setSelection(UnitUtils.indexOfUnit(Prefs.defaultUnit(this)));
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Prefs.setDefaultUnit(SettingsActivity.this, UnitUtils.UNITS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing to do
            }
        });

        setupBottomNav((BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_settings);
    }
}
