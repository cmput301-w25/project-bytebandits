package com.github.bytebandits.bithub;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

public class DatabaseManagerTests {
    private FirebaseFirestore db;
    private static CollectionReference usersCollectionRef;
    private static CollectionReference postsCollectionRef;

    /**
     * Sets up the database before all tests run.
     */
    @BeforeClass
    public static void setup() {
        DatabaseManager.init();
    }

    /**
     * Seeds the Firestore database with test data before each test.
     */
    @Before
    public void seedDatabase() {
        db = DatabaseManager.getDb();

        // Create collections
        String userId = "user123";
        HashMap<String, Object> mockUser = new HashMap<>();
        mockUser.put("name", "John Doe");
        db.collection("users").document(userId).set(mockUser);

        MoodPost mockPost = new MoodPost(UUID.randomUUID(), "Random Mock Content");
        HashMap<String, Object> mockPostMap = new HashMap<>();
        mockPostMap.put("content", mockPost.getContent());
        db.collection("posts").document(mockPost.getId().toString()).set(mockPostMap);

        usersCollectionRef = db.collection("users");
        postsCollectionRef = db.collection("posts");
    }

    /**
     * Tests retrieving a user from Firestore.
     */
    @Test
    public void testGetUser_Success() {
        String userId = "user123";

        DatabaseManager.getUser(userId, new DatabaseManager.OnUserFetchListener() {
            @Override
            public void onUserFetched(HashMap<String, Object> user) {
                assertNotNull(user);
                assertEquals("John Doe", user.get("name"));
            }
        });
    }

    /**
     * Tests retrieving posts associated with a user.
     */
    @Test
    public void testGetPosts_Success() {
        String userId = "user123";

        DatabaseManager.getPosts(userId, new DatabaseManager.OnPostsFetchListener() {
            @Override
            public void onPostsFetched(ArrayList<MoodPost> posts) {
                assertNotNull(posts);
                assertEquals(1, posts.size());
                assertEquals("Random Mock Content", posts.get(0).getContent());
            }
        });
    }

    /**
     * Tests adding a new post to Firestore.
     */
    @Test
    public void testAddPost_Success() {
        MoodPost post = new MoodPost(UUID.randomUUID(), "This is a new post");

        DatabaseManager.addPost(post, new DatabaseManager.OnPostAddedListener() {
            @Override
            public void onPostAdded(boolean success) {
                assertTrue(success);
            }
        });
    }

    /**
     * Tests updating an existing post in Firestore.
     */
    @Test
    public void testUpdatePost_Success() {
        UUID postId = UUID.randomUUID();
        MoodPost post = new MoodPost(postId, "Old content");
        postsCollectionRef.document(postId.toString()).set(post);

        HashMap<String, Object> options = new HashMap<>();
        options.put("content", "Updated content");

        DatabaseManager.updatePost(postId, options, success -> {
            assertTrue(success);
            postsCollectionRef.document(postId.toString()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String updatedContent = task.getResult().getString("content");
                    assertEquals("Updated content", updatedContent);
                }
            });
        });
    }


    /**
     * Tests deleting a post from Firestore.
     */
    @Test
    public void testDeletePost_Success() {
        UUID postId = UUID.randomUUID();
        MoodPost post = new MoodPost(postId, "Content to delete");
        postsCollectionRef.document(postId.toString()).set(post);

        DatabaseManager.deletePost(postId, success -> {
            assertTrue(success);
            postsCollectionRef.document(postId.toString()).get().addOnCompleteListener(task -> {
                assertFalse(task.getResult().exists());
            });
        });
    }

    /**
     * Tests retrieving posts from multiple users.
     */
    @Test
    public void testGetPosts_MultipleUsers_Success() {
        String userId1 = "user123";
        String userId2 = "user456";
        MoodPost post1 = new MoodPost(UUID.randomUUID(), "User 1 post");
        MoodPost post2 = new MoodPost(UUID.randomUUID(), "User 2 post");

        DocumentReference postRef1 = postsCollectionRef.document(post1.getId().toString());
        DocumentReference postRef2 = postsCollectionRef.document(post2.getId().toString());
        postRef1.set(post1);
        postRef2.set(post2);

        usersCollectionRef.document(userId1).update("postRefs", FieldValue.arrayUnion(postRef1));
        usersCollectionRef.document(userId2).update("postRefs", FieldValue.arrayUnion(postRef2));

        ArrayList<String> userIds = new ArrayList<>(Arrays.asList(userId1, userId2));

        DatabaseManager.getPosts(userIds, postsMap -> {
            assertEquals(2, postsMap.size());
            assertNotNull(postsMap.get(userId1));
            assertNotNull(postsMap.get(userId2));
            assertEquals(1, postsMap.get(userId1).size());
            assertEquals(1, postsMap.get(userId2).size());
        });
    }


    /**
     * Cleans up Firestore database after each test by deleting test data.
     */
    @After
    public void tearDown() {
        String projectId = "byte-bandits-project";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
