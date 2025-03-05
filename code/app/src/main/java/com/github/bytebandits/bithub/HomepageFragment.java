package com.github.bytebandits.bithub;

import android.graphics.Movie;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class HomepageFragment extends Fragment {
    private ArrayList<MoodPost> dataList;
    private ListView moodPostList;
    private MoodPostArrayAdapter moodPostAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepage_fragment, container, false);
        dataList = new ArrayList<>();

        // Initialize views and adapters
        moodPostList = view.findViewById(R.id.homepageMoodPostList);
        moodPostAdapter = new MoodPostArrayAdapter(getContext(), dataList);
        moodPostList.setAdapter(moodPostAdapter);

        //Test Data
        dataList.add(new MoodPost(Emotion.HAPPINESS, "Tony Yang",
                null, SocialSituation.ALONE, "I'm happy", null));
        dataList.add(new MoodPost(Emotion.ANGER, "Tony Yang",
                null, SocialSituation.GROUP, "I'm angry", null));
        dataList.add(new MoodPost(Emotion.DISGUST, "Tony Yang",
                null, SocialSituation.PARTNER, null, null));

        // on item click on list, select the item clicked then set edit and delete button as visible
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
