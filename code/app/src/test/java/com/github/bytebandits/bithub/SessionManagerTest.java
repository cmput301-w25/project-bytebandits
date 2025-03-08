package com.github.bytebandits.bithub;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.bytebandits.bithub.Profile;
import com.github.bytebandits.bithub.SessionManager;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

public class SessionManagerTest {
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    private Map<String, Object> fakeSharedPrefs; // Simulated SharedPreferences storage

    @Before
    public void setUp() {
        context = Mockito.mock(Context.class);
        sharedPreferences = Mockito.mock(SharedPreferences.class);
        editor = Mockito.mock(SharedPreferences.Editor.class);
        gson = new Gson();

        // Fake in-memory storage
        fakeSharedPrefs = new HashMap<>();

        // Stub behavior to return the mocked SharedPreferences
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);

        // Stub putBoolean()
        when(editor.putBoolean(anyString(), anyBoolean())).thenAnswer(invocation -> {
            fakeSharedPrefs.put(invocation.getArgument(0), invocation.getArgument(1));
            return editor;
        });

        // Stub putString() - Handles JSON serialization properly
        when(editor.putString(anyString(), anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);

            System.out.println("Key: " + key);
            System.out.println("Value: " + value);
            fakeSharedPrefs.put(key, value);
            return editor;
        });

        // Stub getBoolean()
        when(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenAnswer(
                invocation -> fakeSharedPrefs.getOrDefault(invocation.getArgument(0), invocation.getArgument(1)));

        // Stub getString() - Returns stored JSON when requested
        when(sharedPreferences.getString(anyString(), any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object defaultValue = invocation.getArgument(1); // Allow null
            return fakeSharedPrefs.containsKey(key) ? (String) fakeSharedPrefs.get(key) : (String) defaultValue;
        });

        // Stub commit()
        when(editor.commit()).thenReturn(true);

        // Stub remove() - Removes a key from the fake shared preferences
        when(editor.remove(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            fakeSharedPrefs.remove(key);
            return editor;
        });

        // Stub clear() - Clears all stored values
        when(editor.clear()).thenAnswer(invocation -> {
            fakeSharedPrefs.clear();
            return editor;
        });

        // Initialize SessionManager
        sessionManager = new SessionManager(context);
    }

    @Test
    public void testCreateLoginSession() {
        System.out.println("Running testCreateLoginSession...");
        sessionManager.createLoginSession("testUser", "testPassword");

        System.out.println("Stored Values:");
        System.out.println("IsLoggedIn: " + sharedPreferences.getBoolean("IsLoggedIn", false));

        String username = sharedPreferences.getString("username", "N/A");
        System.out.println("Username: " + sharedPreferences.getString("username", "N/A"));
        String password = sharedPreferences.getString("password", "N/A");
        System.out.println("Password: " + sharedPreferences.getString("password", "N/A"));

        assertTrue(sharedPreferences.getBoolean("IsLoggedIn", false));
        assertEquals("testUser", username);
        assertEquals("testPassword", password);
    }

    @Test
    public void testIsLoggedIn() {
        sessionManager.createLoginSession("testUser", "testPassword");
        assertTrue(sessionManager.isLoggedIn());
    }

    @Test
    public void testSaveProfile() {
        System.out.println("Running testSaveProfile...");

        // Create profile
        Profile profile = new Profile("testUser", "John Doe", "john@example.com");

        // Save profile
        sessionManager.saveProfile(profile);

        // Retrieve saved JSON
        String savedProfileJson = sharedPreferences.getString("profile", null);
        System.out.println("Stored Profile JSON: " + savedProfileJson);
        assertNotNull(savedProfileJson);

        // Deserialize and verify fields
        Profile savedProfile = gson.fromJson(savedProfileJson, Profile.class);
        System.out.println("Deserialized Profile Name: " + savedProfile.getName());
        System.out.println("Deserialized Profile Email: " + savedProfile.getEmail());

        assertEquals("John Doe", savedProfile.getName());
        assertEquals("john@example.com", savedProfile.getEmail());
    }

    @Test
    public void testLogoutUser() {
        System.out.println("Running testLogoutUser...");

        // Create session first
        sessionManager.createLoginSession("testUser", "testPassword");

        // Save profile
        Profile profile = new Profile("testUser", "John Doe", "john@example.com");
        sessionManager.saveProfile(profile);

        // Check that values exist before logout
        assertTrue(sessionManager.isLoggedIn());
        String username = sharedPreferences.getString("username", "N/A");
        System.out.println(username);
        assertNotNull(sharedPreferences.getString("username", null));
        assertNotNull(sharedPreferences.getString("password", null));
        assertNotNull(sharedPreferences.getString("profile", null));

        // Logout user
        sessionManager.logoutUser();

        // Verify all values are cleared
        System.out.println("After Logout:");
        System.out.println("IsLoggedIn: " + sharedPreferences.getBoolean("IsLoggedIn", true));
        System.out.println("Username: " + sharedPreferences.getString("username", "N/A"));
        System.out.println("Password: " + sharedPreferences.getString("password", "N/A"));
        System.out.println("Profile: " + sharedPreferences.getString("profile", "N/A"));

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sharedPreferences.getString("username", null));
        assertNull(sharedPreferences.getString("password", null));
        assertNull(sharedPreferences.getString("profile", null));
    }

}
