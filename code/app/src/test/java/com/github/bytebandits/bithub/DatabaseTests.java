package com.github.bytebandits.bithub;

import com.google.firebase.firestore.*;
import com.google.firebase.firestore.testing.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.junit.jupiter.api.extension.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    private static Firestore firestore;
    private static FirebaseFirestore firestoreDB;
    private static CollectionReference usersCollectionRef;
    private static CollectionReference postsCollectionRef;
    private static FirebaseFirestoreEmulator emulator;

    @BeforeAll
    public static void setup() {
        // Initialize Firestore emulator (use local Firestore emulator)
        emulator = new FirebaseFirestoreEmulator();
        firestore = emulator.getFirestore();
        firestoreDB = FirebaseFirestore.getInstance();
        usersCollectionRef = firestore.collection("users");
        postsCollectionRef = firestore.collection("posts");

        // Initialize database before each test
        Database.init(); // This will initialize your Database with the emulator.
    }

    @AfterEach
    public void teardown() {
        // Clean up the Firestore emulator after each test to avoid data contamination
        firestore.collection("users").get().addOnCompleteListener(task -> {
            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                snapshot.getReference().delete();
            }
        });
        firestore.collection("posts").get().addOnCompleteListener(task -> {
            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                snapshot.getReference().delete();
            }
        });
    }

    @Test
    public void testGetUser_Success() {
        // Given a user document in Firestore
        String userId = "user123";
        HashMap<String, Object> mockUser = new HashMap<>();
        mockUser.put("name", "John Doe");
        usersCollectionRef.document(userId).set(mockUser);

        // When calling the getUser method
        Database.getUser(userId, new Database.OnUserFetchListener() {
            @Override
            public void onUserFetched(HashMap<String, Object> user) {
                // Then ensure that the user data is fetched correctly
                assertNotNull(user);
                assertEquals("John Doe", user.get("name"));
            }
        });
    }

    @Test
    public void testGetPosts_Success() {
        // Given a post document in Firestore
        String userId = "user123";
        MoodPost post = new MoodPost(UUID.randomUUID(), "Feeling great!");
        postsCollectionRef.document(userId).collection("posts").document(post.getId().toString()).set(post);

        // When calling the getPosts method
        Database.getPosts(userId, new Database.OnPostsFetchListener() {
            @Override
            public void onPostsFetched(ArrayList<MoodPost> posts) {
                // Then ensure that the posts are fetched correctly
                assertNotNull(posts);
                assertEquals(1, posts.size());
                assertEquals("Feeling great!", posts.get(0).getContent());
            }
        });
    }

    @Test
    public void testAddPost_Success() {
        // Given a post object
        MoodPost post = new MoodPost(UUID.randomUUID(), "This is a new post");

        // When calling addPost
        Database.addPost(post, new Database.OnPostAddedListener() {
            @Override
            public void onPostAdded(boolean success) {
                // Then ensure the post was added successfully
                assertTrue(success);
            }
        });
    }

    @Test
    public void testUpdatePost_Success() {
        // Given a post document
        String postId = UUID.randomUUID().toString();
        MoodPost post = new MoodPost(UUID.fromString(postId), "Old content");
        postsCollectionRef.document(postId).set(post);

        // When calling updatePost
        HashMap<String, Object> options = new HashMap<>();
        options.put("content", "Updated content");
        Database.updatePost(UUID.fromString(postId), options, new Database.OnPostUpdatedListener() {
            @Override
            public void onPostUpdated(boolean success) {
                // Then ensure the post content was updated correctly
                assertTrue(success);
                postsCollectionRef.document(postId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String updatedContent = (String) task.getResult().get("content");
                        assertEquals("Updated content", updatedContent);
                    }
                });
            }
        });
    }

    @Test
    public void testDeletePost_Success() {
        // Given a post document
        String postId = UUID.randomUUID().toString();
        MoodPost post = new MoodPost(UUID.fromString(postId), "Content to delete");
        postsCollectionRef.document(postId).set(post);

        // When calling deletePost
        Database.deletePost(UUID.fromString(postId), new Database.OnPostDeletedListener() {
            @Override
            public void onPostDeleted(boolean success) {
                // Then ensure the post was deleted successfully
                assertTrue(success);
                postsCollectionRef.document(postId).get().addOnCompleteListener(task -> {
                    assertFalse(task.isSuccessful());
                });
            }
        });
    }

    @Test
    public void testGetPosts_MultipleUsers_Success() {
        // Given multiple users with posts
        String userId1 = "user123";
        String userId2 = "user456";
        MoodPost post1 = new MoodPost(UUID.randomUUID(), "User 1 post");
        MoodPost post2 = new MoodPost(UUID.randomUUID(), "User 2 post");

        postsCollectionRef.document(userId1).collection("posts").document(post1.getId().toString()).set(post1);
        postsCollectionRef.document(userId2).collection("posts").document(post2.getId().toString()).set(post2);

        // When calling getPosts with multiple users
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add(userId1);
        userIds.add(userId2);

        Database.getPosts(userIds, new Database.OnMultipleUsersPostsFetchListener() {
            @Override
            public void onMultipleUsersPostsFetched(HashMap<String, ArrayList<MoodPost>> postsMap) {
                // Then ensure posts are fetched correctly for both users
                assertEquals(2, postsMap.size());
                assertNotNull(postsMap.get(userId1));
                assertNotNull(postsMap.get(userId2));
                assertEquals(1, postsMap.get(userId1).size());
                assertEquals(1, postsMap.get(userId2).size());
            }
        });
    }
}
