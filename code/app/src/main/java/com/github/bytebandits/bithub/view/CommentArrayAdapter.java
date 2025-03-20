package com.github.bytebandits.bithub.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.model.Comment;

import java.util.ArrayList;

public class CommentArrayAdapter extends ArrayAdapter<Comment> {
    public CommentArrayAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        // Check if there is a view in convertView we can reuse, if not create a new view
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.comment_content,
                    parent, false);
        } else {
            view = convertView;
        }

        // Get the movie object wanted and text views of the view
        Comment comment = getItem(position);
        TextView nameView = view.findViewById(R.id.textUserName);
        TextView dateView = view.findViewById(R.id.textDate);
        TextView timeView = view.findViewById(R.id.textTime);
        TextView commentTextView = view.findViewById(R.id.textEmotion);

        // Set the text views of the view based on the movie object
        nameView.setText(comment.getUsername());
        dateView.setText(comment.getFormattedPostedDate());
        timeView.setText(comment.getFormattedPostedTime());
        commentTextView.setText(comment.getText());

        return view;
    }
}
