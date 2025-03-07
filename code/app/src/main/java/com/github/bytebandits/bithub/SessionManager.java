package com.github.bytebandits.bithub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String IS_LOGIN = "IsLoggedIn";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private Context context;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = prefs.edit();
        this.context = context;
        this.gson = new Gson();
    }

    public void createLoginSession(String username, String password) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);

        editor.commit();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(IS_LOGIN, false);
    }

    public void checkLogin() {
        if (!this.isLoggedIn()) {
            // Redirect to the start up activity
            Intent i = new Intent(context, StartupActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            context.startActivity(i);
        }
    }

    // Save Profile object
    public void saveProfile(Profile profile) {
        String profileJson = gson.toJson(profile);
        editor.putString(KEY_PROFILE, profileJson);
        editor.commit(); // Commit changes
    }

    // Get Profile object
    public Profile getProfile() {
        String profileJson = prefs.getString(KEY_PROFILE, null);
        return profileJson != null ? gson.fromJson(profileJson, Profile.class) : null;
    }

    // Clear session
    public void logoutUser() {
        editor.remove(KEY_PROFILE);
        editor.clear();
        editor.commit();

        Intent i = new Intent(context, StartupActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(i);
    }
}