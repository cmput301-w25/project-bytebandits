package com.github.bytebandits.bithub.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.MainActivity;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Getter method to get the state on whether a profile fragment represents another person's profile
     * @return boolean value on whether the above is true or false
     */
    public boolean getIsOtherProfile() {
        return isOtherProfile;
    }

    /**
     * Setter method to set the state on whether a profile fragment represents another person's profile
     * @param newIsOtherProfile  boolean value on whether the above is true or false
     */
    public void setIsOtherProfile(boolean newIsOtherProfile) {
        isOtherProfile = newIsOtherProfile;
    }

    public boolean isOtherProfile = false;

    /**
     * Getter method to get the profile object if the fragment represents another person (not the current user)
     * @return profile object
     */
    public Profile getOtherProfile() {
        return otherProfile;
    }

    /**
     * Setter method to set the profile object of the fragment
     * @param otherProfile profile object that represents another profile (not the current user)
     */
    public void setOtherProfile(Profile otherProfile) {
        this.otherProfile = otherProfile;
    }

    public Profile otherProfile = null;



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
            String username;
            if (isOtherProfile){
                username = getOtherProfile().getUserID();
            }
            else{
                username = SessionManager.getInstance(requireContext()).getUsername();
            }

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
private void profileSearchManager(View view) {
    ArrayList<Profile> profiles = new ArrayList<>();
    ProfileSearchAdapter profileSearchAdapter = new ProfileSearchAdapter(requireContext(), profiles);

    SearchView profileSearch = view.findViewById(R.id.profileSearch);
    ListView profileResults = view.findViewById(R.id.profileResults);
    profileResults.setAdapter(profileSearchAdapter);

    // logic of profile transition via clicking
    profileResults.setOnItemClickListener((adapterView, view1, i, l) -> {
        Profile profile = profiles.get(i);
        Toast.makeText(requireContext(), "Clicked: " + profile.getUserID(), Toast.LENGTH_SHORT).show();
        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setIsOtherProfile(true);
        profileFragment.setOtherProfile(profile);
        ((MainActivity) requireActivity()).replaceFragment(profileFragment);
    });

    // logic to unfocus from search view, although there are cases where if you click certain ui elements, the click wont register and you will be still focused
    ConstraintLayout parentConstraintLayout = view.findViewById(R.id.constraintLayoutProfile);
    parentConstraintLayout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            profileSearch.clearFocus();
        }
    });

    // submit logic for search results
    ImageButton profileSearchIcon = view.findViewById(R.id.profileSearchConfirm);
    profileSearchIcon.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            String query = profileSearch.getQuery().toString();
            if (!query.isEmpty()){
                profileResults.setVisibility(View.VISIBLE);
                DatabaseManager.getInstance().searchUsers(query, users -> {
                    profiles.clear();

                    for (HashMap<String, Object> user : users) {
                        String profileJson = (String) user.get("profile");
                        try{
                            JSONObject profileObj = new JSONObject(profileJson);
                            String userID = profileObj.getString("userID");
                            String userImg;
                            if (profileObj.isNull("img")) {
                                profiles.add(new Profile(userID));
                                // use default profile pic
                            }
//                            else{
//                                  // do stuff if profile pic exists
//                            }
                        }
                        catch (JSONException e) {
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
}
