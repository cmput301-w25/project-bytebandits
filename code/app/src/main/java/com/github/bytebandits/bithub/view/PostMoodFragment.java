package com.github.bytebandits.bithub.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.model.Emotion;
import com.github.bytebandits.bithub.MainActivity;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.SocialSituation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PostMoodFragment extends Fragment {
    private Emotion selectedEmotion;
    private SocialSituation selectedSocialSituation;
    private String selectedDescription;

    public static PostMoodFragment newInstance(MoodPost moodPost) {
        // Use Bundle to get info between fragments
        Bundle args = new Bundle();
        args.putSerializable("mood post", moodPost);
        PostMoodFragment fragment = new PostMoodFragment();
        fragment.setArguments(args);
        return fragment;
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

        // Get the mood post if we need to edit a mood post
        String tag = getTag();
        Bundle bundle = getArguments();
        MoodPost postToEdit;
        if (tag != null && tag.equals("edit mood post") && bundle != null){
            postToEdit = (MoodPost) bundle.getSerializable("mood post");
        }
        else {
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
                DatabaseManager databaseManager = DatabaseManager.getInstance();
                // Add mood post to database
                if (postToEdit == null) {
                    SessionManager sessionManager = SessionManager.getInstance(requireContext());
                    MoodPost moodPost = new MoodPost(selectedEmotion, sessionManager.getProfile(), false, selectedSocialSituation, selectedDescription, null);
                    databaseManager.addPost(moodPost, sessionManager.getUsername(), Optional.empty());
                }
                else {
                    HashMap<String, Object> updateFields = new HashMap<>();
                    updateFields.put("socialSituation", selectedSocialSituation);
                    updateFields.put("emotion", selectedEmotion);
                    updateFields.put("description", selectedDescription);
                    databaseManager.updatePost(postToEdit.getPostID(), updateFields, Optional.empty());
                }
                // Go back to homepage fragment
                ((MainActivity) requireActivity()).replaceFragment(new HomepageFragment());
            }
        });

        cancelButton.setOnClickListener(v -> {
            // Just go back to homepage fragment
            ((MainActivity) requireActivity()).replaceFragment(new HomepageFragment());
        });

        return view;
    }

    /**
     * Checks if a given string contains more than three words.
     * This method is primarily used to validate description inputs
     * to ensure they do not exceed a specified word limit.
     *
     * @param string The input string to be checked. Can be {@code null}.
     * @return {@code true} if the string contains more than three words,
     *         {@code false} if the string is {@code null} or contains
     *         three or fewer words.
     *
    */
    private boolean moreThanThreeWords(String string) {
        if (string == null) { return false; }
        String[] words = string.split("\\s+"); // Groups all whitespace together and splits by the whitespace
        return (words.length > 3);
    }

    /**
     * Selects an item in a {@link Spinner} by matching its value.
     * This method iterates through the items in the spinner's adapter
     * and selects the item that matches the given value.
     *
     * @param spnr  The {@link Spinner} whose item is to be selected.
     *             Must not be {@code null}.
     * @param value The value to match against the spinner's items.
     *             Must not be {@code null}.
     *
     * @throws NullPointerException If either {@code spnr} or {@code value} is {@code null}.
    */
    private void selectSpinnerItemByValue(Spinner spnr, Object value) {
        SpinnerAdapter adapter = spnr.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItem(position).equals(value)) {
                spnr.setSelection(position);
                return;
            }
        }
    }
}
