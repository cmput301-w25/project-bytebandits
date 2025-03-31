package com.github.bytebandits.bithub.view;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.DocumentReferences;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Notification;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents the notifications fragment
 */
public class NotificationsFragment extends Fragment {
    private ArrayList<MoodPost> dataList;
    private ArrayList<Notification> notifications;
    private ListView moodPostList;
    private NotificationArrayAdapter notifAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SessionManager sessionManager;
    private boolean isNotificationCleared = false;

    /**
     * Creates a new instance of the fragment
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI, or null.
     */
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

    /**
     * Clears all notifications
     */
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

    /**
     * Fetches notifications from the database
     */
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
                            List<Notification> newNotifications = latestPosts(posts, requests);

                            // Update lists
                            dataList.clear();
                            dataList.addAll(posts);
                            notifications.clear();
                            notifications.addAll(newNotifications);

                            // Setup ListView and adapter
                            setupListView();
                        });
                    }
            );
        });
    }

    /**
     * Sets up the ListView and adapter
     */
    private void setupListView() {
        if (getView() == null) return;

        moodPostList = getView().findViewById(R.id.notificationList);

        if (notifications.isEmpty()) {
            // Empty state
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

    /**
     * Sets up a snapshot listener for real-time updates
     */
    private void setupSnapshotListener() {
        DatabaseManager.getInstance().getUsersCollectionRef()
                .document(SessionManager.getInstance(requireContext()).getUserId())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Snapshot listener error: " + error.toString());
                        return;
                    }

                    if (value != null && !isNotificationCleared) {
                        ArrayList<DocumentReference> postRefs = (ArrayList<DocumentReference>) value.get(DocumentReferences.NOTIFICATION_POSTS.getDocRefString());
                        ArrayList<DocumentReference> requestRefs = (ArrayList<DocumentReference>) value.get(DocumentReferences.NOTIFICATION_REQS.getDocRefString());

                        if (postRefs == null && requestRefs == null) {
                            Log.d("Firestore", "No notifications found");
                            return;
                        }

                        if (postRefs == null) postRefs = new ArrayList<>();
                        if (requestRefs == null) requestRefs = new ArrayList<>();


                        List<MoodPost> allPosts = new ArrayList<>();
                        ArrayList<HashMap<String, Object>> requests = new ArrayList<>();

                        // Fetch posts asynchronously
                        for (DocumentReference postRef : postRefs) {
                            postRef.get()
                                    .addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            allPosts.add(doc.toObject(MoodPost.class));
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Log.e("Firestore", "Failed to fetch post: " + e.getMessage()));
                        }

                        // Fetch requests asynchronously
                        for (DocumentReference userRef : requestRefs) {
                            userRef.get()
                                    .addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            requests.add((HashMap<String, Object>) doc.getData());
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Log.e("Firestore", "Failed to fetch request: " + e.getMessage()));
                        }

                        // After all async fetches, update UI
                        mainHandler.postDelayed(() -> {
                            List<Notification> newNotifications = latestPosts(allPosts, requests);

                            dataList.clear();
                            dataList.addAll(allPosts);
                            notifications.clear();
                            notifications.addAll(newNotifications);

                            if (notifAdapter != null) {
                                notifAdapter.notifyDataSetChanged();
                            }
                        }, 500); // Delay to allow async calls to complete
                    }
                });
    }

    /**
     * Latest posts
     * @param posts List of posts
     * @param requests List of requests
     * @return List of notifications
     */
    private List<Notification> latestPosts(List<MoodPost> posts, ArrayList<HashMap<String, Object>> requests) {

        List<Notification> notifications = new ArrayList<>();
        for (HashMap<String, Object> request : requests) {
            Notification notification = new Notification();
            notification.setRequest(request);
            notifications.add(notification);
        }
        for (MoodPost post : posts) {
            Notification notification = new Notification();
            notification.setMoodPost(post);
            notifications.add(notification);
        }
        return notifications;
    }

}