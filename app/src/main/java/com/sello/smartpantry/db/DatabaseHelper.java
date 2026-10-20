package com.sello.smartpantry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sello.smartpantry.model.Ingredient;
import com.sello.smartpantry.model.Recipe;
import com.sello.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Local SQLite database for the app.
 *
 * Tables:
 *  - pantry             : the user's own ingredients (full CRUD)
 *  - recipes            : the pre-loaded recipe collection (seeded on first run)
 *  - recipe_ingredients : the ingredients each recipe requires (belongs to a recipe)
 *
 * onCreate() runs only once, the first time the database file is created, which
 * is when the recipes are seeded. The data is stored in a file on the device,
 * so it is still there after the app is closed and reopened.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_PANTRY = "pantry";
    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_RECIPE_ING = "recipe_ingredients";

    private static DatabaseHelper instance;

    /** One shared helper for the whole app (avoids opening the database many times). */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + TABLE_RECIPES + "(id) ON DELETE CASCADE)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 only: a simple rebuild. A real release would migrate the data instead.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ------------------------------------------------------------------
    // PANTRY: Create, Read, Update, Delete
    // ------------------------------------------------------------------

    /** CREATE: inserts a pantry item and returns its new id (or -1 on failure). */
    public long addIngredient(Ingredient item) {
        return getWritableDatabase().insert(TABLE_PANTRY, null, toValues(item));
    }

    /** READ: all pantry items, sorted by name. */
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE_PANTRY, null, null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(ingredientFromCursor(c));
            }
        }
        return list;
    }

    /** READ: one pantry item by id, or null if it does not exist. */
    public Ingredient getIngredient(long id) {
        try (Cursor c = getReadableDatabase().query(
                TABLE_PANTRY, null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c.moveToFirst()) {
                return ingredientFromCursor(c);
            }
        }
        return null;
    }

    /** UPDATE: saves changes to an existing pantry item. Returns the number of rows changed. */
    public int updateIngredient(Ingredient item) {
        return getWritableDatabase().update(
                TABLE_PANTRY, toValues(item), "id = ?", new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: removes a pantry item. Returns the number of rows deleted. */
    public int deleteIngredient(long id) {
        return getWritableDatabase().delete(
                TABLE_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(Ingredient item) {
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry", item.getExpiryDate()); // null is stored as NULL
        return values;
    }

    private Ingredient ingredientFromCursor(Cursor c) {
        int expiryIndex = c.getColumnIndexOrThrow("expiry");
        return new Ingredient(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.isNull(expiryIndex) ? null : c.getString(expiryIndex));
    }

    // ------------------------------------------------------------------
    // RECIPES (read-only for the user)
    // ------------------------------------------------------------------

    /** All recipes, each with its full ingredient list. */
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        List<Recipe> list = new ArrayList<>();
        try (Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(recipeFromCursor(db, c));
            }
        }
        return list;
    }

    /** One recipe by id, or null if it does not exist. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_RECIPES, null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c.moveToFirst()) {
                return recipeFromCursor(db, c);
            }
        }
        return null;
    }

    /** Distinct ingredient names used by recipes; used for the auto-complete suggestions. */
    public List<String> getKnownIngredientNames() {
        List<String> names = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                true, TABLE_RECIPE_ING, new String[]{"name"}, null, null, null, null, "name ASC", null)) {
            while (c.moveToNext()) {
                names.add(c.getString(0));
            }
        }
        return names;
    }

    private Recipe recipeFromCursor(SQLiteDatabase db, Cursor c) {
        Recipe recipe = new Recipe(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("steps")));
        recipe.setIngredients(loadRecipeIngredients(db, recipe.getId()));
        return recipe;
    }

    private List<RecipeIngredient> loadRecipeIngredients(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        try (Cursor c = db.query(TABLE_RECIPE_ING, null, "recipe_id = ?",
                new String[]{String.valueOf(recipeId)}, null, null, "id ASC")) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("quantity")),
                        c.getString(c.getColumnIndexOrThrow("unit"))));
            }
        }
        return list;
    }

    // ------------------------------------------------------------------
    // SEED DATA: 20 recipes inserted the first time the database is created
    // ------------------------------------------------------------------

    /**
     * Adds one recipe. Each ingredient is written as "name;quantity;unit",
     * for example "tomato;3;pcs".
     */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("steps", steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (String line : ingredients) {
            String[] parts = line.split(";");
            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put("recipe_id", recipeId);
            ingredientValues.put("name", parts[0]);
            ingredientValues.put("quantity", Double.parseDouble(parts[1]));
            ingredientValues.put("unit", parts[2]);
            db.insert(TABLE_RECIPE_ING, null, ingredientValues);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "1. Crack the eggs into a bowl, add the salt and whisk.\n"
                + "2. Melt the butter in a pan over medium heat.\n"
                + "3. Pour in the eggs and stir gently until just set.\n"
                + "4. Serve hot.",
                "egg;2;pcs", "butter;1;tbsp", "salt;1;tsp");

        addRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs with the salt.\n"
                + "2. Melt the butter in a pan over medium heat.\n"
                + "3. Pour in the eggs and let the base set.\n"
                + "4. Sprinkle the cheese over one half, fold over and cook for 1 minute.",
                "egg;3;pcs", "cheese;50;g", "butter;1;tbsp", "salt;1;tsp");

        addRecipe(db, "French Toast",
                "1. Whisk the eggs, milk and sugar together.\n"
                + "2. Dip each slice of bread into the mixture.\n"
                + "3. Melt the butter in a pan and fry the bread until golden on both sides.",
                "bread;4;pcs", "egg;2;pcs", "milk;100;ml", "butter;1;tbsp", "sugar;1;tbsp");

        addRecipe(db, "Pancakes",
                "1. Mix the flour and sugar, then whisk in the egg and milk until smooth.\n"
                + "2. Heat a little oil in a pan.\n"
                + "3. Pour in a ladle of batter and cook until bubbles form, then flip.\n"
                + "4. Repeat with the rest of the batter.",
                "flour;150;g", "egg;1;pcs", "milk;250;ml", "sugar;1;tbsp", "cooking oil;1;tbsp");

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water until tender, then drain.\n"
                + "2. Fry the chopped onion and garlic in oil until soft.\n"
                + "3. Add the chopped tomatoes and salt, and simmer for 10 minutes.\n"
                + "4. Toss the pasta in the sauce and serve.",
                "pasta;200;g", "tomato;3;pcs", "onion;1;pcs", "garlic;2;pcs",
                "cooking oil;1;tbsp", "salt;1;tsp");

        addRecipe(db, "Macaroni Cheese",
                "1. Boil the pasta until tender and drain.\n"
                + "2. Melt the butter, stir in the flour, then slowly whisk in the milk to make a sauce.\n"
                + "3. Stir in the cheese until melted.\n"
                + "4. Mix in the pasta and serve.",
                "pasta;200;g", "cheese;100;g", "milk;200;ml", "butter;1;tbsp", "flour;1;tbsp");

        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n"
                + "2. Fry the chopped onion in oil until soft.\n"
                + "3. Push the onion aside and scramble the eggs in the pan.\n"
                + "4. Add the rice and salt, and stir-fry for 3 minutes.",
                "rice;200;g", "egg;2;pcs", "onion;1;pcs", "cooking oil;1;tbsp", "salt;1;tsp");

        addRecipe(db, "Pap and Tomato Gravy",
                "1. Boil water and slowly stir in the maize meal until thick. Cover and cook on low heat for 15 minutes.\n"
                + "2. Fry the chopped onion in oil until soft.\n"
                + "3. Add the chopped tomatoes and salt, and simmer until saucy.\n"
                + "4. Serve the gravy over the pap.",
                "maize meal;250;g", "tomato;2;pcs", "onion;1;pcs", "cooking oil;1;tbsp", "salt;1;tsp");

        addRecipe(db, "Mashed Potatoes",
                "1. Peel the potatoes and boil them with salt until soft.\n"
                + "2. Drain and mash.\n"
                + "3. Stir in the butter and warm milk until creamy.",
                "potato;4;pcs", "butter;2;tbsp", "milk;50;ml", "salt;1;tsp");

        addRecipe(db, "Roast Potatoes",
                "1. Heat the oven to 200 degrees C.\n"
                + "2. Cut the potatoes into chunks and toss with the oil and salt.\n"
                + "3. Roast for 40 minutes, turning once, until golden and crispy.",
                "potato;6;pcs", "cooking oil;2;tbsp", "salt;1;tsp");

        addRecipe(db, "Chicken Rice Bowl",
                "1. Season the chicken with salt and fry in oil until cooked through.\n"
                + "2. Cook the rice.\n"
                + "3. Fry the sliced onion until soft.\n"
                + "4. Serve the chicken and onion over the rice.",
                "chicken;300;g", "rice;200;g", "onion;1;pcs", "cooking oil;1;tbsp", "salt;1;tsp");

        addRecipe(db, "Chicken Stir-Fry",
                "1. Slice the chicken, carrots and onion.\n"
                + "2. Fry the chicken in hot oil with the salt until cooked.\n"
                + "3. Add the carrots and onion and stir-fry for 5 minutes.\n"
                + "4. Serve hot.",
                "chicken;300;g", "carrot;2;pcs", "onion;1;pcs", "cooking oil;1;tbsp", "salt;1;tsp");

        addRecipe(db, "Lentil Soup",
                "1. Fry the chopped onion, carrot and garlic in oil for 5 minutes.\n"
                + "2. Add the rinsed lentils, salt and enough water to cover.\n"
                + "3. Simmer for 30 minutes until the lentils are soft.\n"
                + "4. Serve hot.",
                "lentil;200;g", "onion;1;pcs", "carrot;1;pcs", "garlic;2;pcs",
                "cooking oil;1;tbsp", "salt;1;tsp");

        addRecipe(db, "Vegetable Soup",
                "1. Peel and dice the potatoes, carrots and onion.\n"
                + "2. Add them to a pot with the salt and enough water to cover.\n"
                + "3. Simmer for 25 minutes until soft.\n"
                + "4. Serve hot.",
                "potato;2;pcs", "carrot;2;pcs", "onion;1;pcs", "salt;1;tsp");

        addRecipe(db, "Bean and Tomato Stew",
                "1. Fry the chopped onion in oil until soft.\n"
                + "2. Add the chopped tomatoes and salt, and cook for 5 minutes.\n"
                + "3. Stir in the beans and simmer for 15 minutes.",
                "bean;400;g", "tomato;2;pcs", "onion;1;pcs", "cooking oil;1;tbsp", "salt;1;tsp");

        addRecipe(db, "Cheese and Tomato Toast",
                "1. Toast the bread and spread it with butter.\n"
                + "2. Top with sliced tomato and cheese.\n"
                + "3. Grill until the cheese melts.",
                "bread;2;pcs", "cheese;60;g", "tomato;1;pcs", "butter;1;tbsp");

        addRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n"
                + "2. Spread with peanut butter.\n"
                + "3. Top with sliced banana.",
                "bread;2;pcs", "peanut butter;2;tbsp", "banana;1;pcs");

        addRecipe(db, "Banana Oat Porridge",
                "1. Heat the oats and milk in a pot, stirring, for 5 minutes.\n"
                + "2. Mash in half of the banana and stir.\n"
                + "3. Top with the remaining banana slices and the sugar.",
                "oat;80;g", "banana;1;pcs", "milk;250;ml", "sugar;1;tsp");

        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta until tender and drain.\n"
                + "2. Fry the chopped onion in oil until soft.\n"
                + "3. Add the tuna and salt and heat through.\n"
                + "4. Toss with the pasta and serve.",
                "pasta;200;g", "tuna;170;g", "onion;1;pcs", "cooking oil;1;tbsp", "salt;1;tsp");

        addRecipe(db, "Sweet Maize Porridge",
                "1. Heat the milk in a pot.\n"
                + "2. Stir in the maize meal and cook on low heat for 15 minutes, stirring often.\n"
                + "3. Add the butter and sugar and serve warm.",
                "maize meal;100;g", "milk;250;ml", "butter;1;tbsp", "sugar;1;tbsp");
    }
}
