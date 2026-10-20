package com.sello.smartpantry.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small wrapper around SharedPreferences for the Settings screen.
 */
public final class Prefs {

    private static final String FILE_NAME = "pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    private Prefs() { }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static boolean expiryAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    public static String defaultUnit(Context context) {
        return prefs(context).getString(KEY_DEFAULT_UNIT, "g");
    }

    public static void setDefaultUnit(Context context, String unit) {
        prefs(context).edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }
}
