package com.github.bytebandits.bithub.controller;

import android.content.Context;
import android.util.Log;

import com.github.bytebandits.bithub.model.MoodPost;
import com.google.firebase.firestore.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class DatabaseManager {
    private static final FirebaseFirestore firestoreDB;
    private static CollectionReference usersCollectionRef;
    private static CollectionReference postsCollectionRef;

    static {
        firestoreDB = FirebaseFirestore.getInstance();
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

    public static CollectionReference getUsersCollectionRef() {
        if (usersCollectionRef == null) {
            throw new IllegalStateException("DatabaseManager.init() must be called before accessing Firestore collections.");
        }
        return usersCollectionRef;
    }

    public static CollectionReference getPostsCollectionRef() {
        if (postsCollectionRef == null) {
            throw new IllegalStateException("DatabaseManager.init() must be called before accessing Firestore collections.");
        }
        return postsCollectionRef;
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

    public static void addUser(String userId, HashMap<String, Object> userDetails, Optional<OnUserAddListener> listener) {
        usersCollectionRef.document(userId).set(userDetails)
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("User added successfully");
                    listener.ifPresent(l -> l.onUsersAdded(true));
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    listener.ifPresent(l -> l.onUsersAdded(false));
                });
    }


    /**
     * Callback interface for fetching a user.
     * Implement this interface to handle the fetched user data.
     */
    public interface OnUserFetchListener {
        void onUserFetched(HashMap<String, Object> user);
    }

    public interface OnUserAddListener {
        void onUsersAdded(boolean added);
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
        Log.d("DatabaseManager", "getPosts() called");

        postsCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<MoodPost> posts = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    posts.add(doc.toObject(MoodPost.class));
                }
                Log.d("DatabaseManager", "getPosts() fetched " + posts.size() + " posts");

                if (listener != null) {
                    listener.onPostsFetched(posts);
                } else {
                    Log.e("DatabaseManager", "Listener is null in getPosts()");
                }
            } else {
                Log.e("DatabaseManager", "Firestore query failed: ", task.getException());

                if (listener != null) {
                    listener.onPostsFetched(null);
                } else {
                    Log.e("DatabaseManager", "Listener is null on Firestore query failure");
                }
            }
        });
    }


    /**
     * Callback interface for fetching posts.
     * Implement this interface to handle the fetched posts.
     */
    public interface OnPostsFetchListener {
        OnPostsFetchListener onPostsFetched(ArrayList<MoodPost> posts);
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
    public static void getUserPosts(@NotNull String userId, OnPostsFetchListener listener) {
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
    public static void getUsersPosts(@NotNull ArrayList<String> userIds, OnMultipleUsersPostsFetchListener listener) {
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
    public static void addPost(@NotNull Context context, @NotNull MoodPost post, Optional<OnPostAddedListener> listener) {
        String postId = post.getPostID().toString();

        postsCollectionRef.document(postId).set(post)
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post added successfully");
                    listener.ifPresent(l -> l.onPostAdded(true));
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    listener.ifPresent(l -> l.onPostAdded(false));
                });

        // Get the session username
        String userId = SessionManager.getInstance(context).getUsername();
        if (userId == null) {
            Log.e("DatabaseManager", "User not logged in. Cannot add post.");
            listener.ifPresent(l -> l.onPostAdded(false));
            return;
        }

        DocumentReference postDocRef = postsCollectionRef.document(postId);
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
    public static void updatePost(@NotNull String postId, HashMap<String, Object> options, Optional<OnPostUpdatedListener> listener) {
        DocumentReference postRef = postsCollectionRef.document(postId.toString());

        postRef.update(options)
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post updated successfully");
                    listener.ifPresent(l -> l.onPostUpdated(true));
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    listener.ifPresent(l -> l.onPostUpdated(false));
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
    public static void deletePost(@NotNull Context context, @NotNull String postID, Optional<OnPostDeletedListener> listener) {
        String userId = SessionManager.getInstance(context).getUsername();
        if (userId == null) {
            Log.e("DatabaseManager", "User not logged in. Cannot delete post.");
            listener.ifPresent(l -> l.onPostDeleted(false));
            return;
        }

        DocumentReference postDocRef = postsCollectionRef.document(postID.toString());
        DocumentReference userDocRef = usersCollectionRef.document(userId);

        postDocRef.delete()
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post deleted successfully");
                    userDocRef.update("postRefs", FieldValue.arrayRemove(postDocRef));
                    listener.ifPresent(l -> l.onPostDeleted(true));
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    listener.ifPresent(l -> l.onPostDeleted(false));
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
