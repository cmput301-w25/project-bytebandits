package com.github.bytebandits.bithub.controller;

import android.util.Log;

import com.github.bytebandits.bithub.model.DocumentReferences;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Profile;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.inject.Singleton;

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

    /**
     * Adds a new user to the Firestore database.
     *
     * @param userId      The unique identifier for the user.
     * @param userDetails A HashMap containing user details to be stored.
     * @param listener    An optional listener to handle success or failure
     *                    callbacks.
     */
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

    /**
     * Searches for users whose userId matches or starts with the given query.
     *
     * @param query    The search term used to find users.
     * @param listener A listener to handle the searched users
     * @throws ExecutionException   If an error occurs while executing the query.
     * @throws InterruptedException If the execution is interrupted.
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

        Log.d("DatabaseManager", "searchUsers() execution finished, waiting for Firestore response...");
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
    public void sendNotification(String recipientUserId, DocumentReference docRef, DocumentReferences type) {
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
    public void getNotifications(String userId, OnNotificationsFetchListener listener) {
        DocumentReference userDocRef = this.usersCollectionRef.document(userId);
        userDocRef.get().addOnSuccessListener(userDocSnapshot -> {
            if (!userDocSnapshot.exists()) {
                Log.d("DatabaseManager", "User document does not exist.");
                return;
            }
            ArrayList<MoodPost> postNotifications = new ArrayList<>();
            ArrayList<HashMap<String, Object>> requestNotifications = new ArrayList<>();

            // Fetch post notifications
            if (userDocSnapshot.contains(DocumentReferences.NOTIFICATION_POSTS.getDocRefString())) {
                List<DocumentReference> postRefs =
                        (List<DocumentReference>) userDocSnapshot.get(DocumentReferences.NOTIFICATION_POSTS.getDocRefString());

                for (DocumentReference postRef : postRefs) {
                    postRef.get().addOnSuccessListener(postDoc -> {
                        if (postDoc.exists()) {
                            MoodPost post = postDoc.toObject(MoodPost.class);
                            postNotifications.add(post);
                            Log.d("DatabaseManager", "Post notification fetched: " + post.toString());
                        }
                    }).addOnFailureListener(e -> Log.e("DatabaseManager", "Error fetching post: " + e.getMessage(), e));
                }
            } else {
                Log.d("DatabaseManager", "No post notifications found.");
            }

            // Fetch request notifications
            if (userDocSnapshot.contains(DocumentReferences.NOTIFICATION_REQS.getDocRefString())) {
                List<DocumentReference> userRefs =
                        (List<DocumentReference>) userDocSnapshot.get(DocumentReferences.NOTIFICATION_REQS.getDocRefString());
                Log.d("DatabaseManager", "Request notifications: " + userRefs);

                for (DocumentReference userRef : userRefs) {
                    userRef.get().addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            HashMap<String, Object> user = (HashMap<String, Object>) userDoc.getData();
                            requestNotifications.add(user);
                        }
                    });
                }
            } else {
                Log.d("DatabaseManager", "No request notifications found.");
            }

            listener.onNotificationsFetchListener(postNotifications, requestNotifications);
        }).addOnFailureListener(e -> {
            Log.e("DatabaseManager", "Error fetching notifications: " + e.getMessage(), e);
        });
    }

    /**
     * Remove a post notification from user.
     *
     * @param userId current userId logged in.
     * @param postId postId to remove the notification.
     */
    public void deletePostNotification(String userId, String postId) {
        DocumentReference postDocRef = this.postsCollectionRef.document(postId);
        DocumentReference userDocRef = this.usersCollectionRef.document(userId);

        userDocRef.update(DocumentReferences.NOTIFICATION_POSTS.getDocRefString(), FieldValue.arrayRemove(postDocRef));
    }


    /**
     * Sends a follow request to a specific user.
     *
     * @param currentUserId   The ID of the current user.
     * @param requestedUserId The ID of the user that the request is for.
     *
     */
    public void sendFollowRequest(String currentUserId, String requestedUserId) {
        DocumentReference currentDocRef = this.usersCollectionRef.document(currentUserId);
        this.sendNotification(requestedUserId, currentDocRef, DocumentReferences.NOTIFICATION_REQS);

        // TODO: REMOVE LATER
//         this.acceptUserFollow(requestedUserId, currentUserId);
    }

    /**
     * Accepts a follow request from another user.
     *
     * @param currentUserId   The ID of the current user.
     * @param requestedUserId The ID of the user requesting to follow.
     */
    public void acceptUserFollow(String currentUserId, String requestedUserId) {
        DocumentReference requestedUserDocRef = this.usersCollectionRef.document(requestedUserId);
        DocumentReference currentUserDocRef = this.usersCollectionRef.document(currentUserId);

        // Add the requesting user document reference to your followers
        currentUserDocRef.update(DocumentReferences.FOLLOWERS.getDocRefString(),
                FieldValue.arrayUnion(requestedUserDocRef));
        currentUserDocRef.update(DocumentReferences.NOTIFICATION_REQS.getDocRefString(),
                FieldValue.arrayRemove(requestedUserDocRef));

        // Add current user document reference to requesting's followings
        requestedUserDocRef.update(DocumentReferences.FOLLOWINGS.getDocRefString(),
                FieldValue.arrayUnion(currentUserDocRef));
    }

    /**
     * Rejects a follow request from another user.
     *
     * @param currentUserId   The ID of the current user.
     * @param requestedUserId The ID of the user whose request is being rejected.
     */
    public void rejectUserFollow(String currentUserId, String requestedUserId) {
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
    public void unfollowUser(String currentUserId, String targetUserId) {
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
    public void checkFollowing(String currentUserId, String checkUserId, OnCheckFollowingListener listener) {
        DocumentReference currentUserDocRef = this.usersCollectionRef.document(currentUserId);
        DocumentReference targetUserDocRef = this.usersCollectionRef.document(checkUserId);

        currentUserDocRef.get()
                .addOnSuccessListener(userDocSnapshot -> {
                    if (!userDocSnapshot.exists() || !userDocSnapshot.contains(DocumentReferences.FOLLOWINGS.getDocRefString())) {
                        Log.d("DatabaseManager", "User document does not exist or has no followings.");
                        listener.onCheckFollowingListener(false);
                        return;
                    } else {
                        List<DocumentReference> followings = (List<DocumentReference>) userDocSnapshot.get(DocumentReferences.FOLLOWINGS.getDocRefString());
                        boolean isFollowing = followings.stream().anyMatch(ref -> ref.getId().equals(checkUserId));

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
    public void getFollowers(String userId, OnFollowersFetchListener listener) {
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
     *
     *                 Example Usage:
     *                 DatabaseManager.addPost(post, success -> {
     *                 if (success) {
     *                 System.out.println("Post added successfully.");
     *                 } else {
     *                 System.out.println("Failed to add post.");
     *                 }
     *                 });
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
        userDocRef.update(DocumentReferences.POSTS.getDocRefString(), FieldValue.arrayUnion(postDocRef));

        sendPostNotifications(userDocRef, postDocRef);
    }

    /**
     * Updates a post in the Firestore database.
     * The result is returned via the provided listener.
     *
     * @param postId   The ID of the post to update.
     * @param options  A map of fields to update (field names and values).
     * @param listener The listener that will receive the success result.
     *
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
            Optional<OnPostUpdatedListener> listener) {
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

    public void getAllFollowerPosts(String userId, OnPostsFetchListener listener) {
        DocumentReference userDocRef = this.usersCollectionRef.document(userId);
        userDocRef.get().addOnSuccessListener(userDocSnapshot -> {
            if (!userDocSnapshot.exists() || !userDocSnapshot.contains(DocumentReferences.FOLLOWINGS.getDocRefString())) {
                listener.onPostsFetched(new ArrayList<>());
                return;
            }

            List<DocumentReference> followingDocRefs = (List<DocumentReference>) userDocSnapshot.get(DocumentReferences.FOLLOWINGS.getDocRefString());
            List<String> followingUserIds = new ArrayList<>();

            for (DocumentReference ref : followingDocRefs) {
                followingUserIds.add(ref.getId());
            }

            Log.d("DatabaseManager", "Following user IDs: " + followingUserIds);

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
                                Log.d("DatabaseManager", "Post found: " + post.toString());
                                allPosts.add(post);
                            }

                            if (allPosts.isEmpty()) {
                                Log.d("DatabaseManager", "No public posts found from followed users.");
                            }
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
    public void getUserPublicPosts(String userId, OnPostsFetchListener listener) {
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
                for (DocumentReference postRef : postRefs) {
                    postTasks.add(postRef.get());
                }

                // Wait for all post fetch tasks to complete
                Tasks.whenAllSuccess(postTasks).addOnSuccessListener(posts -> {
                    ArrayList<MoodPost> postList = new ArrayList<>();
                    for (Object obj : posts) {
                        DocumentSnapshot postDoc = (DocumentSnapshot) obj;
                        if (postDoc.exists()) {
                            MoodPost post = postDoc.toObject(MoodPost.class);
                            if (!post.isPrivate()) {
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
    public void getUserPosts(@NotNull String userId, OnPostsFetchListener listener) {
        ArrayList<MoodPost> posts = new ArrayList<>();
        usersCollectionRef.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot userSnapshot = task.getResult();
                Object postRefsObject = userSnapshot.get("postRefs");

                List<DocumentReference> postRefs = new ArrayList<>();

                if (postRefsObject instanceof List) {
                    List<?> rawList = (List<?>) postRefsObject;
                    for (Object item : rawList) {
                        if (item instanceof DocumentReference) {
                            postRefs.add((DocumentReference) item);
                        } else if (item instanceof String) {
                            // Convert string to DocumentReference
                            postRefs.add(firestoreDb.document((String) item));
                        } else {
                            Log.e("getUserPosts", "Unexpected item in postRefs: " + item);
                        }
                    }
                }

                if (postRefs.isEmpty()) {
                    Log.d("getUserPosts", "postRefs is empty");
                    listener.onPostsFetched(posts);
                    return;
                } else {
                    Log.d("getUserPosts", "postRefs size: " + postRefs.size());
                    for (DocumentReference ref : postRefs) {
                        Log.d("getUserPosts", "postRef: " + ref.getPath());
                    }
                }

                int[] postRemaining = { postRefs.size() }; // Track individual post retrieval

                for (DocumentReference postRef : postRefs) {
                    postRef.get().addOnCompleteListener(postTask -> {
                        if (postTask.isSuccessful() && postTask.getResult().exists()) {
                            MoodPost moodPost = postTask.getResult().toObject(MoodPost.class);
                            posts.add(moodPost);
                            Log.d("getUserPosts", "Fetched post: " + moodPost);
                        } else {
                            Log.d("getUserPosts", "Failed to fetch post: " + postRef.getPath());
                        }
                        postRemaining[0]--;

                        // When all postRefs are processed, update the map
                        if (postRemaining[0] == 0) {
                            Log.d("getUserPosts", "All posts fetched, returning list");
                            listener.onPostsFetched(posts);
                        }
                    });
                }
            } else {
                Log.e("getUserPosts", "Failed to fetch user document", task.getException());
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
     *
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
     *
     *                 Example Usage:
     *                 DatabaseManager.deletePost(postId, success -> {
     *                 if (success) {
     *                 System.out.println("Post deleted.");
     *                 }
     *                 });
     */
    public void deletePost(@NotNull String postID, @NotNull String userId, Optional<OnPostDeletedListener> listener) {
        DocumentReference postDocRef = postsCollectionRef.document(postID.toString());
        DocumentReference userDocRef = usersCollectionRef.document(userId);

        postDocRef.delete()
                .addOnSuccessListener(unused -> {
                    defaultSuccessHandler("Post deleted successfully");
                    userDocRef.update(DocumentReferences.POSTS.getDocRefString(), FieldValue.arrayRemove(postDocRef));
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

    private void addComment(String postId, String userId, String comment) {
        postsCollectionRef.document(postId).update("comments", FieldValue.arrayUnion(Map.entry(userId, comment)));
    }

    private void deleteComment(String postId, Map.Entry<String, String> comment) {
        postsCollectionRef.document(postId).update("comments", FieldValue.arrayRemove(comment));
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