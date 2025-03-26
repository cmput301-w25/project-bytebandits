package com.github.bytebandits.bithub.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.Comment;
import com.github.bytebandits.bithub.model.MoodPost;

import java.util.ArrayList;
import java.util.Objects;

public class CommentArrayAdapter extends ArrayAdapter<Comment> {
    private DeleteCommentListener listener;

    public CommentArrayAdapter(Context context, ArrayList<Comment> comments, DeleteCommentListener listener) {
        super(context, 0, comments);
        this.listener = listener;
    }

    // Listener interface that will be implemented in and used to communicate with
    // comments fragment
    interface DeleteCommentListener {
        void deleteComment(int position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if there is a view in convertView we can reuse, if not create a new
        // view
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.comment_content,
                    parent, false);
        } else {
            view = convertView;
        }

        // Get the comment object wanted and text views of the view
        Comment comment = getItem(position);
        TextView nameView = view.findViewById(R.id.textUserName);
        TextView dateView = view.findViewById(R.id.textDate);
        TextView timeView = view.findViewById(R.id.textTime);
        TextView commentTextView = view.findViewById(R.id.textCommentText);
        Button deleteButton = view.findViewById(R.id.deleteCommentButton);

        // Set the text views of the view based on the comment object
        nameView.setText(comment.getProfile().getUserID());
        dateView.setText(comment.getFormattedPostedDate());
        timeView.setText(comment.getFormattedPostedTime());
        commentTextView.setText(comment.getText());
        if (!Objects.equals(comment.getProfile().getUserID(),
                SessionManager.getInstance(getContext()).getProfile().getUserID())) {
            deleteButton.setVisibility(View.GONE);
        } else {
            // If this is our comment, give option to delete
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.deleteComment(position);
                }
            });
        }
        return view;
    }
}
