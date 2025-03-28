package com.github.bytebandits.bithub.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.MainActivity;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.PostFilterManager;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.model.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment representing the homepage.
 * This fragment displays a list of mood posts and provides filtering
 * functionality.
 */
public class HomepageFragment extends Fragment implements FilterDialog.FilterListener {
    private ArrayList<MoodPost> dataList;
    private ArrayList<MoodPost> filteredDataList; // Separate list for filtering
    private ListView moodPostList;
    private MoodPostArrayAdapter moodPostAdapter;
    private ImageButton filterButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Initializes UI components and fetches posts from the database.
     *
     * @param inflater           The LayoutInflater object that can be used to
     *                           inflate any views in the fragment.
     * @param container          this is the parent view that the fragment's UI
     *                           should be attached to.
     * @param savedInstanceState this fragment is being re-constructed from a
     *                           previous saved state.
     * @return The View for the fragment’s UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepage_fragment, container, false);

        filterButton = view.findViewById(R.id.filter_button_homepage);
        filterButton.setOnClickListener(v -> openFilterDialog());

        // Initialize lists
        if (dataList == null) {
            dataList = new ArrayList<>();
            filteredDataList = new ArrayList<>();
            Log.d("HomepageFragment", "dataList initialized as empty list");
        } else {
            Log.d("HomepageFragment", "dataList already initialized with size: " + dataList.size());
        }

        executor.execute(() -> {
                DatabaseManager.getInstance().getAllPublicPosts(posts -> {
                    if (posts == null) {
                        Log.e("HomepageFragment", "Error: posts is null");
                    }

                Log.d("HomepageFragment", "Fetched posts count: " + posts.size());

                // Switch to UI thread for UI updates
                mainHandler.post(() -> {
                    dataList.clear();
                    dataList.addAll(posts);

                    // Reset filteredDataList to show all posts initially
                    filteredDataList.clear();
                    filteredDataList.addAll(dataList);

                    if (moodPostAdapter != null) {
                        moodPostAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("HomepageFragment", "moodPostAdapter is null");
                    }

                    // Initialize views and adapters
                    moodPostList = view.findViewById(R.id.homepageMoodPostList);
                    if (!filteredDataList.isEmpty()) {
                        moodPostAdapter = new MoodPostArrayAdapter(getContext(), filteredDataList);
                        moodPostList.setAdapter(moodPostAdapter);

                        // on item click on list, open detailed view of post
                        moodPostList.setOnItemClickListener((parent, v, position, id) -> {
                            DetailedMoodPostFragment detailedMoodPostFragment = DetailedMoodPostFragment
                                    .newInstance(filteredDataList.get(position));
                            detailedMoodPostFragment.show(getActivity().getSupportFragmentManager(),
                                    "Detailed Mood Post View");
                        });
                    }
                });
                return null;
            });
        });

        // Firestore listener to update lists when database changes
        CollectionReference moodPostRef = DatabaseManager.getInstance().getPostsCollectionRef();
        moodPostRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {
                Log.d("Firestore", "SnapshotListener triggered, updating dataList");
                dataList.clear();
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        dataList.add(snapshot.toObject(MoodPost.class));
                    }
                }
                // Reset filteredDataList to reflect new data
                filteredDataList.clear();
                filteredDataList.addAll(dataList);

                if (moodPostAdapter != null) {
                    moodPostAdapter.notifyDataSetChanged();
                }
            }
        });

        profileSearchManager(view);

        return view;
    }

    /**
     * Manages and encapsulates all logic relating to searching profiles within the
     * homepage fragment
     * 
     * @param view the view in question, so the method can reference the various UI
     *             elements of the layout
     */
    private void profileSearchManager(View view) {
        ArrayList<Profile> profiles = new ArrayList<>();
        ProfileSearchAdapter profileSearchAdapter = new ProfileSearchAdapter(requireContext(), profiles);

        SearchView profileSearch = view.findViewById(R.id.profileSearch);
        ListView profileResults = view.findViewById(R.id.profileResults);
        profileResults.setAdapter(profileSearchAdapter);

        // logic of profile transition via clicking
        profileResults.setOnItemClickListener((adapterView, view1, i, l) -> {
            Profile profile = profiles.get(i);
            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setIsOtherProfile(true);
            profileFragment.setOtherProfile(profile);
            ((MainActivity) requireActivity()).replaceFragment(profileFragment);
        });

        // logic to unfocus from search view, although there are cases where if you
        // click certain ui elements, the click wont register and you will be still
        // focused
        LinearLayout homepageLinearLayoutRoot = view.findViewById(R.id.HomepageLinearLayoutRoot);
        homepageLinearLayoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileSearch.clearFocus();
            }
        });

        // submit logic for search results
        ImageButton profileSearchIcon = view.findViewById(R.id.profileSearchConfirm);
        profileSearchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = profileSearch.getQuery().toString();
                if (!query.isEmpty()) {
                    profileResults.setVisibility(View.VISIBLE);
                    DatabaseManager.getInstance().searchUsers(query, users -> {
                        profiles.clear();

                        for (HashMap<String, Object> user : users) {
                            String profileJson = (String) user.get("profile");
                            try {
                                JSONObject profileObj = new JSONObject(profileJson);
                                String userId = profileObj.getString("userId");
                                String userImg;
                                if (profileObj.isNull("img")) {
                                    profiles.add(new Profile(userId));
                                    // use default profile pic
                                }
                                // else{
                                // // do stuff if profile pic exists
                                // }
                            } catch (JSONException e) {
                                Log.e("ProfileSearchJSONException", e.toString());
                            }
                        }
                        profileSearchAdapter.notifyDataSetChanged();
                    });
                }
            }
        });

        profileSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    profiles.clear();
                    profileSearchAdapter.notifyDataSetChanged();
                    profileResults.setVisibility(View.INVISIBLE);
                }
                return true;
            }
        });
    }

    /**
     * Filters the mood posts based on the selected mood.
     *
     * @param mood The selected mood filter.
     */
    @Override
    public void onFilterSelected(String mood) {
        filteredDataList.clear(); // Clear current filtered list

        if (mood.equals("last_week")) {
            filteredDataList.addAll(filterPostsFromLastWeek(dataList));
        } else {
            List<MoodPost> filteredPosts = PostFilterManager.filterPostsByMood(dataList, mood);
            filteredDataList.addAll(filteredPosts);
        }

        moodPostAdapter.notifyDataSetChanged();
    }

    /**
     * Opens the filter dialog, allowing the user to filter mood posts.
     */
    private void openFilterDialog() {
        FilterDialog filterDialog = new FilterDialog(requireContext(), this);
        filterDialog.showFilterDialog();
    }

    /**
     * Filters mood posts from the last seven days.
     *
     * @param posts The list of mood posts to filter.
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
     * Updates the filteredDataList based on the search query.
     *
     * @param query The search query to filter mood posts by description.
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
}
