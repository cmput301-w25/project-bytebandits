package com.github.bytebandits.bithub.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.PostFilterManager;
import com.github.bytebandits.bithub.model.Profile;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This fragment represents the user's profile screen in the Bithub application.
 * It is responsible for:
 * - Displaying the profile layout.
 * - Providing access to the settings dialog via a settings button.
 */

public class ProfileFragment extends Fragment implements FilterDialog.FilterListener {
    private ArrayList<MoodPost> dataList;
    private ArrayList<MoodPost> filteredDataList;
    private ListView moodPostListHistory;
    private MoodPostArrayAdapter moodPostAdapter;
    private ImageButton settingsButton;
    private ImageButton filterButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Profile profile;
    private TextView usernameTextView;
    private ImageButton followingButton;
    private static final String PROFILE = "profile";


    /**
     * Creates a new instance of ProfileFragment with the given profile.
     *
     * @param profile The Profile object to be displayed in the fragment.
     * @return A new instance of ProfileFragment.
     */
    public static ProfileFragment newInstance(Profile profile) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(PROFILE, profile);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           used to inflate views.
     * @param container          the parent view that the fragment's UI should
     *                           attach to.
     * @param savedInstanceState this fragment is being re-constructed from a
     *                           previous saved state.
     * @return The View for the fragment's UI
     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false); // display profile fragment layout
        profile = (Profile) getArguments().getSerializable(PROFILE);
        followingButton = view.findViewById(R.id.follow_button);
        settingsButton = view.findViewById(R.id.settings_button);
        filterButton = view.findViewById(R.id.filter_button);
        usernameTextView = view.findViewById(R.id.username_textview);

        String userId = profile.getUserId();
        String loggedInUser = SessionManager.getInstance(requireContext()).getUserId();

        usernameTextView.setText(userId);

        // Hide settings button if viewing another user's profile
        // Show following button if viewing another user's profile
        if (!userId.equalsIgnoreCase(loggedInUser)) {
            settingsButton.setVisibility(View.GONE);
            filterButton.setVisibility(View.GONE);
            followingButton.setVisibility(View.VISIBLE);
            followingButton.setOnClickListener(v -> sendFollowRequest());
        } else {
            settingsButton.setVisibility(View.VISIBLE);
            filterButton.setVisibility(View.VISIBLE);
            followingButton.setVisibility(View.GONE);
            settingsButton.setOnClickListener(v -> openSettings());
        }

        filterButton.setOnClickListener(v -> openFilterDialog());

        // Initialize both dataList and filteredDataList to avoid NullPointerException
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        if (filteredDataList == null) {
            filteredDataList = new ArrayList<>();
        }

        executor.execute(() -> {
            String userIdToBeRendered;
            String localUserId = SessionManager.getInstance(requireContext()).getUserId();

            if (userId.equals(localUserId)) {
                userIdToBeRendered = localUserId;  // render the current user
            } else {
                userIdToBeRendered = userId;   // render another user
            }

            DatabaseManager.getInstance().getUserPosts(userIdToBeRendered, posts -> {
                if (posts == null) {
                    Log.e("ProfileFragment", "Error: posts is null");
                    return null;
                }

                Log.d("ProfileFragment", "Fetched posts count: " + posts.size());

                // Switch to UI thread for UI updates
                mainHandler.post(() -> {
                    dataList.clear();
                    dataList.addAll(posts);
                    filteredDataList.clear();
                    filteredDataList.addAll(posts);

                    if (moodPostAdapter != null) {
                        moodPostAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("ProfileFragment", "moodPostAdapter is null");
                    }

                    // Initialize views and adapters
                    moodPostListHistory = view.findViewById(R.id.mood_post_list_history);
                    moodPostAdapter = new MoodPostArrayAdapter(getContext(), filteredDataList);
                    moodPostListHistory.setAdapter(moodPostAdapter);

                    // open detailed view of the user's clicked post
                    moodPostListHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
                            DetailedMoodPostFragment detailedMoodPostFragment = DetailedMoodPostFragment
                                    .newInstance(dataList.get(position));
                            detailedMoodPostFragment.show(getActivity().getSupportFragmentManager(),
                                    "Detailed Mood Post View");
                        }
                    });
                });
                return null;
            });
        });

        // Listener to update dataList and filteredDataList whenever the database
        // changes
        CollectionReference moodPostRef = DatabaseManager.getInstance().getPostsCollectionRef();
        moodPostRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {
                dataList.clear();
                filteredDataList.clear();
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        MoodPost post = snapshot.toObject(MoodPost.class);
                        dataList.add(post);
                    }
                    filteredDataList.addAll(dataList); // Copy the entire list to filteredDataList
                }
                if (moodPostAdapter != null) {
                    moodPostAdapter.notifyDataSetChanged();
                }
            }
        });
        return view;
    }

    /**
     * Opens the settings dialog for the user.
     * This method is only accessible when the user is viewing their own profile.
     */
    private void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(requireContext());
        settingsDialog.showSettingsDialog();
    }

    /**
     * Displays the filter dialog when the settings button is clicked.
     */
    private void openFilterDialog() {
        FilterDialog filterDialog = new FilterDialog(requireContext(), this);
        filterDialog.showFilterDialog();
    }

    /**
     * Handles the filter selection event from the filter dialog.
     * Updates the list of displayed mood posts based on the selected mood filter.
     *
     * @param mood The mood selected by the user for filtering.
     */
    @Override
    public void onFilterSelected(String mood) {
        Log.d("ProfileFragment", "Filter selected: " + mood);
        filteredDataList.clear();
        if (mood.equals("last_week")) {
            filteredDataList.addAll(filterPostsFromLastWeek(dataList));
        } else {
            filteredDataList.addAll(PostFilterManager.filterPostsByMood(dataList, mood));
        }
        if (moodPostAdapter != null) {
            moodPostAdapter.notifyDataSetChanged();
        } else {
            Log.e("Profile fragment","Mood Post adapter is null in profile fragment");
        }
    }

    /**
     * Filters the list of mood posts to include only those that are recent (last 7
     * days).
     *
     * @param posts The full list of mood posts.
     * @return A list of mood posts from the last week.
     */
    private List<MoodPost> filterPostsFromLastWeek(List<MoodPost> posts) {
        List<MoodPost> filteredPosts = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long oneWeekInMillis = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds

        for (MoodPost post : posts) {
            long postTime = post.getPostedDateTime().getTime();
            if ((currentTime - postTime) <= oneWeekInMillis) {
                filteredPosts.add(post);
            }
        }
        return filteredPosts;
    }

    /**
     * Updates the displayed list of mood posts based on the user's search query.
     *
     * @param query The search query entered by the user.
     */
    @Override
    public void onSearchQueryChanged(String query) {
        filteredDataList.clear();

        if (query.isEmpty()) {
            filteredDataList.addAll(dataList);
        } else {
            for (MoodPost post : dataList) {
                if (post.getDescription() != null
                        && post.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredDataList.add(post);
                }
            }
        }
        moodPostAdapter.notifyDataSetChanged();
    }

    public void sendFollowRequest() {

    }
}
