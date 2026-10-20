package com.sello.smartpantry;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.sello.smartpantry.db.DatabaseHelper;
import com.sello.smartpantry.model.Recipe;
import com.sello.smartpantry.model.RecipeIngredient;
import com.sello.smartpantry.util.IngredientNames;
import com.sello.smartpantry.util.UnitUtils;

/**
 * Recipe Detail screen: the full ingredient list and method for one recipe.
 * The recipe id arrives through the Intent that started this screen.
 */
public class RecipeDetailActivity extends BaseActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        applyInsets(findViewById(R.id.root));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = DatabaseHelper.getInstance(this).getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvRecipeName)).setText(recipe.getName());

        StringBuilder ingredients = new StringBuilder();
        for (RecipeIngredient item : recipe.getIngredients()) {
            ingredients.append("\u2022 ")
                    .append(IngredientNames.capitalize(item.getName()))
                    .append(" - ")
                    .append(UnitUtils.formatQty(item.getQuantity()))
                    .append(' ')
                    .append(item.getUnit())
                    .append('\n');
        }
        ((TextView) findViewById(R.id.tvIngredients)).setText(ingredients.toString().trim());
        ((TextView) findViewById(R.id.tvSteps)).setText(recipe.getSteps());
    }
}
