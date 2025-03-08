package com.github.bytebandits.bithub;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;

public class SessionManagerTest {
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    @Before
    public void setUp() {
        context = context.getApplicationContext();
        sessionManager = new SessionManager(context);
        sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    @Test
    public void testCreateLoginSession() {
        sessionManager.createLoginSession("testUser", "testPassword");

        assertTrue(sharedPreferences.getBoolean("IsLoggedIn", false));
        assertEquals("testUser", sharedPreferences.getString("username", null));
        assertEquals("testPassword", sharedPreferences.getString("password", null));
    }

    @Test
    public void testIsLoggedIn() {
        sessionManager.createLoginSession("testUser", "testPassword");
        assertTrue(sessionManager.isLoggedIn());
    }

    @Test
    public void testSaveProfile() {
        Profile profile = new Profile("testUser", "John Doe", "john@example.com");
        sessionManager.saveProfile(profile);

        String savedProfileJson = sharedPreferences.getString("profile", null);
        assertNotNull(savedProfileJson);

        Profile savedProfile = gson.fromJson(savedProfileJson, Profile.class);
        assertEquals("John Doe", savedProfile.getName());
        assertEquals("john@example.com", savedProfile.getEmail());
    }

    @Test
    public void testLogoutUser() {
        sessionManager.createLoginSession("testUser", "testPassword");
        sessionManager.logoutUser();

        assertFalse(sharedPreferences.getBoolean("IsLoggedIn", true));
        assertNull(sharedPreferences.getString("username", null));
        assertNull(sharedPreferences.getString("password", null));
        assertNull(sharedPreferences.getString("profile", null));
    }
}
