package com.github.bytebandits.bithub;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class HomepageFragment extends Fragment implements DatabaseManager.OnPostsFetchListener{
    private ArrayList<MoodPost> dataList;
    private ListView moodPostList;
    private MoodPostArrayAdapter moodPostAdapter;

    @Override
    public void onPostsFetched(ArrayList<MoodPost> posts) {
        dataList = posts;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepage_fragment, container, false);

        // Initialize dataList from database
        DatabaseManager.init();
        DatabaseManager.getPosts(((MainActivity) requireActivity()).profile.getUserID(),
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
        moodPostList = view.findViewById(R.id.homepageMoodPostList);
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
        return view;
    }
}
