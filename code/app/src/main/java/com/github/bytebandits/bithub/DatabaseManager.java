package com.github.bytebandits.bithub;

import android.util.Log;
import com.google.firebase.firestore.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class DatabaseManager {
    private static FirebaseFirestore firestoreDB;
    private static CollectionReference usersCollectionRef;
    private static CollectionReference postsCollectionRef;

    static {
        firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.useEmulator("10.0.2.2", 8080);
    }

    public static FirebaseFirestore getDb() {
        return firestoreDB;
    }
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
     *
     * Example usage:
     * DatabaseManager.getUser("user123", user -> {
     *     if (user != null) {
     *         System.out.println("User data: " + user);
     *     } else {
     *         System.out.println("User not found.");
     *     }
     * });
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
     *
     * Example Usage:
     * DatabaseManager.getPosts(posts -> {
     *     for (MoodPost post : posts) {
     *         System.out.println(post);
     *     }
     *   }
     * )
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
     *
     * Example Usage:
     * DatabaseManager.getPosts(userId, postsMap -> {
     *     if (postsMap != null) {
     *         // Do what you need here
     *     }
     * });
     */
    public static void getPosts(@NotNull String userId, OnPostsFetchListener listener) {
        ArrayList<MoodPost> posts = new ArrayList<>();
        usersCollectionRef.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot userSnapshot = task.getResult();
                ArrayList<DocumentReference> postRefs = (ArrayList<DocumentReference>) userSnapshot.get("postRefs");

                // User does not have any posts
                if (postRefs == null || postRefs.isEmpty()) {
                    listener.onPostsFetched(posts);
                    return;
                }

                int[] postRemaining = {postRefs.size()}; // Track individual post retrieval

                for (DocumentReference postRef : postRefs) {
                    postRef.get().addOnCompleteListener(postTask -> {
                        if (postTask.isSuccessful() && postTask.getResult().exists()) {
                            posts.add(postTask.getResult().toObject(MoodPost.class));
                        }
                        postRemaining[0]--;

                        // When all postRefs are processed, update the map
                        if (postRemaining[0] == 0) {
                            listener.onPostsFetched(posts);
                        }
                    });
                }
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
     *
     * Example Usage:
     * ArrayList<String> userIds = new ArrayList<>();
     * userIds.add("user1");
     * userIds.add("user2");
     * DatabaseManager.getPosts(userIds, postsMap -> {
     *     if (postsMap != null) {
     *         for (String userId : postsMap.keySet()) {
     *             ArrayList<MoodPost> posts = postsMap.get(userId);
     *             Log.d("Database", "User " + userId + " has " + posts.size() + " posts.");
     *         }
     *     }
     * });
     */
    public static void getPosts(@NotNull ArrayList<String> userIds, OnMultipleUsersPostsFetchListener listener) {
        HashMap<String, ArrayList<MoodPost>> postsMap = new HashMap<>();
        int[] remaining = {userIds.size()}; // To track when all user posts are fetched

        for (String userId : userIds) {
            usersCollectionRef.document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot userSnapshot = task.getResult();
                    ArrayList<DocumentReference> postRefs = (ArrayList<DocumentReference>) userSnapshot.get("postRefs");

                    // User does not have any posts
                    if (postRefs == null || postRefs.isEmpty()) {
                        postsMap.put(userId, new ArrayList<>());
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            listener.onMultipleUsersPostsFetched(postsMap);
                        }
                        return;
                    }

                    ArrayList<MoodPost> posts = new ArrayList<>();
                    int[] postRemaining = {postRefs.size()}; // Track individual post retrieval

                    for (DocumentReference postRef : postRefs) {
                        postRef.get().addOnCompleteListener(postTask -> {
                            if (postTask.isSuccessful() && postTask.getResult().exists()) {
                                posts.add(postTask.getResult().toObject(MoodPost.class));
                            }
                            postRemaining[0]--;

                            // When all postRefs are processed, update the map
                            if (postRemaining[0] == 0) {
                                postsMap.put(userId, posts);
                                remaining[0]--;

                                if (remaining[0] == 0) {
                                    listener.onMultipleUsersPostsFetched(postsMap);
                                }
                            }
                        });
                    }
                } else {
                    // user was unable to be fetched
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        listener.onMultipleUsersPostsFetched(postsMap);
                    }
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
     *
     * Example Usage:
     * DatabaseManager.addPost(post, success -> {
     *     if (success) {
     *         System.out.println("Post added successfully.");
     *     } else {
     *         System.out.println("Failed to add post.");
     *     }
     * });
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

        // TODO: Get the current session username
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
     *
     * Example Usage:
     * HashMap<String, Object> updateFields = new HashMap<>();
     * updateFields.put("title", "Updated Title");
     * DatabaseManager.updatePost(postId, updateFields, success -> {
     *     if (success) {
     *         System.out.println("Post updated.");
     *     }
     * });
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
     *
     * Example Usage:
     * DatabaseManager.deletePost(postId, success -> {
     *     if (success) {
     *         System.out.println("Post deleted.");
     *     }
     * });
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
