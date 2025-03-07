package com.github.bytebandits.bithub;

import android.util.Log;
import java.util.*;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.auth.User;

import org.jetbrains.annotations.NotNull;

public final class Database {
    private static final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private static CollectionReference usersCollectionRef;
    private static CollectionReference postsCollectionRef;

    public static void init() {
        usersCollectionRef = firestoreDB.collection("users");
        postsCollectionRef = firestoreDB.collection("posts");
    }

    // Default success and failure handlers
    private static <T> void defaultSuccessHandler(T result) {
        Log.d("Database", "Operation successful: " + result);
    }

    private static void defaultFailureHandler(Exception e) {
        Log.e("Database", "Operation failed", e);
    }

    public static HashMap<String, Object> getUser(String userId) {
        usersCollectionRef.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
            }
        });


    }

    // Fetch all posts (Asynchronous)
    public static ArrayList<MoodPost> getPosts() {
        ArrayList<MoodPost> posts = new ArrayList<>();

        postsCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    posts.add(doc.toObject(MoodPost.class));
                }
            } else {
                defaultFailureHandler(task.getException());
            }
        });

        return posts;
    }

    // Fetch posts by a specific user UUID
    public static void getPosts(@NotNull String userId) {
        ArrayList<MoodPost> posts = new ArrayList<>();
        postsCollectionRef.document(userId).collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    posts.add(doc.toObject(MoodPost.class));
                }
                defaultSuccessHandler(posts);
            } else {
                defaultFailureHandler(task.getException());
            }
        });
    }

    // Fetch multiple users' posts
    public static HashMap<UUID, ArrayList<MoodPost>> getPosts(@NotNull ArrayList<String> userIds) {
        HashMap<String, ArrayList<MoodPost>> postsMap = new HashMap<>();

        for (String userId : userIds) {
            postsCollectionRef.document(userId).collection("posts").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ArrayList<MoodPost> posts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        posts.add(doc.toObject(MoodPost.class));
                    }
                    postsMap.put(userId, posts);

                    if (postsMap.size() == userIds.size()) {
                        defaultSuccessHandler(postsMap);
                    }
                } else {
                    defaultFailureHandler(task.getException());
                }
            });
        }
    }


    public static void addPost(@NotNull MoodPost post) {
        String postId = post.getId().toString();

        postsCollectionRef.document(postId).set(post)
                .addOnSuccessListener(unused -> defaultSuccessHandler("Post added successfully"))
                .addOnFailureListener(Database::defaultFailureHandler);

        // set the post reference (document reference) to user id's list of posts
        DocumentReference postDocRef = postsCollectionRef.document(postId);

        // mock getting user id from session manager
        // String userId = SessionManager.getCurrentProfile().getUserId();
        String userId = "mock";

        DocumentReference userDocRef = usersCollectionRef.document(userId);
        userDocRef.update("postRefs", FieldValue.arrayUnion(postDocRef));

        Database.sendPostNotifications(userDocRef, postDocRef);
    }

    private static void sendPostNotifications(DocumentReference userDocRef, DocumentReference postDocRef){
        DocumentSnapshot userDocSnapshot = userDocRef.get().getResult();
        if (userDocSnapshot.exists()) {
            Object followersRefsObject = userDocSnapshot.get("followersRefs");
            if (followersRefsObject instanceof ArrayList<?>) {
                ArrayList<DocumentReference> followersRef = (ArrayList<DocumentReference>) followersRefsObject;

                for (DocumentReference followerRef : followersRef) {
                    followerRef.update("notificationsRef", FieldValue.arrayUnion(postDocRef));
                }
            }
        }
    }

    // UUID postId, HashMap<String, Object> options
    public static void updatePost(@NotNull UUID postId, HashMap<String, Object> options) {
        DocumentReference postRef = postsCollectionRef.document(postId.toString());

        postRef.update(options)
                .addOnSuccessListener(unused -> defaultSuccessHandler("Post updated successfully"))
                .addOnFailureListener(Database::defaultFailureHandler);
    }

    public static void deletePost(@NotNull UUID postID) {
//        String userId = MainActivity.getCurrentProfile().getUserId();

        postsCollectionRef.document(postID.toString()).delete()
                .addOnSuccessListener(unused -> defaultSuccessHandler("Post deleted successfully"))
                .addOnFailureListener(Database::defaultFailureHandler);


        // get postIds from user
//        usersRef.document(userId).

    }
}
