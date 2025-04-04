package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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

/**
 * Detailed view of a specific mood post, giving more details other than the list view
 *
 * @author Tony Yang
 */
public class DetailedMoodPostFragment extends DialogFragment {
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
        ImageView viewSocialIcon = view.findViewById(R.id.detailedViewSocialIcon);
        TextView viewName = view.findViewById(R.id.detailedViewName);
        TextView viewDate = view.findViewById(R.id.detailedViewDate);
        TextView viewTime = view.findViewById(R.id.detailedViewTime);
        TextView viewEmotion = view.findViewById(R.id.detailedViewEmotion);
        TextView viewDescription = view.findViewById(R.id.detailedViewDescription);
        ImageView viewMoodIcon = view.findViewById(R.id.detailedViewMoodIcon);
        ImageView viewImage = view.findViewById(R.id.detailedViewImage);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        Button editButton = view.findViewById(R.id.editButton);
        Button backButton = view.findViewById(R.id.backButton);
        Button commentsButton = view.findViewById(R.id.commentsButton);
        Button viewProfileButton = view.findViewById(R.id.viewProfileButton);
        MoodPost moodPost = (MoodPost) getArguments().getSerializable("moodPost");
        Profile profile = SessionManager.getInstance(getContext()).getProfile();

        // Set the text views to mood post data
        if (moodPost.getSocialSituation() == null) {
            viewSocialStatus.setText(null);
            viewSocialIcon.setVisibility(View.INVISIBLE);
        } else {
            viewSocialStatus.setText(moodPost.getSocialSituation().name());
        }
        viewName.setText(moodPost.getProfile().getUserId());
        viewDate.setText(moodPost.getFormattedPostedDate());
        viewTime.setText(moodPost.getFormattedPostedTime());
        viewEmotion.setText(moodPost.getEmotion().getState());
        if (moodPost.getDescription() == null) {
            viewDescription.setText("No Description...");
            viewDescription.setAlpha(0.5F);
        } else {
            viewDescription.setText(moodPost.getDescription());
        }
        viewMoodIcon.setImageResource(moodPost.getEmotion().getLogoID());
        if (moodPost.getImage() != null) {
            byte[] imageArray = Base64.decode(moodPost.getImage(), Base64.DEFAULT);
            viewImage.setImageBitmap(BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        backButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        commentsButton.setOnClickListener(v -> {
            CommentsFragment commentsFragment = CommentsFragment.newInstance(moodPost);
            commentsFragment.show(getActivity().getSupportFragmentManager(), "Comments View");
        });

        // Show and set edit and delete buttons if this post is ours
        if (Objects.equals(moodPost.getProfile().getUserId(), profile.getUserId())) {
            viewProfileButton.setVisibility(View.GONE);
            deleteButton.setOnClickListener(v -> {
                // Get confirmation for delete
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Delete")
                        .setMessage(
                                "Are you sure you want to delete this post?")
                        .setPositiveButton("Yes", (confirmDialog, which) -> {
                            DatabaseManager.getInstance().deletePost(moodPost.getPostID(),
                                    moodPost.getProfile().getUserId(),
                                    null);
                            dialog.dismiss();
                            confirmDialog.cancel();
                        })
                        .setNegativeButton("No", (confirmDialog, which) -> {
                            confirmDialog.cancel();
                        })
                        .show();
            });

            editButton.setOnClickListener(v -> {
                ((MainActivity) requireActivity()).editMoodFragment(moodPost);
                dialog.dismiss();
            });
        } else {
            viewProfileButton.setOnClickListener(v -> {
                ((MainActivity) requireActivity()).replaceFragment(ProfileFragment.newInstance(moodPost.getProfile()));
                dialog.dismiss();
            });
            deleteButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
        }

        return dialog;
    }
}
