package com.example.videomeeting.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static com.example.videomeeting.utils.Constants.CURRENT_USER;
import static com.example.videomeeting.utils.Constants.INTENT_PHONE_NUMBER;
import static com.example.videomeeting.utils.Constants.PREF_HAS_INTRO_BEEN_OPENED;
import static com.example.videomeeting.utils.Constants.PREF_IS_SIGNED_IN;
import static com.example.videomeeting.utils.Constants.PREF_NEEDS_TO_SETUP_PROFILE;
import static com.example.videomeeting.utils.Constants.PREF_SHOULD_SHOW_BATTERY_DIALOG;

public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_GLOBAL_NAME, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public void clearPrefsForSignOut() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_IS_SIGNED_IN);
        editor.remove(INTENT_PHONE_NUMBER);
        editor.remove(PREF_NEEDS_TO_SETUP_PROFILE);
        editor.apply();
    }
}
