package com.github.bytebandits.bithub;

import android.util.Log;

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.model.DocumentReferences;
import com.github.bytebandits.bithub.model.Emotion;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Profile;
import com.github.bytebandits.bithub.model.SocialSituation;
import com.google.firebase.firestore.*;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * DatabaseManager tests
 *
 * @author Michael Tran
 */
public class DatabaseManagerTest {
    private DatabaseManager dbInstance;
    private Profile testProfile;

    @BeforeClass
    public static void setup() {
        DatabaseManager.getInstance(true);
    }

    @Before
    public void seedDatabase() {
        this.dbInstance = DatabaseManager.getInstance();
        CollectionReference postsCollectionRef = dbInstance.getPostsCollectionRef();
        CollectionReference usersCollectionRef = dbInstance.getUsersCollectionRef();

        // Add Users
        HashMap<String, Object> user1 = new HashMap<>();
        user1.put("userId", "testUser1");
        user1.put("name", "John Doe");
        user1.put("email", "johndoe@example.com");
        user1.put("age", 30);
        user1.put("isActive", true);

        HashMap<String, Object> user2 = new HashMap<>(user1);
        user2.put("userId", "testUser2");
        user2.put("name", "Doe John");

        DocumentReference user1DocRef = usersCollectionRef.document((String) Objects.requireNonNull(user1.get("userId")));
        DocumentReference user2DocRef = usersCollectionRef.document((String) Objects.requireNonNull(user2.get("userId")));

        user1DocRef.set(user1);
        user2DocRef.set(user2);

        // Add Posts
        this.testProfile = new Profile((String) Objects.requireNonNull(user1.get("userId")));
        MoodPost[] moodPosts = {
                new MoodPost(Emotion.HAPPINESS, testProfile, false, null, null, null, true),
                new MoodPost(Emotion.SADNESS, new Profile((String) Objects.requireNonNull(user2.get("userId"))), false, SocialSituation.ALONE, "Test Desc",
                        null, true),
        };

        int count = 0;
        for (MoodPost post : moodPosts) {
            DocumentReference postDocRef = postsCollectionRef.document(post.getPostID());
            postDocRef.set(post);
            if (count == 0)
                user1DocRef.update(DocumentReferences.POSTS.getDocRefString(), FieldValue.arrayUnion(postDocRef));
            else
                user2DocRef.update(DocumentReferences.POSTS.getDocRefString(), FieldValue.arrayUnion(postDocRef));
            count++;
        }
    }

    @Test
    public void testGetUser_Success() {
        Log.d("DatabaseManagerTest", "Calling getUser...");
        dbInstance.getUser(testProfile.getUserId(), user -> {
            Log.d("DatabaseManagerTest", "Callback executed");
            String name = (String) user.get("name");
            assert name != null;
            assertTrue(name.matches("John Doe"));
        });
    }

    @Test
    public void testGetAllPosts_Success() {
        dbInstance.getAllPosts(
                dbPosts ->
                        assertFalse(dbPosts.isEmpty())
        );
    }

    @Test
    public void testAddPosts_Success() {
        MoodPost post = new MoodPost(
                Emotion.SHAME,
                testProfile, false, SocialSituation.ALONE, "Test Desc",
                null, true
        );

        dbInstance.addPost(post, testProfile.getUserId(), Assert::assertTrue);
    }

    @Test
    public void testUpdatePost_Success() {
        MoodPost post = new MoodPost(Emotion.SURPRISE, testProfile, false, null, "Test Post", null, true);
        dbInstance.addPost(post, testProfile.getUserId(), null);

        HashMap<String, Object> updateFields = new HashMap<>();
        updateFields.put("description", "Updated Description");

        dbInstance.updatePost(post.getPostID(), updateFields, Assert::assertTrue);

    }

    @Test
    public void testDeletePost_Success() {
        MoodPost post = new MoodPost(Emotion.ANGER, testProfile, false, null, "To be deleted", null, true);
        dbInstance.addPost(post, testProfile.getUserId(), null);

        dbInstance.deletePost(post.getPostID(), testProfile.getUserId(), Assert::assertTrue);
    }

    @Test
    public void testAddUser_Success() {
        HashMap<String, Object> userDetails = new HashMap<>();
        userDetails.put("userId", "testUser3");
        userDetails.put("name", "Jane Doe");

        dbInstance.addUser("testUser3", userDetails, Assert::assertTrue);
    }

    @Test
    public void testSearchUsers_Success() {
        dbInstance.searchUsers("testUser", users ->
            assertFalse(users.isEmpty())
        );
    }

    @Test
    public void testGetUserPosts_Success() {
        dbInstance.getUserPosts(testProfile.getUserId(), posts -> {
            assertNotNull(posts);
            assertFalse(posts.isEmpty());
        });
    }

    @Test
    public void testGetUsersPosts_Success() {
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add("testUser1");
        userIds.add("testUser2");

        dbInstance.getUsersPosts(userIds, postsMap -> {
            assertNotNull(postsMap);
            assertEquals(2, postsMap.size());
        });
    }

    @AfterClass
    public static void tearDown() {
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
