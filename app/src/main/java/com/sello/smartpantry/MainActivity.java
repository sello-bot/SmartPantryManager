package com.sello.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sello.smartpantry.adapter.PantryAdapter;
import com.sello.smartpantry.db.DatabaseHelper;
import com.sello.smartpantry.model.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantry List screen (the app's home screen).
 * Shows every pantry item from the database in a RecyclerView. Tap a row to edit it,
 * press Delete to remove it, and press + to add a new ingredient.
 */
public class MainActivity extends BaseActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applyInsets(findViewById(R.id.root));

        db = DatabaseHelper.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.pantryRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new PantryAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        emptyText = findViewById(R.id.emptyText);

        // Intent: open the Add screen (no extras means "add a new item")
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        setupBottomNav((BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_pantry);
    }

    /** Reload the list every time the screen becomes visible, so changes made elsewhere show up. */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<Ingredient> items = db.getAllIngredients();
        adapter.setItems(items);
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(Ingredient item) {
        // Intent with data: tell the Add/Edit screen which item to edit
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Ingredient item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteIngredient(item.getId());
                    loadPantry();
                    Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
