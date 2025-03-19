package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.MainActivity;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.model.Profile;

import java.util.Objects;
import java.util.Optional;

public class DetailedMoodPostFragment extends DialogFragment{
    public static DetailedMoodPostFragment newInstance(MoodPost moodPost) {
        // Use Bundle to get info between fragments
        Bundle args = new Bundle();
        args.putSerializable("moodPost", moodPost);
        DetailedMoodPostFragment fragment = new DetailedMoodPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get views and retrieve data needed
        View view = LayoutInflater.from(getContext()).inflate(R.layout.detailed_mood_post_fragment, null);
        TextView viewSocialStatus = view.findViewById(R.id.detailedViewSocialSituation);
        TextView viewName = view.findViewById(R.id.detailedViewName);
        TextView viewDate = view.findViewById(R.id.detailedViewDate);
        TextView viewTime = view.findViewById(R.id.detailedViewTime);
        TextView viewEmotion = view.findViewById(R.id.detailedViewEmotion);
        TextView viewDescription = view.findViewById(R.id.detailedViewDescription);
        ImageView viewImage = view.findViewById(R.id.detailedViewImage);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        Button editButton = view.findViewById(R.id.editButton);
        Button backButton = view.findViewById(R.id.backButton);
        Button commentsButton = view.findViewById(R.id.commentsButton);
        MoodPost moodPost = (MoodPost) getArguments().getSerializable("moodPost");
        Profile profile = SessionManager.getInstance(getContext()).getProfile();

        // Set the text views to mood post data
        viewSocialStatus.setText(moodPost.getSocialSituationString());
        viewName.setText(moodPost.getUsername());
        viewDate.setText(moodPost.getFormattedPostedDate());
        viewTime.setText(moodPost.getFormattedPostedTime());
        viewEmotion.setText(moodPost.getEmotionString());
        viewDescription.setText(moodPost.getDescription());
        viewImage.setImageResource(moodPost.getEmotion().getLogoID());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        backButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        commentsButton.setOnClickListener(v -> {
            // TODO: display the comments
            dialog.dismiss();
        });

        // Show and set edit and delete buttons if this post is ours
        if (Objects.equals(moodPost.getUsername(), profile.getUserID())) {
            deleteButton.setOnClickListener(v -> {
                DatabaseManager.deletePost(requireContext(), moodPost.getPostID(), Optional.empty());
                dialog.dismiss();
            });

            editButton.setOnClickListener(v -> {
                ((MainActivity) requireActivity()).editMoodFragment(moodPost);
                dialog.dismiss();
            });
        }
        else {
            deleteButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
        }

        return dialog;
    }
}
