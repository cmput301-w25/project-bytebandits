package com.github.bytebandits.bithub.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.model.MoodPost;

import java.util.ArrayList;

public class NotificationArrayAdapter extends ArrayAdapter<MoodPost> {
    public NotificationArrayAdapter(Context context, ArrayList<MoodPost> moodPosts) {
        super(context, 0, moodPosts);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        // Check if there is a view in convertView we can reuse, if not create a new view
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.notification_content,
                    parent, false);
        } else {
            view = convertView;
        }

        // Get the movie object wanted and text views of the view
        MoodPost moodPost = getItem(position);
        TextView nameView = view.findViewById(R.id.textUserName);
        TextView dateView = view.findViewById(R.id.textDate);
        TextView timeView = view.findViewById(R.id.textTime);
        TextView emotionView = view.findViewById(R.id.emotion_group);

        // Set the text views of the view based on the movie object
        nameView.setText(moodPost.getProfile().getUserID());
        dateView.setText(moodPost.getFormattedPostedDate());
        timeView.setText(moodPost.getFormattedPostedTime());
        emotionView.setText(moodPost.getEmotion().getState());
        emotionView.setTextColor(ContextCompat.getColor(getContext(), moodPost.getEmotion().getColor()));

        return view;
    }
}

