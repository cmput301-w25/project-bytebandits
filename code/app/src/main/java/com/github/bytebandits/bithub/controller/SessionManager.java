package com.github.bytebandits.bithub.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.github.bytebandits.bithub.model.Profile;
import com.github.bytebandits.bithub.view.StartupActivity;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_USERNAME = "username";
    private static final String IS_LOGIN = "IsLoggedIn";

    private static SessionManager instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private Context context;

    // Private constructor to prevent direct instantiation
    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = prefs.edit();
        this.gson = new Gson();
    }

    // Public method to get the singleton instance
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // Create login session
    public void createLoginSession(String username) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(IS_LOGIN, false);
    }

    public SharedPreferences getSharedPreferences() {
        return this.prefs;
    }

    public SharedPreferences.Editor getEditor() {
        return this.editor;
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(context, StartupActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    // Save Profile object
    public void saveProfile(Profile profile) {
        String profileJson = gson.toJson(profile);
        editor.putString(KEY_PROFILE, profileJson);
        editor.apply();
    }

    // Get Profile object
    public Profile getProfile() {
        String profileJson = prefs.getString(KEY_PROFILE, null);
        return profileJson != null ? gson.fromJson(profileJson, Profile.class) : null;
    }

    // Logout user and clear session
    public void logoutUser() {
        editor.clear();
        editor.commit();

        Intent i = new Intent(context, StartupActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static String getPrefName() {
        return PREF_NAME;
    }

    public static String getKeyProfile() {
        return KEY_PROFILE;
    }

    public static String getKeyUsername() {
        return KEY_USERNAME;
    }

    public static String getIsLoginKey() {
        return IS_LOGIN;
    }
}
