package com.github.bytebandits.bithub.view;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsFragment extends Fragment {
    private ArrayList<MoodPost> dataList;
    private ListView moodPostList;
    private NotificationArrayAdapter notifAdapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private SessionManager sessionManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        sessionManager = SessionManager.getInstance(getContext());

        // Initialize dataList to avoid NullPointerException
        if (dataList == null) {
            dataList = new ArrayList<>();
            Log.d("NotificationsFragment", "dataList initialized as empty list");
        } else {
            Log.d("NotificationsFragment", "dataList already initialized with size: " + dataList.size());
        }

        executor.execute(() -> {
            DatabaseManager.getInstance().getAllPublicPosts(posts -> {
                if (posts == null) {
                    Log.e("NotificationsFragment", "Error: notifications is null");
                }

                Log.d("NotificationsFragment", "Fetched notifications count: " + posts.size());

                // Process and filter posts
                List<MoodPost> uniqueUserPosts = getUniqueUserLatestPosts(posts);

                // Switch to UI thread for UI updates
                mainHandler.post(() -> {
                    dataList.clear();
                    dataList.addAll(uniqueUserPosts);

                    if (notifAdapter != null) {
                        notifAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("NotificationsFragment", "notificationAdapter is null");
                    }

                    // Initialize views and adapters
                    moodPostList = view.findViewById(R.id.notificationList);
                    if (dataList.size() > 0) {
                        notifAdapter = new NotificationArrayAdapter(getContext(), dataList);
                        moodPostList.setAdapter(notifAdapter);

                        // on item click on list, open detailed view of post
                        moodPostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                DetailedMoodPostFragment detailedMoodPostFragment =
                                        DetailedMoodPostFragment.newInstance(dataList.get(position));
                                detailedMoodPostFragment.show(getActivity().getSupportFragmentManager(), "Detailed Mood Post View");
                            }
                        });
                    }
                });
                return null;
            });
        });

        // Listener so that dataList gets updated whenever the database does
        CollectionReference moodPostRef = DatabaseManager.getInstance().getPostsCollectionRef();
        moodPostRef.addSnapshotListener((value, error) -> {
            if (error != null){
                Log.e("Firestore", error.toString());
            }
            if (value != null){
                Log.d("Firestore", "SnapshotListener triggered, updating dataList");
                // Convert snapshots to MoodPosts
                List<MoodPost> allPosts = new ArrayList<>();
                for (QueryDocumentSnapshot snapshot : value) {
                    allPosts.add(snapshot.toObject(MoodPost.class));
                }

                // Process and filter posts
                Log.d("NotificationsFragment", "Prior to unique user latest post call: " + allPosts.size());
                List<MoodPost> uniqueUserPosts = getUniqueUserLatestPosts(allPosts);

                dataList.clear();
                dataList.addAll(uniqueUserPosts);

                if (notifAdapter != null) {
                    notifAdapter.notifyDataSetChanged();
                }
            }
        });



        return view;
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