package com.github.bytebandits.bithub.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Notification;

import java.util.ArrayList;

public class NotificationArrayAdapter extends ArrayAdapter<Notification> {
    public NotificationArrayAdapter(Context context, ArrayList<Notification> moodPosts) {
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

        Notification notification = getItem(position);
        TextView nameView = view.findViewById(R.id.textUserName);
        TextView dateView = view.findViewById(R.id.textDate);
        TextView timeView = view.findViewById(R.id.textTime);
        TextView emotionView = view.findViewById(R.id.emotion_group);
        TextView actionView = view.findViewById(R.id.action);
        ImageButton buttonA = view.findViewById(R.id.accept);
        ImageButton buttonD = view.findViewById(R.id.decline);
        TextView textA = view.findViewById(R.id.textTime2);
        TextView textD = view.findViewById(R.id.textTime5);

        // Set the text views of the view based on the movie object
        if (notification.getNotificationType()){
            buttonA.setVisibility(View.VISIBLE);
            buttonD.setVisibility(View.VISIBLE);
            textD.setVisibility(View.VISIBLE);
            textA.setVisibility(View.VISIBLE);
            emotionView.setVisibility(View.GONE);
            actionView.setText("has requested to follow you:");
            buttonA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle accept button click
                    DatabaseManager.getInstance().acceptUserFollow(SessionManager.getInstance(getContext()).getUserId(), notification.getProfile().getUserId());
                    remove(notification);
                }
            });
            buttonD.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle decline button click
                    DatabaseManager.getInstance().rejectUserFollow(SessionManager.getInstance(getContext()).getUserId(), notification.getProfile().getUserId());
                    remove(notification);
                }
            });

        }
        else {
            buttonA.setVisibility(View.GONE);
            buttonD.setVisibility(View.GONE);
            textD.setVisibility(View.GONE);
            textA.setVisibility(View.GONE);
            emotionView.setText(notification.getPost().getEmotion().getState());
            emotionView.setTextColor(ContextCompat.getColor(getContext(), notification.getPost().getEmotion().getColor()));
        }
        nameView.setText(notification.getProfile().getUserId());
        dateView.setText(notification.getFormattedPostedDate());
        timeView.setText(notification.getFormattedPostedTime());


        return view;
    }
}

