package com.github.bytebandits.bithub.controller;

import android.content.Context;
import android.util.Log;

import com.github.bytebandits.bithub.model.MoodPost;
import com.google.firebase.firestore.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class DatabaseManager {
    private final FirebaseFirestore firestoreDb;
    private final CollectionReference usersCollectionRef;
    private final CollectionReference postsCollectionRef;

    private static DatabaseManager instance;

    // Singleton Instance
    private DatabaseManager() {
        this.firestoreDb = FirebaseFirestore.getInstance();
        this.usersCollectionRef = firestoreDb.collection("users");
        this.postsCollectionRef = firestoreDb.collection("posts");
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public FirebaseFirestore getDb() { return firestoreDb; }
    public CollectionReference getUsersCollectionRef() { return usersCollectionRef; }
    public CollectionReference getPostsCollectionRef() { return postsCollectionRef; }


    /**
     * Default success handler for Firebase operations, logs the result.
     *
     * @param result The result of the operation (generic type).
     */
    private <T> void defaultSuccessHandler(T result) {
        Log.d("Database", "Operation successful: " + result);
    }

    /**
     * Default failure handler for Firebase operations, logs the error.
     *
     * @param e The exception that occurred.
     */
    private void defaultFailureHandler(Exception e) {
        Log.e("Database", "Operation failed", e);
    }

    // User Management

    /**
     * Fetches a user by their userId from the FireStore database.
     * The result is returned via the provided listener.
     *
     * @param userId The user ID to fetch.
     * @param listener The listener that will receive the result (user data or null).
     * Example usage:
     * DatabaseManager.getUser("user123", user -> {
     *     if (user != null) {
     *         System.out.println("User data: " + user);
     *     } else {
     *         System.out.println("User not found.");
     *     }
     * });
     */
    public void getUser(String userId, OnUserFetchListener listener) {
        usersCollectionRef.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                HashMap<String, Object> user = (HashMap<String, Object>) task.getResult().getData();
                listener.onUserFetched(user);
            } else {
                listener.onUserFetched(null);
            }
        });
    }

    public void addUser(String userId, HashMap<String, Object> userDetails, Optional<OnUserAddListener> listener) {
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

    private void sendNotification(String recipientUserId, DocumentReference docRef){
        DocumentReference recipientDocRef = this.usersCollectionRef.document(recipientUserId);
        recipientDocRef.update("notificationRefs", FieldValue.arrayUnion(docRef));
    }

    private void acceptUserFollow(String currentUserId, DocumentReference requestedUserDocRef) {
        DocumentReference currentUserDocRef = this.usersCollectionRef.document(currentUserId);

        currentUserDocRef.update("followerRefs", FieldValue.arrayUnion(requestedUserDocRef));
        currentUserDocRef.update("notificationsRefs", FieldValue.arrayRemove(requestedUserDocRef));
    }

    private void rejectUserFollow() {

    }

    // Post Management

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
    public void addPost(@NotNull MoodPost post, @NotNull String userId, Optional<OnPostAddedListener> listener) {
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

        DocumentReference postDocRef = postsCollectionRef.document(postId);
        DocumentReference userDocRef = usersCollectionRef.document(userId);
        userDocRef.update("postRefs", FieldValue.arrayUnion(postDocRef));

        sendPostNotifications(userDocRef, postDocRef);
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
    public void updatePost(@NotNull String postId, HashMap<String, Object> options, Optional<OnPostUpdatedListener> listener) {
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
     * Fetches all posts from the FireStore database.
     * The result is returned via the provided listener.
     *
     * @param listener The listener that will receive the result (list of posts).
     * Example Usage:
     * DatabaseManager.getPosts(posts -> {
     *     for (MoodPost post : posts) {
     *         System.out.println(post);
     *     }
     *   }
     * )
     */
    public void getAllPosts(OnPostsFetchListener listener) {
        postsCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<MoodPost> posts = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    posts.add(doc.toObject(MoodPost.class));
                }
                if (listener != null) {
                    listener.onPostsFetched(posts);
                } else {
                    Log.e("DatabaseManager", "Listener is null in getPosts()");
                }
            } else {
                if (listener != null) {
                    listener.onPostsFetched(null);
                } else {
                    Log.e("DatabaseManager", "Listener is null on Firestore query failure");
                }
            }
        });
    }

    /**
     * Fetches posts from a specific user by their userId.
     * The result is returned via the provided listener.
     *
     * @param userId The user ID whose posts need to be fetched.
     * @param listener The listener that will receive the result (list of posts).
     * Example Usage:
     * DatabaseManager.getPosts(userId, postsMap -> {
     *     if (postsMap != null) {
     *         // Do what you need here
     *     }
     * });
     */
    public void getUserPosts(@NotNull String userId, OnPostsFetchListener listener) {
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
    public void getUsersPosts(@NotNull ArrayList<String> userIds, OnMultipleUsersPostsFetchListener listener) {
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
    public void deletePost(@NotNull String postID, @NotNull String userId, Optional<OnPostDeletedListener> listener) {
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

    @SuppressWarnings("unchecked")
    private void sendPostNotifications(DocumentReference userDocRef, DocumentReference postDocRef) {
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
     * Callback interface for fetching posts.
     * Implement this interface to handle the fetched posts.
     */
    public interface OnPostsFetchListener {
        OnPostsFetchListener onPostsFetched(ArrayList<MoodPost> posts);
    }

    /**
     * Callback interface for fetching posts from multiple users.
     * Implement this interface to handle the fetched posts.
     */
    public interface OnMultipleUsersPostsFetchListener {
        void onMultipleUsersPostsFetched(HashMap<String, ArrayList<MoodPost>> postsMap);
    }

    /**
     * Callback interface for adding a post.
     * Implement this interface to handle the success or failure of adding a post.
     */
    public interface OnPostAddedListener {
        void onPostAdded(boolean success);
    }

    /**
     * Callback interface for updating a post.
     * Implement this interface to handle the success or failure of updating a post.
     */
    public interface OnPostUpdatedListener {
        void onPostUpdated(boolean success);
    }

    /**
     * Callback interface for deleting a post.
     * Implement this interface to handle the success or failure of deleting a post.
     */
    public interface OnPostDeletedListener {
        void onPostDeleted(boolean success);
    }
}
