package com.github.bytebandits.bithub;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.*;

public class DatabaseManagerTests {
    private static FirebaseFirestore db;
    private static CollectionReference usersCollectionRef;
    private static CollectionReference postsCollectionRef;
    private static Context mockContext;

    @BeforeClass
    public static void setup() {
        DatabaseManager.init();
        mockContext = mock(Context.class);
    }

    @Before
    public void seedDatabase() {
        db = DatabaseManager.getDb();
        usersCollectionRef = db.collection("users");
        postsCollectionRef = db.collection("posts");
    }

    @Test
    public void testGetUser_Success() {
        String userId = "user123";
        HashMap<String, Object> mockUser = new HashMap<>();
        mockUser.put("name", "John Doe");
        usersCollectionRef.document(userId).set(mockUser);

        DatabaseManager.getUser(userId, user -> {
            assertNotNull(user);
            assertEquals("John Doe", user.get("name"));
        });
    }

    @Test
    public void testGetPosts_Success() {
        String userId = "user123";
        DatabaseManager.getUserPosts(userId, posts -> {
            assertNotNull(posts);
            assertTrue(posts.size() >= 0);
        });
    }

    @Test
    public void testAddPost_Success() {
        MoodPost post = new MoodPost(Emotion.SADNESS, new Profile("John Smith"), false, SocialSituation.ALONE, "Test Desc", null);

        DatabaseManager.addPost(mockContext, post, success -> assertTrue(success));
    }

    @Test
    public void testUpdatePost_Success() {
        String postId = UUID.randomUUID().toString();
        MoodPost post = new MoodPost(postId, "Old content");
        postsCollectionRef.document(postId).set(post);

        HashMap<String, Object> options = new HashMap<>();
        options.put("content", "Updated content");

        DatabaseManager.updatePost(postId, options, success -> {
            assertTrue(success);
            postsCollectionRef.document(postId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    assertEquals("Updated content", task.getResult().getString("content"));
                }
            });
        });
    }

    @After
    public void tearDown() {
        String projectId = "byte-bandits-project";
        URL url;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.getResponseCode();
            urlConnection.disconnect();
        } catch (IOException e) {
            Log.e("Cleanup Error", Objects.requireNonNull(e.getMessage()));
        }
    }
}