package com.github.bytebandits.bithub;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PostMoodFragment extends Fragment {

    private Emotion selectedEmotion;
    private SocialSituation selectedSocialSituation;
    private String selectedDescription;

    // Listener interface that will be implemented in and used to communicate with main activity
    // MAYBE DELETE LATER ONCE DATABASE IS IMPLEMENTED
    interface AddMoodPostDialogListener {
        void addMoodPost(MoodPost moodPost);
    }
    private AddMoodPostDialogListener listener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Attach this fragment to the parent fragment that called it
        if (getParentFragment() instanceof AddMoodPostDialogListener) {
            listener = (AddMoodPostDialogListener) getParentFragment();
        } else {
            throw new RuntimeException(getParentFragment() + "must implement AddMoodPostDialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_mood_fragment, container, false);
        // Initialize views
        Spinner editEmotion = view.findViewById(R.id.postMoodEmotion);
        Spinner editSocialSituation = view.findViewById(R.id.postMoodSocialSituation);
        EditText editDescription = view.findViewById(R.id.postMoodDescription);
        Button cancelButton = view.findViewById(R.id.postMoodCancelButton);
        Button confirmButton = view.findViewById(R.id.postMoodConfirmButton);

        // Initialize the spinners
        // Create ArrayAdapters from the enumerations and a default spinner layout.
        List<Object> socialSituations = new ArrayList<>();
        socialSituations.add("prefer not to say");
        socialSituations.addAll(Arrays.asList(SocialSituation.values())); // add the rest of the social situations
        Emotion[] emotions = Emotion.values();
        ArrayAdapter<Emotion> emotionAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                emotions
        );
        ArrayAdapter<Object> socialSituationAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                socialSituations
        );

        // Specify the layout to use when the list of choices appears and apply the adapters
        socialSituationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editEmotion.setAdapter(emotionAdapter);
        editSocialSituation.setAdapter(socialSituationAdapter);

        // Initialize selected emotions and social situations
        selectedEmotion = emotionAdapter.getItem(0);
        selectedSocialSituation = null;

        // Handle user selected input for spinners
        editEmotion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedEmotion = (Emotion) parentView.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Default to first emotion on none selected
                selectedEmotion = emotionAdapter.getItem(0);
            }
        });
        editSocialSituation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // if prefer not to say is selected, set social situation to null
                if (position == 0) { selectedSocialSituation = null; }
                // else set it regularly
                else { selectedSocialSituation = (SocialSituation) parentView.getItemAtPosition(position); }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Default to null option on none selected
                selectedSocialSituation = null;
            }
        });

        confirmButton.setOnClickListener(v -> {
            // Get inputs
            // TODO Data verification : max 20 char or 3 words
            if (editDescription.getText().toString().isEmpty()) { selectedDescription = null; }
            else { selectedDescription = editDescription.getText().toString(); }

            // Send mood post back to homepage fragment
            listener.addMoodPost(new MoodPost(selectedEmotion, "Tony Yang",
                    null, selectedSocialSituation, selectedDescription, null));

            // Go back to homepage fragment
            getParentFragmentManager().popBackStack();
        });

        cancelButton.setOnClickListener(v -> {
            // Just go back to homepage fragment
            listener.addMoodPost(null);
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}
