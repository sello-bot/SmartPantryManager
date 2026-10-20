package com.sello.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sello.smartpantry.adapter.RecipeAdapter;
import com.sello.smartpantry.db.DatabaseHelper;
import com.sello.smartpantry.model.Ingredient;
import com.sello.smartpantry.model.Recipe;
import com.sello.smartpantry.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen.
 * Runs the strict-matching rule against the current pantry and lists ONLY the recipes
 * the user can cook right now. Shows a friendly message when nothing matches.
 */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        applyInsets(findViewById(R.id.root));

        db = DatabaseHelper.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recipeRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new RecipeAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        emptyText = findViewById(R.id.emptyText);

        setupBottomNav((BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_suggested);
    }

    /** Re-run the matching every time the screen is shown, so it reflects the latest pantry. */
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<Ingredient> pantry = db.getAllIngredients();
        List<Recipe> allRecipes = db.getAllRecipes();
        List<Recipe> matches = RecipeMatcher.findStrictMatches(pantry, allRecipes);

        adapter.setItems(matches);
        emptyText.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
