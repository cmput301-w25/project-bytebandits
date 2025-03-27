package com.github.bytebandits.bithub.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.R;

import java.util.ArrayList;

public class MoodPostArrayAdapter extends ArrayAdapter<MoodPost> {
    public MoodPostArrayAdapter(Context context, ArrayList<MoodPost> moodPosts) {
        super(context, 0, moodPosts);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        // Check if there is a view in convertView we can reuse, if not create a new view
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.mood_post_content,
                    parent, false);
        } else {
            view = convertView;
        }

        // Get the movie object wanted and text views of the view
        MoodPost moodPost = getItem(position);
        ImageView logoView = view.findViewById(R.id.moodIcon);
        TextView nameView = view.findViewById(R.id.textUserName);
        TextView dateView = view.findViewById(R.id.textDate);
        TextView timeView = view.findViewById(R.id.textTime);
        TextView emotionView = view.findViewById(R.id.textCommentText);

        // Set the text views of the view based on the movie object
        nameView.setText(moodPost.getProfile().getUserId());
        dateView.setText(moodPost.getFormattedPostedDate());
        timeView.setText(moodPost.getFormattedPostedTime());
        emotionView.setText(moodPost.getEmotion().getState());
        logoView.setImageResource(moodPost.getEmotion().getLogoID());

        return view;
    }
}
