package com.github.bytebandits.bithub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * This fragment represents the user's profile screen in the Bithub application.
 * It is responsible for:
 * - Displaying the profile layout.
 * - Providing access to the settings dialog via a settings button.
 */

public class ProfileFragment extends Fragment implements DatabaseManager.OnPostsFetchListener {

    private ArrayList<MoodPost> dataList;
    private ListView moodPostListHistory;
    private MoodPostArrayAdapter moodPostAdapter;
    private ImageButton settingsButton;

    @Override
    public void onPostsFetched(ArrayList<MoodPost> posts) {
        dataList = posts;
    }

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

        // Initialize dataList from database
        DatabaseManager.init();
        DatabaseManager.getPosts(((MainActivity) requireActivity()).Profile.getUserID(),
                new DatabaseManager.OnPostsFetchListener());

        // Listener so that dataList gets updated whenever the database does
        CollectionReference moodPostRef = db.getPostsCollectionRef(); // Maybe change later if getter name is wrong
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
                moodPostAdapter.notifyDataSetChanged();
            }
        });

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

        return view;
    }

    /**
     * Displays the settings dialog when the settings button is clicked.
     */
    private void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(requireContext());
        settingsDialog.showSettingsDialog();
    }
}