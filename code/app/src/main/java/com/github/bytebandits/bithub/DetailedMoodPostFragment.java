package com.github.bytebandits.bithub;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Optional;

/**
 * A {@link DialogFragment} that displays the details of a {@link MoodPost} in a
 * dialog window.
 * Allows users to view, edit, or delete the mood post.
 */
public class DetailedMoodPostFragment extends DialogFragment {

    /**
     * Creates a new instance of {@code DetailedMoodPostFragment} with the provided
     * mood post.
     *
     * @param moodPost The mood post to be displayed in detail.
     * @return A new instance of {@code DetailedMoodPostFragment}.
     */
    public static DetailedMoodPostFragment newInstance(MoodPost moodPost) {
        // Use Bundle to get info between fragments
        Bundle args = new Bundle();
        args.putSerializable("moodPost", moodPost);
        DetailedMoodPostFragment fragment = new DetailedMoodPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to create the dialog instance when the fragment is displayed.
     *
     * @param savedInstanceState The previously saved state of the fragment, if
     *                           available.
     * @return A {@link Dialog} displaying the detailed mood post.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get views of inputs and retrieve serialized data
        View view = LayoutInflater.from(getContext()).inflate(R.layout.detailed_mood_post_fragment, null);
        TextView viewSocialStatus = view.findViewById(R.id.detailedViewSocialSituation);
        TextView viewName = view.findViewById(R.id.detailedViewName);
        TextView viewDate = view.findViewById(R.id.detailedViewDate);
        TextView viewTime = view.findViewById(R.id.detailedViewTime);
        TextView viewFeelingHeader = view.findViewById(R.id.detailedViewFeelingHeader);
        TextView viewEmotion = view.findViewById(R.id.detailedViewEmotion);
        TextView viewDescription = view.findViewById(R.id.detailedViewDescription);
        ImageView viewImage = view.findViewById(R.id.detailedViewImage);
        MoodPost moodPost = (MoodPost) getArguments().getSerializable("moodPost");

        // Set the text views to mood post data
        viewSocialStatus.setText(moodPost.getSocialSituationString());
        viewName.setText(moodPost.getUsername());
        viewDate.setText(moodPost.getFormattedPostedDate());
        viewTime.setText(moodPost.getFormattedPostedTime());
        viewFeelingHeader.setText("Feeling... ");
        viewEmotion.setText(moodPost.getEmotionString());
        viewDescription.setText(moodPost.getDescription());
        viewImage.setImageResource(moodPost.getEmotion().getLogoID());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Detailed View of Mood Post")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Edit", (dialog, which) -> {
                    ((MainActivity) requireActivity()).editMoodFragment(moodPost);
                })
                .setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseManager.deletePost(requireContext(), moodPost.getPostID(), Optional.empty());
                })
                .create();
    }
}
