package com.example.ula_tickets_app;

import android.content.Context;
import android.content.SharedPreferences;

public class CacheHelper {
    private static final String PREFS_NAME = "ULA_APP_Cache";

    public static void saveToCache(Context context, String key, String value){
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getFromCache(Context context, String key, String defaultValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(key, defaultValue);
    }

}
