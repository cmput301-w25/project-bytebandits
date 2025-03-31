package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.model.MoodPost;

/**
 * Fragment to allow the user to insert a comment into a mood post
 *
 * @author Tony Yang
 */
public class AddCommentFragment extends DialogFragment {
    public static AddCommentFragment newInstance(MoodPost moodPost) {
        // Use Bundle to get info between fragments
        Bundle args = new Bundle();
        args.putSerializable("moodPost", moodPost);
        AddCommentFragment fragment = new AddCommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // Listener interface that will be implemented in and used to communicate with comments fragment
    interface AddCommentDialogListener {
        void addComment(String commentText);
    }

    private AddCommentDialogListener listener;
    private String inputtedText;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Check if the parent fragment implements the listener interface
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof AddCommentDialogListener) {
            listener = (AddCommentDialogListener) parentFragment;
        }  else {
            throw new RuntimeException(parentFragment + "must implement AddCommentDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get views and mood post
        View view = LayoutInflater.from(getContext()).inflate(R.layout.add_comment_fragment, null);
        EditText editCommentText = view.findViewById(R.id.addCommentText);
        Button cancelButton = view.findViewById(R.id.addCommentCancelButton);
        Button confirmButton = view.findViewById(R.id.addCommentConfirmButton);
        MoodPost moodPost = (MoodPost) getArguments().getSerializable("moodPost");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Set back button logic
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Set add button logic
        confirmButton.setOnClickListener(v -> {
            String commentText = editCommentText.getText().toString();
            if (commentText.isEmpty()) {
                editCommentText.setError("Comment cannot be blank");
            }
            else {
                listener.addComment(commentText);
                dialog.dismiss();
            }
        });

        return dialog;
    }
}
