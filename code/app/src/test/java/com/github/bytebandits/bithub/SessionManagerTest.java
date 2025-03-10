package com.github.bytebandits.bithub;

import android.content.Context;
import android.content.SharedPreferences;
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
    private Map<String, Object> fakeSharedPrefs;

    @Before
    public void setUp() {
        context = Mockito.mock(Context.class);
        sharedPreferences = Mockito.mock(SharedPreferences.class);
        editor = Mockito.mock(SharedPreferences.Editor.class);
        gson = new Gson();
        fakeSharedPrefs = new HashMap<>();
        sessionManager = SessionManager.getInstance(context);


        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);

        when(editor.putBoolean(anyString(), anyBoolean())).thenAnswer(invocation -> {
            fakeSharedPrefs.put(invocation.getArgument(0), invocation.getArgument(1));
            return editor;
        });

        when(editor.putString(anyString(), anyString())).thenAnswer(invocation -> {
            fakeSharedPrefs.put(invocation.getArgument(0), invocation.getArgument(1));
            return editor;
        });

        when(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenAnswer(
                invocation -> fakeSharedPrefs.getOrDefault(invocation.getArgument(0), invocation.getArgument(1)));

        when(sharedPreferences.getString(anyString(), any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return fakeSharedPrefs.containsKey(key) ? (String) fakeSharedPrefs.get(key) : (String) invocation.getArgument(1);
        });

        when(editor.commit()).thenReturn(true);

        when(editor.remove(anyString())).thenAnswer(invocation -> {
            fakeSharedPrefs.remove(invocation.getArgument(0));
            return editor;
        });

        when(editor.clear()).thenAnswer(invocation -> {
            fakeSharedPrefs.clear();
            return editor;
        });
    }

    @Test
    public void testCreateLoginSession() {
        sessionManager.createLoginSession("testUser");
        assertTrue(sessionManager.isLoggedIn());
        assertEquals("testUser", sessionManager.getUsername());
    }

    @Test
    public void testIsLoggedIn() {
        sessionManager.createLoginSession("testUser");
        assertTrue(sessionManager.isLoggedIn());
    }

    @Test
    public void testSaveProfile() {
        Profile profile = new Profile("testUser");
        sessionManager.saveProfile(profile);
        Profile savedProfile = sessionManager.getProfile();
        assertNotNull(savedProfile);
    }

    @Test
    public void testLogoutUser() {
        sessionManager.createLoginSession("testUser");
        assertTrue(sessionManager.isLoggedIn());
        sessionManager.logoutUser();
        assertFalse(sessionManager.isLoggedIn());
    }
}
