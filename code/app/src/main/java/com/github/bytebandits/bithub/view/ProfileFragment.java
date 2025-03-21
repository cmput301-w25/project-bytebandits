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
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.MainActivity;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This fragment represents the user's profile screen in the Bithub application.
 * It is responsible for:
 * - Displaying the profile layout.
 * - Providing access to the settings dialog via a settings button.
 */

public class ProfileFragment extends Fragment {
    private ArrayList<MoodPost> dataList;
    private ListView moodPostListHistory;
    private MoodPostArrayAdapter moodPostAdapter;
    private ImageButton settingsButton;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater used to inflate views.
     * @param container the parent view that the fragment's UI should attach to.
     * @param savedInstanceState this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI
     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);  // display profile fragment layout
        // Initialize dataList to avoid NullPointerException
        if (dataList == null) {
            dataList = new ArrayList<>();
            Log.d("ProfileFragment", "dataList initialized as empty list");
        } else {
            Log.d("ProfileFragment", "dataList already initialized with size: " + dataList.size());
        }

        executor.execute(() -> {
            String username = SessionManager.getInstance(requireContext()).getUsername();
            DatabaseManager.getInstance().getUserPosts(username, posts -> {
                if (posts == null) {
                    Log.e("ProfileFragment", "Error: posts is null");
                    return null;
                }

                Log.d("ProfileFragment", "Fetched posts count: " + posts.size());

                // Switch to UI thread for UI updates
                mainHandler.post(() -> {
                    dataList.clear();
                    dataList.addAll(posts);

                    if (moodPostAdapter != null) {
                        moodPostAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("ProfileFragment", "moodPostAdapter is null");
                    }

                    // Initialize views and adapters
                    moodPostListHistory = view.findViewById(R.id.mood_post_list_history);
                    moodPostAdapter = new MoodPostArrayAdapter(getContext(), dataList);
                    moodPostListHistory.setAdapter(moodPostAdapter);

                    // open detailed view of the user's clicked post
                    moodPostListHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            DetailedMoodPostFragment detailedMoodPostFragment =
                                    DetailedMoodPostFragment.newInstance(dataList.get(position));
                            detailedMoodPostFragment.show(getActivity().getSupportFragmentManager(), "Detailed Mood Post View");
                        }
                    });

                    settingsButton = view.findViewById(R.id.settings_button);
                    settingsButton.setOnClickListener(v -> openSettings());
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
                dataList.clear();
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        snapshot.toObject(MoodPost.class);
                        dataList.add(snapshot.toObject(MoodPost.class));
                    }
                }
                if (moodPostAdapter != null) {
                    moodPostAdapter.notifyDataSetChanged();
                }
            }
        });

        profileSearchManager(view);

        return view;
    }

    /**
     * Displays the settings dialog when the settings button is clicked.
     */
    private void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(requireContext());
        settingsDialog.showSettingsDialog();
    }

    /**
     * Manages and encapsulates all logic relating to searching profiles within the profile fragment
     * @param view the view in question, so the method can reference the various UI elements of the layout
     */
    private void profileSearchManager(View view){
        ArrayList<Profile> profiles = new ArrayList<Profile>();
        ProfileSearchAdapter profileSearchAdapter = new ProfileSearchAdapter(requireContext(), profiles);

        SearchView profileSearch = view.findViewById((R.id.profileSearch));
        ListView profileResults = view.findViewById(R.id.profileResults);
        profileResults.setAdapter(profileSearchAdapter);

        profileResults.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // all for manual testing and setup for transitioning to different fragments
                Profile profile = profiles.get(i);
                Toast.makeText(requireContext(), "Clicked: " + profile.getUserID(), Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).replaceFragment(new HomepageFragment());
                // and update state of navbar
            }
        });

        DatabaseManager.getInstance().searchUsers("test", new DatabaseManager.OnUserSearchFetchListener(){
            @Override
            public void onUsersFetched(List<HashMap<String, Object>> users) {
//                HashMap<String, ?> userHashMap = (HashMap<String, ?>) users.get(0).get("test");
//                Object object = users.get(0).get("test");

                profileSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        profileResults.setVisibility(View.VISIBLE);
                        if (s.isEmpty()){
                            profiles.clear();
                            profileSearchAdapter.notifyDataSetChanged();
                            profileResults.setVisibility(View.INVISIBLE);
                        }
                        else{
                            // remove comment and call db method here
                            profiles.add(new Profile("zzzzzzzzzzz"));
                            profileSearchAdapter.notifyDataSetChanged();
                        }

                        return true;
                    }
                });
            }
        });


    }
}
