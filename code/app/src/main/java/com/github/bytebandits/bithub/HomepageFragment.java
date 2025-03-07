package com.github.bytebandits.bithub;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class HomepageFragment extends Fragment implements
        PostMoodFragment.AddMoodPostDialogListener{
    private ArrayList<MoodPost> dataList;
    private ListView moodPostList;
    private MoodPostArrayAdapter moodPostAdapter;

    // GET RID OF LATER ONCE DATABASE IS IMPLEMENTED
    private FloatingActionButton tempButton;
    @Override
    public void addMoodPost(MoodPost moodPost) {
        if (!(moodPost == null)) {
            moodPostAdapter.add(moodPost);
            moodPostAdapter.notifyDataSetChanged();
        }
        FrameLayout layout = getView().findViewById(R.id.child_fragment_container);
        layout.setVisibility(View.GONE);
    }
    private void launchPostMoodFragment() {
        FrameLayout layout = getView().findViewById(R.id.child_fragment_container);
        layout.setVisibility(View.VISIBLE);
        PostMoodFragment postMoodFragment = new PostMoodFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.child_fragment_container, postMoodFragment); // Use the parent fragment's container
        transaction.addToBackStack(null); // Optional: Add to back stack
        transaction.commit();
    }

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
                false, SocialSituation.ALONE, "I'm happy", null));
        dataList.add(new MoodPost(Emotion.ANGER, "Tony Yang",
                false, SocialSituation.GROUP, "I'm angry", null));
        dataList.add(new MoodPost(Emotion.DISGUST, "Tony Yang",
                false, SocialSituation.PARTNER, null, null));

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

        // DELETE LATER ONCE NAV BAR IS IMPLEMENTED
        // Method to launch the child fragment
        tempButton = view.findViewById(R.id.tempButton);
        tempButton.setOnClickListener(v -> {
            // Go to post mood fragment
            launchPostMoodFragment();
        });


        return view;
    }
}
