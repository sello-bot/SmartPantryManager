package com.sello.smartpantry.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Makes ingredient names comparable. "Tomatoes", "tomato " and "TOMATO" all
 * normalise to "tomato", so the matching logic does not break on trivial
 * differences in spelling, case or singular/plural.
 */
public final class IngredientNames {

    /** Alternative names mapped to one standard name (keys are already normalised). */
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        ALIASES.put("aubergine", "eggplant");
        ALIASES.put("courgette", "zucchini");
        ALIASES.put("scallion", "green onion");
        ALIASES.put("spring onion", "green onion");
        ALIASES.put("capsicum", "bell pepper");
        ALIASES.put("plain flour", "flour");
        ALIASES.put("oil", "cooking oil");
        ALIASES.put("vegetable oil", "cooking oil");
        ALIASES.put("sunflower oil", "cooking oil");
        ALIASES.put("mielie meal", "maize meal");
        ALIASES.put("rolled oat", "oat");
    }

    private IngredientNames() { }

    /** Lower-cases, strips punctuation, collapses spaces, singularises each word and applies aliases. */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String word : cleaned.split(" ")) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(singular(word));
        }
        String name = result.toString();
        String alias = ALIASES.get(name);
        return alias != null ? alias : name;
    }

    /** A simple English singulariser: tomatoes -> tomato, berries -> berry, eggs -> egg. */
    private static String singular(String word) {
        if (word.length() <= 3) {
            return word;
        }
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes") || word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    /** Capitalises the first letter for display. */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }
}
