package com.sello.smartpantry.util;

import com.sello.smartpantry.model.Ingredient;
import com.sello.smartpantry.model.Recipe;
import com.sello.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE STRICT-MATCHING RULE (core business logic of the app).
 *
 * A recipe is only suggested if EVERY ingredient it needs is in the pantry, in at
 * least the required quantity. If even one ingredient is missing or short, the
 * recipe is excluded.
 *
 * To be robust to real-world messiness:
 *  - names are normalised ("Tomatoes" and "tomato" are the same ingredient);
 *  - units are converted to a base unit (1 kg == 1000 g, 1 cup == 240 ml);
 *  - several pantry rows of the same ingredient are added together.
 */
public final class RecipeMatcher {

    private RecipeMatcher() { }

    /** Returns only the recipes the user can cook right now. */
    public static List<Recipe> findStrictMatches(List<Ingredient> pantry, List<Recipe> recipes) {
        Map<String, Double> stock = buildStock(pantry);
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMake(recipe, stock)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    /** Totals the pantry per ingredient and kind of unit, e.g. "tomato|2" -> 5.0 pieces. */
    public static Map<String, Double> buildStock(List<Ingredient> pantry) {
        Map<String, Double> stock = new HashMap<>();
        for (Ingredient item : pantry) {
            String key = stockKey(item.getName(), item.getUnit());
            double amount = UnitUtils.toBase(item.getQuantity(), item.getUnit());
            Double existing = stock.get(key);
            stock.put(key, (existing == null ? 0.0 : existing) + amount);
        }
        return stock;
    }

    /** True only if every required ingredient is in stock in a sufficient amount. */
    public static boolean canMake(Recipe recipe, Map<String, Double> stock) {
        if (recipe.getIngredients().isEmpty()) {
            return false;
        }
        for (RecipeIngredient need : recipe.getIngredients()) {
            Double have = stock.get(stockKey(need.getName(), need.getUnit()));
            double required = UnitUtils.toBase(need.getQuantity(), need.getUnit());
            if (have == null || have + 0.000001 < required) {
                return false; // one missing or short ingredient rules the recipe out
            }
        }
        return true;
    }

    private static String stockKey(String name, String unit) {
        return IngredientNames.normalize(name) + "|" + UnitUtils.typeOf(unit);
    }
}
