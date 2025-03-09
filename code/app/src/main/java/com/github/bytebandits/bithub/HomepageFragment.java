package com.github.bytebandits.bithub;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class HomepageFragment extends Fragment{
    private Database db = Database.init();
    private ArrayList<MoodPost> dataList;
    private ListView moodPostList;
    private MoodPostArrayAdapter moodPostAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepage_fragment, container, false);

        // Initialize dataList from database
        dataList = db.getPosts(((MainActivity) requireActivity()).profile.getUserID());

        /* NEED TO ADD SOMETHING LIKE THIS SO THAT DATALIST GETS UPDATED WHENEVER THE DATABASE DOES
           Here movieArrayList is equal to this file's dataList

        db = FirebaseFirestore.getInstance();
        moviesRef = db.collection("movies");

        moviesRef.addSnapshotListener((value, error) -> {
            if (error != null){
                Log.e("Firestore", error.toString());
            }
            if (value != null){
                movieArrayList.clear();
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        snapshot.toObject(Movie.class);
                        movieArrayList.add(snapshot.toObject(Movie.class));
                    }
                }
                movieArrayAdapter.notifyDataSetChanged();
            }
        });
         */

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
