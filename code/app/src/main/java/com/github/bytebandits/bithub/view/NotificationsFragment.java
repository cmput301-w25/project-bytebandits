package com.github.bytebandits.bithub.view;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Notification;
<<<<<<< HEAD
=======
import com.github.bytebandits.bithub.model.Profile;
>>>>>>> c307e62 (Notification model added)
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsFragment extends Fragment {
    private ArrayList<MoodPost> dataList;
<<<<<<< HEAD
=======

>>>>>>> c307e62 (Notification model added)
    private ArrayList<Notification> notifications;
    private ListView moodPostList;
    private NotificationArrayAdapter notifAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SessionManager sessionManager;
    private boolean isNotificationCleared = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        sessionManager = SessionManager.getInstance(getContext());

        // Initialize lists
        dataList = new ArrayList<>();
        notifications = new ArrayList<>();

        Button clearNotificationsButton = view.findViewById(R.id.clear_notifications_button);
        clearNotificationsButton.setOnClickListener(v -> clearAllNotifications());

        // Setup initial notification fetch
        fetchNotifications();

        // Setup snapshot listener for real-time updates
        setupSnapshotListener();

        return view;
    }

    private void clearAllNotifications() {
        String userId = sessionManager.getUserId();

        // Clear notifications in Firestore
        DatabaseManager.getInstance().clearAllNotifications(userId);

        // Clear local lists
        notifications.clear();
        dataList.clear();

        // Update UI
        if (notifAdapter != null) {
            notifAdapter.notifyDataSetChanged();
        }

        // Set flag to prevent refetching
        isNotificationCleared = true;

        Log.d("NotificationsFragment", "All notifications cleared");
    }

    private void fetchNotifications() {
        // Skip fetching if notifications were just cleared
        if (isNotificationCleared) {
            isNotificationCleared = false;
            return;
        }

        // Initialize notifications to avoid NullPointerException
        if (notifications == null) {
            notifications = new ArrayList<>();
            Log.d("NotificationsFragment", "notifications initialized as empty list");
        }

        // Initialize notifications to avoid NullPointerException
        if (notifications == null) {
            notifications = new ArrayList<>();
            Log.d("NotificationsFragment", "notifications initialized as empty list");
        }

        executor.execute(() -> {
            DatabaseManager.getInstance().getNotifications(
                    sessionManager.getUserId(),
                    (posts, requests) -> {
                        mainHandler.post(() -> {
                            // Process and filter posts
                            List<MoodPost> uniqueUserPosts = getUniqueUserLatestPosts(posts);
                            List<Notification> newNotifications = latestPosts(uniqueUserPosts, requests);

                            // Update lists
                            dataList.clear();
                            dataList.addAll(uniqueUserPosts);
                            notifications.clear();
                            notifications.addAll(newNotifications);

                            // Setup ListView and adapter
                            setupListView();
                        });
                    }
            );
        });
    }

    private void setupListView() {
        if (getView() == null) return;

        moodPostList = getView().findViewById(R.id.notificationList);

        if (notifications.isEmpty()) {
            // Optional: Show empty state
            return;
        }

        notifAdapter = new NotificationArrayAdapter(getContext(), notifications);
        moodPostList.setAdapter(notifAdapter);

        moodPostList.setOnItemClickListener((parent, view, position, id) -> {
            Notification notification = notifications.get(position);

            if (notification.getPost() != null) {
                DetailedMoodPostFragment detailedMoodPostFragment =
                        DetailedMoodPostFragment.newInstance(notification.getPost());
                detailedMoodPostFragment.show(getParentFragmentManager(), "Detailed Mood Post View");
            }
        });
    }

    private void setupSnapshotListener() {
        DatabaseManager.getInstance().getPostsCollectionRef()
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", error.toString());
                        return;
                    }

                    if (value != null && !isNotificationCleared) {
                        // Convert snapshots to MoodPosts
                        List<MoodPost> allPosts = new ArrayList<>();
                        for (QueryDocumentSnapshot snapshot : value) {
                            allPosts.add(snapshot.toObject(MoodPost.class));
                        }

                        // Process and filter posts
                        List<Notification> newNotifications = latestPosts(allPosts, new ArrayList<>());
                        List<MoodPost> uniqueUserPosts = getUniqueUserLatestPosts(allPosts);

                        mainHandler.post(() -> {
                            dataList.clear();
                            dataList.addAll(uniqueUserPosts);
                            notifications.clear();
                            notifications.addAll(newNotifications);

                            if (notifAdapter != null) {
                                notifAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
    }


    private List<Notification> latestPosts(List<MoodPost> uniqueUserPosts, ArrayList<HashMap<String, Object>> requests) {

        List<Notification> notifications = new ArrayList<>();
        for (HashMap<String, Object> request : requests) {
            Notification notification = new Notification();
            notification.setRequest(request);
            notifications.add(notification);
        }
        for (MoodPost post : uniqueUserPosts) {
            Notification notification = new Notification();
            notification.setMoodPost(post);
            notifications.add(notification);
        }
        return notifications;
    }

    private List<Notification> latestPosts(List<MoodPost> uniqueUserPosts) {
        List<Notification> notifications = new ArrayList<>();
        for (MoodPost post : uniqueUserPosts) {
            Notification notification = new Notification();
            notification.setMoodPost(post);
            notifications.add(notification);
        }
        return notifications;
    }

    private List<Notification> latestPosts(List<MoodPost> uniqueUserPosts) {
        List<Notification> notifications = new ArrayList<>();
        for (MoodPost post : uniqueUserPosts) {
            Notification notification = new Notification();
            notification.setMoodPost(post);
            notifications.add(notification);
        }
        return notifications;
    }

    private List<MoodPost> getUniqueUserLatestPosts(List<MoodPost> posts) {

        String userId = sessionManager.getUserId();
        // Use a LinkedHashMap to maintain order and uniqueness
        Map<String, MoodPost> uniqueUserPosts = new LinkedHashMap<>();

        // Sort posts by date in descending order (most recent first)
        List<MoodPost> sortedPosts = new ArrayList<>(posts);
        sortedPosts.sort((p1, p2) -> p2.getPostedDateTime().compareTo(p1.getPostedDateTime()));

        // Iterate through sorted posts and keep only the first (latest) post for each user
        for (MoodPost post : sortedPosts) {
            String postUserId = post.getProfile().getUserId();

            // Only add if this user's post is not already in the map
            if (!uniqueUserPosts.containsKey(postUserId) && !postUserId.equals(userId)) {
                uniqueUserPosts.put(postUserId, post);
            }
        }

        // Return the list of unique user posts
        return new ArrayList<>(uniqueUserPosts.values());
    }
}