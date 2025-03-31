package com.github.bytebandits.bithub.controller;

import android.util.Log;

import androidx.annotation.Nullable;

import com.github.bytebandits.bithub.model.DocumentReferences;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Profile;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;


/**
 * DatabaseManager class that involves all actions for posts and gets from the Firebase Firestore.
 * @author Michael Tran, Rasheed Othman
 */
public final class DatabaseManager {
    private final FirebaseFirestore firestoreDb;
    private final CollectionReference usersCollectionRef;
    private final CollectionReference postsCollectionRef;

    private static DatabaseManager instance;

    // Singleton Instance
    private DatabaseManager(boolean useEmulator) {
        this.firestoreDb = FirebaseFirestore.getInstance();
        if (useEmulator) {
            this.firestoreDb.useEmulator("10.0.2.2", 8080);
        }

        this.usersCollectionRef = firestoreDb.collection("users");
        this.postsCollectionRef = firestoreDb.collection("posts");
    }

    @Singleton
    public static synchronized DatabaseManager getInstance(boolean useEmulator) {
        if (instance == null) {
            instance = new DatabaseManager(useEmulator);
        }
        return instance;
    }

    public static DatabaseManager getInstance() {
        return getInstance(false); // Default: Don't use emulator
    }

    // Testing functions for offline persistence
    public void setOffline() {
        this.firestoreDb.disableNetwork();
    }
    public void setOnline(){
        this.firestoreDb.enableNetwork();
    }

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
     * @param userId   The user ID to fetch.
     * @param listener The listener that will receive the result (user data or
     *                 null).
     *                 Example usage:
     *                 DatabaseManager.getUser("user123", user -> {
     *                 if (user != null) {
     *                 System.out.println("User data: " + user);
     *                 } else {
     *                 System.out.println("User not found.");
     *                 }
     *                 });
     */
    public void getUser(@NotNull String userId, OnUserFetchListener listener) {
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
     * Adds a new user to the Firestore database.
     *
     * @param userId      The unique identifier for the user.
     * @param userDetails A HashMap containing user details to be stored.
     * @param listener    An optional listener to handle success or failure
     *                    callbacks.
     */
    public void addUser(@NotNull String userId, HashMap<String, Object> userDetails, @Nullable OnUserAddListener listener) {
        usersCollectionRef.document(userId).set(userDetails)
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("User added successfully");
                    if (listener != null) {
                        listener.onUsersAdded(true);
                    }
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    if (listener != null) {
                        listener.onUsersAdded(false);
                    }
                });
    }

    /**
     * Searches for users whose userId matches or starts with the given query.
     *
     * @param query    The search term used to find users.
     * @param listener A listener to handle the searched users
     */
    public void searchUsers(String query, OnUserSearchFetchListener listener) {
        Log.d("DatabaseManager", "Starting searchUsers for query: " + query);

        Query users = this.usersCollectionRef
                .orderBy("userId")
                .startAt(query)
                .endAt(query + "~");

        users.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                List<HashMap<String, Object>> userList = new ArrayList<>();

                Log.d("DatabaseManager", "Query successful. Found " + querySnapshot.size() + " users.");

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Map<String, Object> userData = doc.getData();
                    userList.add((HashMap<String, Object>) userData);

                    // Log each user entry
                    Log.d("DatabaseManager", "Fetched user: " + userData);
                }

                if (listener != null) {
                    listener.onUsersFetched(userList);
                }
            } else {
                Log.e("DatabaseManager", "Error fetching users", task.getException());
                if (listener != null) {
                    listener.onUsersFetched(null);
                }
            }
        });
    }

    // Get Followers, Edit Post,

    /**
     * Sends a notification to a specific user.
     *
     * @param recipientUserId The ID of the user who will receive the notification.
     * @param docRef          A reference to the document associated with the
     *                        notification.
     * @param type            Determine whether it is a post notification or follow notification.
     */
    public void sendNotification(@NotNull String recipientUserId, DocumentReference docRef, DocumentReferences type) {
        DocumentReference recipientDocRef = this.usersCollectionRef.document(recipientUserId);
        recipientDocRef.update(type.getDocRefString(), FieldValue.arrayUnion(docRef));
    }

    /**
     * Fetches notifications for a given user from Firestore.
     *
     * @param userId The unique ID of the user whose notifications are to be retrieved.
     *
     *
     */
    @SuppressWarnings("unchecked")
    public void getNotifications(@NotNull String userId, OnNotificationsFetchListener listener) {
        DocumentReference userDocRef = this.usersCollectionRef.document(userId);
        userDocRef.get().addOnSuccessListener(userDocSnapshot -> {
            ArrayList<MoodPost> postNotifications = new ArrayList<>();
            ArrayList<HashMap<String, Object>> requestNotifications = new ArrayList<>();
            if (!userDocSnapshot.exists()) {
                listener.onNotificationsFetchListener(postNotifications, new ArrayList<>());
                return;
            }

            // Prepare lists of tasks
            List<Task<DocumentSnapshot>> postTasks = new ArrayList<>();
            List<Task<DocumentSnapshot>> requestTasks = new ArrayList<>();

            // Fetch post notifications
            Object postRefsObj = userDocSnapshot.get(DocumentReferences.NOTIFICATION_POSTS.getDocRefString());
            if (postRefsObj instanceof List) {
                List<DocumentReference> postRefs = (List<DocumentReference>) postRefsObj;
                for (DocumentReference postRef : postRefs) {
                    postTasks.add(postRef.get());
                }
            }

            // Fetch request notifications
            Object requestRefsObj = userDocSnapshot.get(DocumentReferences.NOTIFICATION_REQS.getDocRefString());
            if (requestRefsObj instanceof List) {
                List<DocumentReference> requestRefs = (List<DocumentReference>) requestRefsObj;
                for (DocumentReference requestRef : requestRefs) {
                    requestTasks.add(requestRef.get());
                }
            }

            // If no tasks, immediately return
            if (postTasks.isEmpty() && requestTasks.isEmpty()) {
                listener.onNotificationsFetchListener(postNotifications, requestNotifications);
                return;
            }

            // Create a list to track all tasks
            List<Task<DocumentSnapshot>> allTasks = new ArrayList<>();
            allTasks.addAll(postTasks);
            allTasks.addAll(requestTasks);

            // Wait for all tasks to complete
            Tasks.whenAll(allTasks).addOnCompleteListener(task -> {
                // Process post notifications
                for (Task<DocumentSnapshot> postTask : postTasks) {
                    if (postTask.isSuccessful()) {
                        DocumentSnapshot postDoc = postTask.getResult();
                        if (postDoc.exists()) {
                            try {
                                MoodPost post = postDoc.toObject(MoodPost.class);

                                // Handle profile conversion if needed
                                if (post != null) {
                                    postNotifications.add(post);
                                }
                            } catch (Exception e) {
                                Log.e("DatabaseManager", "Error processing post", e);
                            }
                        }
                    } else {
                        Log.e("DatabaseManager", "Post task failed: " + postTask.getException());
                    }
                }

                // Process request notifications
                for (Task<DocumentSnapshot> requestTask : requestTasks) {
                    if (requestTask.isSuccessful()) {
                        DocumentSnapshot userDoc = requestTask.getResult();
                        if (userDoc.exists()) {
                            try {
                                HashMap<String, Object> user = new HashMap<>(Objects.requireNonNull(userDoc.getData()));
                                requestNotifications.add(user);
                            } catch (Exception e) {
                                Log.e("DatabaseManager", "Error processing request", e);
                            }
                        }
                    } else {
                        Log.e("DatabaseManager", "Request task failed: " + requestTask.getException());
                    }
                }

                // Call listener with all fetched notifications
                listener.onNotificationsFetchListener(postNotifications, requestNotifications);
            });
        }).addOnFailureListener(e -> {
            Log.e("DatabaseManager", "Error fetching user document: " + e.getMessage(), e);
            listener.onNotificationsFetchListener(new ArrayList<>(), new ArrayList<>());
        });
    }

    public void clearAllNotifications(@NotNull String userId) {
        DocumentReference userDocRef = this.usersCollectionRef.document(userId);

        // Clear both post and request notifications
        Map<String, Object> updates = new HashMap<>();
        updates.put(DocumentReferences.NOTIFICATION_POSTS.getDocRefString(), new ArrayList<>());
        updates.put(DocumentReferences.NOTIFICATION_REQS.getDocRefString(), new ArrayList<>());

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid ->
                    Log.d("DatabaseManager", "All notifications cleared successfully")
                )
                .addOnFailureListener(e ->
                    Log.e("DatabaseManager", "Error clearing notifications", e)
                );
    }


    /**
     * Sends a follow request to a specific user.
     *
     * @param currentUserId   The ID of the current user.
     * @param requestedUserId The ID of the user that the request is for.
     *
     */
    public void sendFollowRequest(@NotNull String currentUserId, @NotNull String requestedUserId) {
        DocumentReference currentDocRef = this.usersCollectionRef.document(currentUserId);
        this.sendNotification(requestedUserId, currentDocRef, DocumentReferences.NOTIFICATION_REQS);
    }

    /**
     * Accepts a follow request from another user.
     *
     * @param currentUserId   The ID of the current user.
     * @param requestedUserId The ID of the user requesting to follow.
     */
    public void acceptUserFollow(@NotNull String currentUserId, @NotNull String requestedUserId) {
        DocumentReference requestedUserDocRef = this.usersCollectionRef.document(requestedUserId);
        DocumentReference currentUserDocRef = this.usersCollectionRef.document(currentUserId);

        // Add the requesting user document reference to your followers
        currentUserDocRef.update(DocumentReferences.FOLLOWERS.getDocRefString(),
                FieldValue.arrayUnion(requestedUserDocRef));
        currentUserDocRef.update(DocumentReferences.NOTIFICATION_REQS.getDocRefString(),
                FieldValue.arrayRemove(requestedUserDocRef));

        // Add current user document reference to requester's followings
        requestedUserDocRef.update(DocumentReferences.FOLLOWINGS.getDocRefString(),
                FieldValue.arrayUnion(currentUserDocRef));
    }

    /**
     * Rejects a follow request from another user.
     *
     * @param currentUserId   The ID of the current user.
     * @param requestedUserId The ID of the user whose request is being rejected.
     */
    public void rejectUserFollow(@NotNull String currentUserId, @NotNull String requestedUserId) {
        DocumentReference requestedUserDocRef = this.usersCollectionRef.document(requestedUserId);
        DocumentReference currentUserDocRef = this.usersCollectionRef.document(currentUserId);

        currentUserDocRef.update(DocumentReferences.NOTIFICATION_REQS.getDocRefString(),
                FieldValue.arrayRemove(requestedUserDocRef));
    }

    /**
     * Unfollow a user.
     *
     * @param currentUserId The ID of the current user.
     * @param targetUserId  The ID of the user that will be unfollowed.
     */
    public void unfollowUser(@NotNull String currentUserId, @NotNull String targetUserId) {
        DocumentReference currentUserDocRef = this.usersCollectionRef.document(currentUserId);
        DocumentReference targetUserDocRef = this.usersCollectionRef.document(targetUserId);

        // Remove following from current user
        currentUserDocRef.update(DocumentReferences.FOLLOWINGS.getDocRefString(), FieldValue.arrayRemove(targetUserDocRef));

        // Remove follower from target user
        targetUserDocRef.update(DocumentReferences.FOLLOWERS.getDocRefString(), FieldValue.arrayRemove(currentUserDocRef));
    }

    /**
     * Check if a user is being followed by the current user signed in
     *
     * @param currentUserId The ID of the user whose followings list is being checked.
     * @param checkUserId   The ID of the user to check if they are being followed.
     * @param listener      A callback to return whether the target user is being followed.
     */
    @SuppressWarnings("unchecked")
    public void checkFollowing(@NotNull String currentUserId, @NotNull String checkUserId, OnCheckFollowingListener listener) {
        DocumentReference currentUserDocRef = this.usersCollectionRef.document(currentUserId);

        currentUserDocRef.get()
                .addOnSuccessListener(userDocSnapshot -> {
                    if (!userDocSnapshot.exists() || !userDocSnapshot.contains(DocumentReferences.FOLLOWINGS.getDocRefString())) {
                        Log.d("DatabaseManager", "User document does not exist or has no followings.");
                        listener.onCheckFollowingListener(false);
                    } else {
                        List<DocumentReference> followings = (List<DocumentReference>) userDocSnapshot.get(DocumentReferences.FOLLOWINGS.getDocRefString());
                        boolean isFollowing = false;
                        if (followings != null && !followings.isEmpty()) {
                            isFollowing = followings.stream().anyMatch(ref -> ref.getId().equals(checkUserId));
                        }
                        Log.d("DatabaseManager", "Is following: " + isFollowing);
                        listener.onCheckFollowingListener(isFollowing);
                    }
                }).addOnFailureListener(e -> {
                    Log.e("DatabaseManager", "Error checking following status: " + e.getMessage(), e);
                    listener.onCheckFollowingListener(false);
                });
    }

    /**
     * Retrieves a list of followers for a given user.
     *
     * @param userId   The ID of the user whose followers are being fetched.
     * @param listener A listener to handle the list of fetched followers.
     */
    @SuppressWarnings("unchecked")
    public void getFollowers(@NotNull String userId, OnFollowersFetchListener listener) {
        DocumentReference currentUserDocRef = this.usersCollectionRef.document(userId);
        ArrayList<Profile> followers = new ArrayList<>();

        currentUserDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot userSnapshot = task.getResult();
                ArrayList<DocumentReference> followerRefs = (ArrayList<DocumentReference>) userSnapshot
                        .get(DocumentReferences.FOLLOWERS.getDocRefString());

                // User does not have any followers
                if (followerRefs == null || followerRefs.isEmpty()) {
                    listener.onFollowersFetched(followers);
                    return;
                }

                int[] remainingFollowers = { followerRefs.size() }; // Track pending follower retrievals

                for (DocumentReference followerRef : followerRefs) {
                    followerRef.get().addOnCompleteListener(followerTask -> {
                        if (followerTask.isSuccessful() && followerTask.getResult().exists()) {
                            followers.add(followerTask.getResult().toObject(Profile.class));
                        }
                        remainingFollowers[0]--;

                        // When all followerRefs are processed, invoke the listener
                        if (remainingFollowers[0] == 0) {
                            listener.onFollowersFetched(followers);
                        }
                    });
                }
            }
        });
    }

    // Post Management

    /**
     * Adds a new post to the Firestore database.
     * The result is returned via the provided listener.
     *
     * @param post     The post object to be added.
     * @param listener The listener that will receive the success result.
     *                 Example Usage:
     *                 DatabaseManager.addPost(post, success -> {
     *                 if (success) {
     *                 System.out.println("Post added successfully.");
     *                 } else {
     *                 System.out.println("Failed to add post.");
     *                 }
     *                 });
     */
    public void addPost(@NotNull MoodPost post, @NotNull String userId, @Nullable OnPostAddedListener listener) {
        String postId = post.getPostID();

        postsCollectionRef.document(postId).set(post)
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post added successfully");
                    if (listener != null) {
                        listener.onPostAdded(true);
                    }
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    if (listener != null) {
                        listener.onPostAdded(false);
                    }
                });

        DocumentReference postDocRef = postsCollectionRef.document(postId);
        DocumentReference userDocRef = usersCollectionRef.document(userId);
        userDocRef.update(DocumentReferences.POSTS.getDocRefString(), FieldValue.arrayUnion(postDocRef));

        if (!post.isPrivate())
            sendPostNotifications(userDocRef, postDocRef);
    }

    /**
     * Updates a post in the Firestore database.
     * The result is returned via the provided listener.
     *
     * @param postId   The ID of the post to update.
     * @param options  A map of fields to update (field names and values).
     * @param listener The listener that will receive the success result.
     *                 Example Usage:
     *                 HashMap<String, Object> updateFields = new HashMap<>();
     *                 updateFields.put("title", "Updated Title");
     *                 DatabaseManager.updatePost(postId, updateFields, success -> {
     *                 if (success) {
     *                 System.out.println("Post updated.");
     *                 }
     *                 });
     */
    public void updatePost(@NotNull String postId, HashMap<String, Object> options,
            @Nullable OnPostUpdatedListener listener) {
        DocumentReference postRef = postsCollectionRef.document(postId);

        postRef.update(options)
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post updated successfully");
                    if (listener != null) {
                        listener.onPostUpdated(true);
                    }
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    if (listener != null) {
                        listener.onPostUpdated(false);
                    }
                });
    }

    /**
     * Fetches all posts from the FireStore database.
     * The result is returned via the provided listener.
     *
     * @param listener The listener that will receive the result (list of posts).
     *                 Example Usage:
     *                 DatabaseManager.getPosts(posts -> {
     *                       for (MoodPost post : posts) {
     *                          System.out.println(post);
     *                       }
     *                    }
     *                 )
     */
    public void getAllPosts(OnPostsFetchListener listener) {
        postsCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<MoodPost> posts = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    posts.add(doc.toObject(MoodPost.class));
                }

                // Sort the posts by dateTime in descending order (most recent first)
                posts.sort((p1, p2) -> p2.getPostedDateTime().compareTo(p1.getPostedDateTime()));

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
     * Fetches all public posts from the users that the given user follows.
     *
     * @param userId   The unique ID of the user whose followed users' posts are to be retrieved.
     * @param listener A callback interface to handle the fetched posts.
     *
     */
    @SuppressWarnings("unchecked")
    public void getAllFollowerPosts(@NotNull String userId, OnPostsFetchListener listener) {
        DocumentReference userDocRef = this.usersCollectionRef.document(userId);
        userDocRef.get().addOnSuccessListener(userDocSnapshot -> {
            if (!userDocSnapshot.exists() || !userDocSnapshot.contains(DocumentReferences.FOLLOWINGS.getDocRefString())) {
                listener.onPostsFetched(new ArrayList<>());
                return;
            }

            List<DocumentReference> followingDocRefs = (List<DocumentReference>) userDocSnapshot.get(DocumentReferences.FOLLOWINGS.getDocRefString());
            List<String> followingUserIds = new ArrayList<>();

            if (followingDocRefs != null) {
                for (DocumentReference ref : followingDocRefs) {
                    followingUserIds.add(ref.getId());
                }
            }

            if (followingUserIds.isEmpty()) {
                listener.onPostsFetched(new ArrayList<>());
                return;
            }

            this.postsCollectionRef
                    .whereIn("profile.userId", followingUserIds)
                    .whereEqualTo("private", false)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<MoodPost> allPosts = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                MoodPost post = doc.toObject(MoodPost.class);
                                allPosts.add(post);
                            }

                            if (allPosts.isEmpty()) {
                                Log.d("DatabaseManager", "No public posts found from followed users.");
                            }

                            allPosts.sort((p1, p2) -> p2.getPostedDateTime().compareTo(p1.getPostedDateTime()));
                            listener.onPostsFetched(allPosts);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("DatabaseManager", "Error fetching posts: " + e.getMessage(), e);
                        listener.onPostsFetched(new ArrayList<>()); // Return empty list on failure
                    });
        });
    }

    /**
     * Fetches all posts from the FireStore database that are labelled as public.
     * The result is returned via the provided listener
     *
     * @param listener The listener that will receive the result (list of posts).
     */
    public void getAllPublicPosts(OnPostsFetchListener listener) {
        Query publicPosts = this.postsCollectionRef.whereEqualTo("private", false);

        publicPosts.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                ArrayList<MoodPost> postList = new ArrayList<>();

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        postList.add(doc.toObject(MoodPost.class));
                    } catch (ClassCastException e) {
                        Log.e("DatabaseManager", "Error casting document to MoodPost", e);
                    }

                }

                if (listener != null) {
                    listener.onPostsFetched(postList);
                }
            } else {
                Log.e("DatabaseManager", "Error fetching posts", task.getException());
                if (listener != null) {
                    listener.onPostsFetched(null);
                }
            }
        });
    }


    /**
     * Fetches all posts from the FireStore database that are labelled as public under one user.
     * The result is returned via the provided listener
     *
     * @param userId The userId (typically the current session user) which to get the private posts of
     * @param listener The listener that will receive the result (list of posts).
     *
     */
    @SuppressWarnings("unchecked")
    public void getUserPublicPosts(@NotNull String userId, OnPostsFetchListener listener) {
        Query userQuery = this.usersCollectionRef.whereEqualTo("userId", userId);

        userQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot userSnapshot = task.getResult();

                if (userSnapshot.isEmpty()) {
                    Log.e("DatabaseManager", "User not found");
                    if (listener != null) {
                        listener.onPostsFetched(null);
                    }
                    return;
                }

                DocumentSnapshot userDoc = userSnapshot.getDocuments().get(0);
                List<DocumentReference> postRefs = (List<DocumentReference>) userDoc.get(DocumentReferences.POSTS.getDocRefString());

                // Fetch posts from the list of DocumentReferences
                List<Task<DocumentSnapshot>> postTasks = new ArrayList<>();
                if (postRefs != null) {
                    for (DocumentReference postRef : postRefs) {
                        postTasks.add(postRef.get());
                    }
                }

                // Wait for all post fetch tasks to complete
                Tasks.whenAllSuccess(postTasks).addOnSuccessListener(posts -> {
                    ArrayList<MoodPost> postList = new ArrayList<>();
                    for (Object obj : posts) {
                        DocumentSnapshot postDoc = (DocumentSnapshot) obj;
                        if (postDoc.exists()) {
                            MoodPost post = postDoc.toObject(MoodPost.class);
                            if (post != null && !post.isPrivate()) {
                                postList.add(post);
                            }
                        }
                    }

                    // Return posts to listener
                    if (listener != null) {
                        listener.onPostsFetched(postList);
                    }
                }).addOnFailureListener(e -> {
                    Log.e("DatabaseManager", "Error fetching posts", e);
                    if (listener != null) {
                        listener.onPostsFetched(null);
                    }
                });

            } else {
                Log.e("DatabaseManager", "Error fetching user", task.getException());
                if (listener != null) {
                    listener.onPostsFetched(null);
                }
            }
        });
    }


    /**
     * Fetches posts from a specific user by their userId.
     * The result is returned via the provided listener.
     *
     * @param userId   The user ID whose posts need to be fetched.
     * @param listener The listener that will receive the result (list of posts).
     *                 Example Usage:
     *                 DatabaseManager.getPosts(userId, postsMap -> {
     *                 if (postsMap != null) {
     *                 // Do what you need here
     *                 }
     *                 });
     */
    @SuppressWarnings("unchecked")
    public void getUserPosts(@NotNull String userId, OnPostsFetchListener listener) {
        DocumentReference userDocRef = usersCollectionRef.document(userId);
        userDocRef.get().addOnSuccessListener(userSnapshot  -> {
            if (!userSnapshot.exists() || !userSnapshot.contains(DocumentReferences.POSTS.getDocRefString())) {
                Log.d("DatabaseManager", "User document does not exist or has no postRefs");
                listener.onPostsFetched(new ArrayList<>());
                return;
            }

            ArrayList<DocumentReference> postRefs = (ArrayList<DocumentReference>) userSnapshot.get(DocumentReferences.POSTS.getDocRefString());
            if (postRefs == null) {
                listener.onPostsFetched(new ArrayList<>());
                return;
            }

            if (postRefs.isEmpty()) {
                Log.d("DatabaseManager", "User document has no postRefs");
                listener.onPostsFetched(new ArrayList<>());
                return;
            }
            AtomicInteger remainingPosts = new AtomicInteger(postRefs.size());
            List<MoodPost> posts = Collections.synchronizedList(new ArrayList<>());

            for (DocumentReference postRef : postRefs) {
                postRef.get().addOnSuccessListener(postSnapshot -> {
                    if (postSnapshot.exists()) {
                        MoodPost moodPost = postSnapshot.toObject(MoodPost.class);
                        if (moodPost != null) {
                            posts.add(moodPost);
                            Log.d("DatabaseManager", "Fetched post: " + moodPost);
                        }
                    } else {
                        Log.d("DatabaseManager", "Post not found: " + postRef.getPath());
                    }

                    if (remainingPosts.decrementAndGet() == 0) {
                        posts.sort((p1, p2) -> p2.getPostedDateTime().compareTo(p1.getPostedDateTime()));
                        listener.onPostsFetched(new ArrayList<>(posts));
                    }
                }).addOnFailureListener(e -> {
                    Log.e("DatabaseManager", "Error fetching post: " + postRef.getPath(), e);
                    if (remainingPosts.decrementAndGet() == 0) {
                        listener.onPostsFetched(new ArrayList<>(posts));
                    }
                });
            }
        });
    }

    /**
     * Fetches posts from multiple users based on their userIds.
     * The result is returned via the provided listener.
     * Once all posts are fetched, the listener will be called with the results.
     *
     * @param userIds  List of user IDs whose posts need to be fetched.
     * @param listener The listener that will receive the result (map of userId to
     *                 their posts).
     *                 Example Usage:
     *                 ArrayList<String> userIds = new ArrayList<>();
     *                 userIds.add("user1");
     *                 userIds.add("user2");
     *                 DatabaseManager.getPosts(userIds, postsMap -> {
     *                 if (postsMap != null) {
     *                 for (String userId : postsMap.keySet()) {
     *                 ArrayList<MoodPost> posts = postsMap.get(userId);
     *                 Log.d("Database", "User " + userId + " has " + posts.size() +
     *                 " posts.");
     *                 }
     *                 }
     *                 });
     */
    @SuppressWarnings("unchecked")
    public void getUsersPosts(@NotNull ArrayList<String> userIds, OnMultipleUsersPostsFetchListener listener) {
        HashMap<String, ArrayList<MoodPost>> postsMap = new HashMap<>();
        int[] remaining = { userIds.size() }; // To track when all user posts are fetched

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
                    int[] postRemaining = { postRefs.size() }; // Track individual post retrieval

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
     * @param postID   The ID of the post to delete.
     * @param listener The listener that will receive the success result.
     *                 Example Usage:
     *                 DatabaseManager.deletePost(postId, success -> {
     *                 if (success) {
     *                 System.out.println("Post deleted.");
     *                 }
     *                 });
     */
    public void deletePost(@NotNull String postID, @NotNull String userId, @Nullable OnPostDeletedListener listener) {
        DocumentReference postDocRef = postsCollectionRef.document(postID);
        DocumentReference userDocRef = usersCollectionRef.document(userId);

        postDocRef.delete()
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post deleted successfully");
                    userDocRef.update(DocumentReferences.POSTS.getDocRefString(), FieldValue.arrayRemove(postDocRef));
                    if (listener != null) {
                        listener.onPostDeleted(true);
                    }
                })
                .addOnFailureListener(e -> {
                    defaultFailureHandler(e);
                    if (listener != null) {
                        listener.onPostDeleted(false);
                    }
                });
    }

    /**
     * Sends a post notification to all followers of a user.
     *
     * @param userDocRef A reference to the Firestore document of the user who created the post.
     * @param postDocRef A reference to the Firestore document of the new post.
     */
    @SuppressWarnings("unchecked")
    private void sendPostNotifications(DocumentReference userDocRef, DocumentReference postDocRef) {
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Object followersRefsObject = documentSnapshot.get(DocumentReferences.FOLLOWERS.getDocRefString());
                if (followersRefsObject instanceof ArrayList<?>) {
                    ArrayList<DocumentReference> followersRef = (ArrayList<DocumentReference>) followersRefsObject;

                    for (DocumentReference followerRef : followersRef) {
                        followerRef.update(DocumentReferences.NOTIFICATION_POSTS.getDocRefString(),
                                FieldValue.arrayUnion(postDocRef));
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

    /**
     * Callback interface for fetching multiple users.
     * Implement this interface to handle the fetched users data.
     */
    public interface OnUserSearchFetchListener {
        void onUsersFetched(List<HashMap<String, Object>> users);
    }

    /**
     * Callback interface for adding a user.
     * Implement this interface to check if successful.
     */
    public interface OnUserAddListener {
        void onUsersAdded(boolean added);
    }

    /**
     * Callback interface for fetching a user's followers.
     * Implement this interface to handle the fetched user data.
     */
    public interface OnFollowersFetchListener {
        void onFollowersFetched(ArrayList<Profile> followers);
    }

    /**
     * Callback interface for fetching posts.
     * Implement this interface to handle the fetched posts.
     */
    public interface OnPostsFetchListener {
        void onPostsFetched(ArrayList<MoodPost> posts);
    }

    /**
     * Callback interface for fetching posts from multiple users.
     * Implement this interface to handle the fetched posts.
     */
    public interface OnMultipleUsersPostsFetchListener {
        void onMultipleUsersPostsFetched(HashMap<String, ArrayList<MoodPost>> postsMap);
    }

    /**
     * Callback interface for fetching notifications from a user.
     * Implement this interface to handle the fetched notifications.
     */
    public interface OnNotificationsFetchListener {
        void onNotificationsFetchListener(ArrayList<MoodPost> posts, ArrayList<HashMap<String, Object>> requests);
    }

    /**
     * Callback interface for checking if a user is following another user.
     * Implement this interface to handle the fetched boolean.
     */
    public interface OnCheckFollowingListener {
        void onCheckFollowingListener(boolean following);
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