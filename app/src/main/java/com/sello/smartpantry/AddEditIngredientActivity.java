package com.sello.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sello.smartpantry.db.DatabaseHelper;
import com.sello.smartpantry.model.Ingredient;
import com.sello.smartpantry.util.Prefs;
import com.sello.smartpantry.util.UnitUtils;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add / Edit Ingredient screen.
 * If an ingredient id is passed in the Intent, the screen edits that item (UPDATE);
 * otherwise it creates a new one (CREATE). All input is validated before saving.
 */
public class AddEditIngredientActivity extends BaseActivity {

    public static final String EXTRA_INGREDIENT_ID = "ingredient_id";

    private DatabaseHelper db;
    private TextInputLayout nameLayout;
    private TextInputLayout qtyLayout;
    private MaterialAutoCompleteTextView nameInput;
    private TextInputEditText qtyInput;
    private TextInputEditText expiryInput;
    private Spinner unitSpinner;

    private long editId = -1; // -1 means "adding a new ingredient"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        applyInsets(findViewById(R.id.root));

        db = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        nameLayout = findViewById(R.id.nameLayout);
        qtyLayout = findViewById(R.id.qtyLayout);
        nameInput = findViewById(R.id.nameInput);
        qtyInput = findViewById(R.id.qtyInput);
        expiryInput = findViewById(R.id.expiryInput);
        unitSpinner = findViewById(R.id.unitSpinner);

        // Unit drop-down
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, UnitUtils.UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setSelection(UnitUtils.indexOfUnit(Prefs.defaultUnit(this)));

        // Name suggestions come from the ingredients used in the recipes
        nameInput.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, db.getKnownIngredientNames()));

        expiryInput.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnClearDate).setOnClickListener(v -> expiryInput.setText(""));
        findViewById(R.id.btnSave).setOnClickListener(v -> saveIngredient());

        // Did the previous screen send us an id? Then we are editing.
        editId = getIntent().getLongExtra(EXTRA_INGREDIENT_ID, -1);
        if (editId != -1) {
            toolbar.setTitle("Edit Ingredient");
            loadIngredientForEditing();
        }
    }

    private void loadIngredientForEditing() {
        Ingredient item = db.getIngredient(editId);
        if (item == null) {
            Toast.makeText(this, "Ingredient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        nameInput.setText(item.getName(), false);
        qtyInput.setText(UnitUtils.formatQty(item.getQuantity()));
        unitSpinner.setSelection(UnitUtils.indexOfUnit(item.getUnit()));
        if (item.getExpiryDate() != null) {
            expiryInput.setText(item.getExpiryDate());
        }
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, day) -> expiryInput.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates the form, then creates or updates the ingredient. */
    private void saveIngredient() {
        nameLayout.setError(null);
        qtyLayout.setError(null);

        String name = textOf(nameInput).trim().replaceAll("\\s+", " ");
        String qtyText = textOf(qtyInput).trim().replace(',', '.');
        boolean valid = true;

        // --- validate the name ---
        if (name.isEmpty()) {
            nameLayout.setError("Please enter an ingredient name");
            valid = false;
        } else if (name.length() > 40) {
            nameLayout.setError("Name is too long (maximum 40 characters)");
            valid = false;
        } else if (!name.matches("[\\p{L}][\\p{L} '\\-]*")) {
            nameLayout.setError("Use letters, spaces or hyphens only");
            valid = false;
        }

        // --- validate the quantity ---
        double quantity = 0;
        if (qtyText.isEmpty()) {
            qtyLayout.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText);
                if (quantity <= 0 || quantity > 100000) {
                    qtyLayout.setError("Quantity must be more than 0 and at most 100000");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                qtyLayout.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) {
            return; // stay on the form so the user can fix the errors
        }

        String unit = (String) unitSpinner.getSelectedItem();
        String expiry = textOf(expiryInput).trim();
        if (expiry.isEmpty()) {
            expiry = null;
        }

        if (editId == -1) {
            db.addIngredient(new Ingredient(0, name, quantity, unit, expiry));        // CREATE
            Toast.makeText(this, name + " added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateIngredient(new Ingredient(editId, name, quantity, unit, expiry)); // UPDATE
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private static String textOf(TextView view) {
        CharSequence text = view.getText();
        return text == null ? "" : text.toString();
    }
}
