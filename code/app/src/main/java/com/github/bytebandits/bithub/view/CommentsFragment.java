package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Movie;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.Comment;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Profile;

import java.util.ArrayList;

public class CommentsFragment extends DialogFragment implements
        AddCommentFragment.AddCommentDialogListener {
    private ListView commentList;
    private ArrayList<Comment> dataList;
    private CommentArrayAdapter commentAdapter;
    private Profile profile = SessionManager.getInstance(getContext()).getProfile();
    public static CommentsFragment newInstance(MoodPost moodPost) {
        // Use Bundle to get info between fragments
        Bundle args = new Bundle();
        args.putSerializable("moodPost", moodPost);
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // Add the comment given and notify a change
    @Override
    public void addComment(String commentText) {
        dataList.add(new Comment(profile, commentText));
        commentAdapter.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get views and retrieve data needed
        View view = LayoutInflater.from(getContext()).inflate(R.layout.comments_fragment, null);
        commentList = view.findViewById(R.id.commentsList);
        Button backButton = view.findViewById(R.id.commentsBackButton);
        Button addButton = view.findViewById(R.id.commentsAddButton);
        MoodPost moodPost = (MoodPost) getArguments().getSerializable("moodPost");

        // Create comment array
        dataList = new ArrayList<>();
        commentAdapter = new CommentArrayAdapter(getContext(), dataList);
        commentList.setAdapter(commentAdapter);

        // Test Data REMOVE LATER
        dataList.add(new Comment(profile, "THIS IS A TEST COMMENT. But will you still love me when nobody wants me around." +
                "When I turn 81 and forget things, will you still be proud? Cause I am the one that waited this long. " +
                "I am the one that might get it wrong. And I'll be the one that will love you the way I'm supposed to. " +
                "Girl. But will you still love me when nobody wants me around"));
        dataList.add(new Comment(profile, "THIS IS A TEST COMMENT. That is unfortunate lol"));
        dataList.add(new Comment(profile, "THIS IS A TEST COMMENT. L"));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Set back button logic
        backButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Set add button logic
        addButton.setOnClickListener(v -> {
            AddCommentFragment addCommentFragment = new AddCommentFragment();
            addCommentFragment.show(getChildFragmentManager(), "Add Comment View");
        });

        return dialog;
    }
}
