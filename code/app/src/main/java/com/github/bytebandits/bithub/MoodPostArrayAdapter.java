package com.github.bytebandits.bithub;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Custom ArrayAdapter for displaying a list of MoodPost objects in a ListView.
 * It inflates a custom layout for each MoodPost item and populates the views
 * with relevant data.
 */
public class MoodPostArrayAdapter extends ArrayAdapter<MoodPost> {
    /**
     * Constructs a new MoodPostArrayAdapter.
     *
     * @param context   The context of the calling activity or fragment.
     * @param moodPosts The list of MoodPost objects to be displayed.
     */
    public MoodPostArrayAdapter(Context context, ArrayList<MoodPost> moodPosts) {
        super(context, 0, moodPosts);
    }

    /**
     * Provides a view for an AdapterView (ListView).
     *
     * @param position    The position of the item in the data set.
     * @param convertView The existing view to reuse, if possible.
     * @param parent      The parent view that the returned view will be attached
     *                    to.
     * @return The populated view representing the MoodPost item.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if there is a view in convertView we can reuse, if not create a new
        // view
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
        TextView emotionView = view.findViewById(R.id.textEmotion);

        // Set the text views of the view based on the movie object
        nameView.setText(moodPost.getUsername());
        dateView.setText(moodPost.getFormattedPostedDate());
        timeView.setText(moodPost.getFormattedPostedTime());
        emotionView.setText(moodPost.getEmotion().getState());
        logoView.setImageResource(moodPost.getEmotion().getLogoID());

        return view;
    }
}
