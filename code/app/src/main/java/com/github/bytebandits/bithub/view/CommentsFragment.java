package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.Comment;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Profile;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The CommentsFragment to add the dialog to insert a comment
 *
 * @author Tony Yang
 */
public class CommentsFragment extends DialogFragment implements
        AddCommentFragment.AddCommentDialogListener,
        CommentArrayAdapter.DeleteCommentListener {
    private ListView commentList;
    private ArrayList<Comment> dataList;
    private CommentArrayAdapter commentAdapter;
    private Profile profile = SessionManager.getInstance(getContext()).getProfile();
    private MoodPost moodPost;
    public static CommentsFragment newInstance(MoodPost moodPost) {
        // Use Bundle to get info between fragments
        Bundle args = new Bundle();
        args.putSerializable("moodPost", moodPost);
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // Add the comment given to the display and database and notify a change
    @Override
    public void addComment(String commentText) {
        dataList.add(new Comment(profile, commentText));
        HashMap<String, Object> updateFields = new HashMap<>();
        updateFields.put("comments", dataList);
        DatabaseManager.getInstance().updatePost(moodPost.getPostID(), updateFields, null);
        commentAdapter.notifyDataSetChanged();
    }

    // Delete the comment given from display and database and notify a change
    @Override
    public void deleteComment(int position) {
        dataList.remove(position);
        HashMap<String, Object> updateFields = new HashMap<>();
        updateFields.put("comments", dataList);
        DatabaseManager.getInstance().updatePost(moodPost.getPostID(), updateFields, null);
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
        moodPost = (MoodPost) getArguments().getSerializable("moodPost");

        // Create comment array
        dataList = moodPost.getComments();
        commentAdapter = new CommentArrayAdapter(getContext(), dataList, this);
        commentList.setAdapter(commentAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Set back button logic
        backButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Set add button logic
        addButton.setOnClickListener(v -> {
            AddCommentFragment addCommentFragment = AddCommentFragment.newInstance(moodPost);
            addCommentFragment.show(getChildFragmentManager(), "Add Comment View");
        });

        return dialog;
    }
}
