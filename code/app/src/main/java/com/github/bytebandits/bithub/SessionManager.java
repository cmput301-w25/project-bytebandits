package com.github.bytebandits.bithub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;

/**
 * Manages user sessions using SharedPreferences.
 * Provides functionality to store, retrieve, and clear user session data.
 */
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

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @param context Application context
     */
    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = prefs.edit();
        // this.editor.clear();
        // this.editor.commit();
        this.gson = new Gson();
    }

    /**
     * Retrieves the singleton instance of SessionManager.
     *
     * @param context Application context
     * @return The SessionManager instance
     */
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    /**
     * Creates a user login session by storing the username.
     *
     * @param username The username of the logged-in user
     */
    public void createLoginSession(String username) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    /**
     * Checks if the user is logged in.
     *
     * @return True if the user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(IS_LOGIN, false);
    }

    /**
     * Retrieves the SharedPreferences instance.
     *
     * @return SharedPreferences instance
     */
    public SharedPreferences getSharedPreferences() {
        return this.prefs;
    }

    /**
     * Retrieves the SharedPreferences Editor instance.
     *
     * @return SharedPreferences Editor instance
     */
    public SharedPreferences.Editor getEditor() {
        return this.editor;
    }

    /**
     * Retrieves the stored username of the logged-in user.
     *
     * @return The username, or null if not found
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    /**
     * Checks login status and redirects to StartupActivity if not logged in.
     */
    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(context, StartupActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    /**
     * Saves the user's profile in SharedPreferences.
     *
     * @param profile The Profile object to be stored
     */
    public void saveProfile(Profile profile) {
        String profileJson = gson.toJson(profile);
        editor.putString(KEY_PROFILE, profileJson);
        editor.apply();
    }

    /**
     * Retrieves the stored user profile.
     *
     * @return The Profile object, or null if not found
     */
    public Profile getProfile() {
        String profileJson = prefs.getString(KEY_PROFILE, null);
        return profileJson != null ? gson.fromJson(profileJson, Profile.class) : null;
    }

    /**
     * Logs out the user by clearing session data and redirects to StartupActivity.
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();

        Intent i = new Intent(context, StartupActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * Retrieves the preference name.
     *
     * @return The preference name
     */
    public static String getPrefName() {
        return PREF_NAME;
    }

    /**
     * Retrieves the profile key.
     *
     * @return The profile key
     */
    public static String getKeyProfile() {
        return KEY_PROFILE;
    }

    /**
     * Retrieves the username key.
     *
     * @return The username key
     */
    public static String getKeyUsername() {
        return KEY_USERNAME;
    }

    /**
     * Retrieves the login status key.
     *
     * @return The login status key
     */
    public static String getIsLoginKey() {
        return IS_LOGIN;
    }
}
