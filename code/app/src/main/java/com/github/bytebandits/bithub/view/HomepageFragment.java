package com.github.bytebandits.bithub.view;

import static java.lang.Thread.sleep;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.MainActivity;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.model.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomepageFragment extends Fragment {
    private ArrayList<MoodPost> dataList;
    private ListView moodPostList;
    private MoodPostArrayAdapter moodPostAdapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepage_fragment, container, false);

        // Initialize dataList to avoid NullPointerException
        if (dataList == null) {
            dataList = new ArrayList<>();
            Log.d("HomepageFragment", "dataList initialized as empty list");
        } else {
            Log.d("HomepageFragment", "dataList already initialized with size: " + dataList.size());
        }

        executor.execute(() -> {
                    DatabaseManager.getInstance().getAllPosts(posts -> {
                        if (posts == null) {
                            Log.e("HomepageFragment", "Error: posts is null");
                        }

                        Log.d("HomepageFragment", "Fetched posts count: " + posts.size());

                        // Switch to UI thread for UI updates
                        mainHandler.post(() -> {
                            dataList.clear();
                            dataList.addAll(posts);

                            if (moodPostAdapter != null) {
                                moodPostAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("HomepageFragment", "moodPostAdapter is null");
                            }

                            // Initialize views and adapters
                            moodPostList = view.findViewById(R.id.homepageMoodPostList);
                            if (dataList.size() > 0) {
                                moodPostAdapter = new MoodPostArrayAdapter(getContext(), dataList);
                                moodPostList.setAdapter(moodPostAdapter);

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
     * Manages and encapsulates all logic relating to searching profiles within the homepage fragment
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
            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setIsOtherProfile(true);
            profileFragment.setOtherProfile(profile);
            ((MainActivity) requireActivity()).replaceFragment(profileFragment);
        });

        // logic to unfocus from search view, although there are cases where if you click certain ui elements, the click wont register and you will be still focused
        LinearLayout homepageLinearLayoutRoot = view.findViewById(R.id.HomepageLinearLayoutRoot);
        homepageLinearLayoutRoot.setOnClickListener(new View.OnClickListener() {
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
