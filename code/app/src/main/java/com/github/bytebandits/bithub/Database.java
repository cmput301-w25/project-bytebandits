package com.github.bytebandits.bithub;

import android.util.Log;
import com.google.firebase.firestore.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import com.google.firebase.firestore.auth.User;

public final class Database {
    private static final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private static CollectionReference usersCollectionRef;
    private static CollectionReference postsCollectionRef;

    /**
     * Initializes Firestore collections.
     * Call this method before using any database operations to set up the necessary references.
     */
    public static void init() {
        usersCollectionRef = firestoreDB.collection("users");
        postsCollectionRef = firestoreDB.collection("posts");
    }

    /**
     * Default success handler for Firebase operations, logs the result.
     *
     * @param result The result of the operation (generic type).
     */
    private static <T> void defaultSuccessHandler(T result) {
        Log.d("Database", "Operation successful: " + result);
    }

    /**
     * Default failure handler for Firebase operations, logs the error.
     *
     * @param e The exception that occurred.
     */
    private static void defaultFailureHandler(Exception e) {
        Log.e("Database", "Operation failed", e);
    }

    /**
     * Fetches a user by their userId from the Firestore database.
     * The result is returned via the provided listener.
     *
     * @param userId The user ID to fetch.
     * @param listener The listener that will receive the result (user data or null).
     */
    public static void getUser(String userId, OnUserFetchListener listener) {
        usersCollectionRef.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                HashMap<String, Object> user = (HashMap<String, Object>) task.getResult().getData();
                listener.onUserFetched(user);
            } else {
                listener.onUserFetched(null);
            }
        });
    }

    /**
     * Callback interface for fetching a user.
     * Implement this interface to handle the fetched user data.
     */
    public interface OnUserFetchListener {
        void onUserFetched(HashMap<String, Object> user);
    }

    /**
     * Fetches all posts from the Firestore database.
     * The result is returned via the provided listener.
     *
     * @param listener The listener that will receive the result (list of posts).
     */
    public static void getPosts(OnPostsFetchListener listener) {
        postsCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<MoodPost> posts = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    posts.add(doc.toObject(MoodPost.class));
                }
                listener.onPostsFetched(posts);
            } else {
                listener.onPostsFetched(null);
            }
        });
    }

    /**
     * Callback interface for fetching posts.
     * Implement this interface to handle the fetched posts.
     */
    public interface OnPostsFetchListener {
        void onPostsFetched(ArrayList<MoodPost> posts);
    }

    /**
     * Fetches posts from a specific user by their userId.
     * The result is returned via the provided listener.
     *
     * @param userId The user ID whose posts need to be fetched.
     * @param listener The listener that will receive the result (list of posts).
     */
    public static void getPosts(@NotNull String userId, OnPostsFetchListener listener) {
        postsCollectionRef.document(userId).collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<MoodPost> posts = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    posts.add(doc.toObject(MoodPost.class));
                }
                listener.onPostsFetched(posts);
            } else {
                listener.onPostsFetched(null);
            }
        });
    }

    /**
     * Fetches posts from multiple users based on their userIds.
     * The result is returned via the provided listener.
     * Once all posts are fetched, the listener will be called with the results.
     *
     * @param userIds List of user IDs whose posts need to be fetched.
     * @param listener The listener that will receive the result (map of userId to their posts).
     */
    public static void getPosts(@NotNull ArrayList<String> userIds, OnMultipleUsersPostsFetchListener listener) {
        HashMap<String, ArrayList<MoodPost>> postsMap = new HashMap<>();
        int[] remaining = {userIds.size()}; // To track when all user posts are fetched

        for (String userId : userIds) {
            postsCollectionRef.document(userId).collection("posts").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ArrayList<MoodPost> posts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        posts.add(doc.toObject(MoodPost.class));
                    }
                    postsMap.put(userId, posts);
                }
                remaining[0]--;
                if (remaining[0] == 0) {
                    listener.onMultipleUsersPostsFetched(postsMap);
                }
            });
        }
    }

    /**
     * Callback interface for fetching posts from multiple users.
     * Implement this interface to handle the fetched posts.
     */
    public interface OnMultipleUsersPostsFetchListener {
        void onMultipleUsersPostsFetched(HashMap<String, ArrayList<MoodPost>> postsMap);
    }

    /**
     * Adds a new post to the Firestore database.
     * The result is returned via the provided listener.
     *
     * @param post The post object to be added.
     * @param listener The listener that will receive the success result.
     */
    public static void addPost(@NotNull MoodPost post, OnPostAddedListener listener) {
        String postId = post.getId().toString();

        postsCollectionRef.document(postId).set(post)
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post added successfully");
                    listener.onPostAdded(true);
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    listener.onPostAdded(false);
                });

        // Set the post reference (document reference) to user id's list of posts
        DocumentReference postDocRef = postsCollectionRef.document(postId);
        String userId = "mock"; // Mock userId

        DocumentReference userDocRef = usersCollectionRef.document(userId);
        userDocRef.update("postRefs", FieldValue.arrayUnion(postDocRef));

        sendPostNotifications(userDocRef, postDocRef);
    }

    /**
     * Callback interface for adding a post.
     * Implement this interface to handle the success or failure of adding a post.
     */
    public interface OnPostAddedListener {
        void onPostAdded(boolean success);
    }

    @SuppressWarnings("unchecked")
    private static void sendPostNotifications(DocumentReference userDocRef, DocumentReference postDocRef) {
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Object followersRefsObject = documentSnapshot.get("followersRefs");
                if (followersRefsObject instanceof ArrayList<?>) {
                    ArrayList<DocumentReference> followersRef = (ArrayList<DocumentReference>) followersRefsObject;

                    for (DocumentReference followerRef : followersRef) {
                        followerRef.update("notificationsRef", FieldValue.arrayUnion(postDocRef));
                    }
                }
            }
        });
    }

    /**
     * Updates a post in the Firestore database.
     * The result is returned via the provided listener.
     *
     * @param postId The ID of the post to update.
     * @param options A map of fields to update (field names and values).
     * @param listener The listener that will receive the success result.
     */
    public static void updatePost(@NotNull UUID postId, HashMap<String, Object> options, OnPostUpdatedListener listener) {
        DocumentReference postRef = postsCollectionRef.document(postId.toString());

        postRef.update(options)
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post updated successfully");
                    listener.onPostUpdated(true);
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    listener.onPostUpdated(false);
                });
    }

    /**
     * Callback interface for updating a post.
     * Implement this interface to handle the success or failure of updating a post.
     */
    public interface OnPostUpdatedListener {
        void onPostUpdated(boolean success);
    }

    /**
     * Deletes a post from the Firestore database.
     * The result is returned via the provided listener.
     *
     * @param postID The ID of the post to delete.
     * @param listener The listener that will receive the success result.
     */
    public static void deletePost(@NotNull UUID postID, OnPostDeletedListener listener) {
        postsCollectionRef.document(postID.toString()).delete()
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post deleted successfully");
                    listener.onPostDeleted(true);
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    listener.onPostDeleted(false);
                });
    }

    /**
     * Callback interface for deleting a post.
     * Implement this interface to handle the success or failure of deleting a post.
     */
    public interface OnPostDeletedListener {
        void onPostDeleted(boolean success);
    }
}
