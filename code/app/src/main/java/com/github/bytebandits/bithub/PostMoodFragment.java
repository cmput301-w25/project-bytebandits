package com.github.bytebandits.bithub;

import android.content.Context;
import android.graphics.Movie;
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
import android.widget.SpinnerAdapter;
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

    public static PostMoodFragment newInstance(MoodPost moodPost, int position) {
        // Use Bundle to get info between fragments
        Bundle args = new Bundle();
        args.putSerializable("mood post", moodPost);
        args.putSerializable("position", position);
        PostMoodFragment fragment = new PostMoodFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // Listener interface that will be implemented in and used to communicate with main activity
    // MAYBE DELETE LATER ONCE DATABASE IS IMPLEMENTED
    interface MoodPostDialogListener {
        void addMoodPost(MoodPost moodPost);
        void editMoodPost(MoodPost moodPost, int position);
    }
    private MoodPostDialogListener listener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Attach this fragment to the parent fragment that called it
        if (getParentFragment() instanceof MoodPostDialogListener) {
            listener = (MoodPostDialogListener) getParentFragment();
        } else {
            throw new RuntimeException(getParentFragment() + "must implement MoodPostDialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_mood_fragment, container, false);
        // Initialize views
        Spinner editEmotion = view.findViewById(R.id.postMoodEmotion);
        Spinner editSocialSituation = view.findViewById(R.id.postMoodSocialSituation);
        EditText editDescription = view.findViewById(R.id.postMoodDescription);
        Button cancelButton = view.findViewById(R.id.postMoodCancelButton);
        Button confirmButton = view.findViewById(R.id.postMoodConfirmButton);

        // Get the mood post if we need ot edit a mood post
        String tag = getTag();
        Bundle bundle = getArguments();
        MoodPost postToEdit;
        int postPosition;
        if (tag != null && tag.equals("edit mood post") && bundle != null){
            postToEdit = (MoodPost) bundle.getSerializable("mood post");
            postPosition = (int) bundle.getSerializable("position");
        }
        else {
            postPosition = 0;  // just initialize it so that IDE doesn't yell at me when passing it later
            postToEdit = null;
        }

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
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
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
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
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

        // If editing mood post, set the input views to mood post properties
        if (postToEdit != null) {
            selectSpinnerItemByValue(editEmotion, postToEdit.getEmotion());
            selectSpinnerItemByValue(editSocialSituation, postToEdit.getSocialSituation());
            if (postToEdit.getDescription() == null) { editDescription.setText(""); }
            else { editDescription.setText(postToEdit.getDescription()); }
        }

        confirmButton.setOnClickListener(v -> {
            // Get inputs
            if (editDescription.getText().toString().isEmpty()) { selectedDescription = null; }
            else { selectedDescription = editDescription.getText().toString(); }

            // Check for valid description input (max 20 char. or 3 words), if valid, add mood post
            if (selectedDescription != null &&
                    ((moreThanThreeWords(selectedDescription)) || selectedDescription.length() > 20)) {
                editDescription.setError("Description can be max 20 characters or 3 words");
            }
            else {
                // Send mood post back to homepage fragment
                if (postToEdit == null) {
                    listener.addMoodPost(new MoodPost(selectedEmotion, "Tony Yang",
                            false, selectedSocialSituation, selectedDescription, null));
                }
                else {
                    postToEdit.setEmotion(selectedEmotion);
                    postToEdit.setSocialSituation(selectedSocialSituation);
                    postToEdit.setDescription(selectedDescription);
                    listener.editMoodPost(postToEdit, postPosition);
                }
                // Go back to homepage fragment
                getParentFragmentManager().popBackStack();
            }
        });

        cancelButton.setOnClickListener(v -> {
            // Just go back to homepage fragment
            listener.addMoodPost(null);
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    // Helper function that checks if a string has a max of three words
    // Used for checking for valid description input
    private boolean moreThanThreeWords(String string) {
        if (string == null) { return false; }
        String[] words = string.split("\\s+"); // Groups all whitespace together and splits by the whitespace
        return (words.length > 3);
    }

    // Given a value wanted, select the item in the spinner given
    public static void selectSpinnerItemByValue(Spinner spnr, Object value) {
        SpinnerAdapter adapter = spnr.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItem(position).equals(value)) {
                spnr.setSelection(position);
                return;
            }
        }
    }
}
