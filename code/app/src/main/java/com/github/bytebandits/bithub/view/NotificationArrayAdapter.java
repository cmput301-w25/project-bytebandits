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
import com.github.bytebandits.bithub.model.Notification;

import java.util.ArrayList;

/**
 * Represents the notification array adapter
 *
 * @author Soham Limbachia
 */
public class NotificationArrayAdapter extends ArrayAdapter<Notification> {
    /**
     * Constructor for NotificationArrayAdapter
     * @param context The context of the activity
     * @param moodPosts The list of mood posts to display
     */
    public NotificationArrayAdapter(Context context, ArrayList<Notification> moodPosts) {
        super(context, 0, moodPosts);
    }

    /**
     * Gets the view for the notification
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.notification_content,
                    parent, false);
        } else {
            view = convertView;
        }

        Notification notification = getItem(position);
        if (notification == null) {
            return view;
        }

        TextView nameView = view.findViewById(R.id.textUserName);
        TextView dateView = view.findViewById(R.id.textDate);
        TextView timeView = view.findViewById(R.id.textTime);
        TextView emotionView = view.findViewById(R.id.emotion_group);
        TextView actionView = view.findViewById(R.id.action);
        ImageButton buttonA = view.findViewById(R.id.accept);
        ImageButton buttonD = view.findViewById(R.id.decline);
        TextView textA = view.findViewById(R.id.textTime2);
        TextView textD = view.findViewById(R.id.textTime5);

        // Set the text views of the view based on the notification type.
        if (notification.getNotificationType()){
            // Set the text views of the request notification view.
            buttonA.setVisibility(View.VISIBLE);
            buttonD.setVisibility(View.VISIBLE);
            textD.setVisibility(View.VISIBLE);
            textA.setVisibility(View.VISIBLE);
            emotionView.setVisibility(View.GONE);
            actionView.setText(R.string.notification_request);
            buttonA.setOnClickListener(v -> {
                // Handle accept button click
                DatabaseManager.getInstance().acceptUserFollow(SessionManager.getInstance(getContext()).getUserId(), notification.getProfile().getUserId());
                buttonA.setVisibility(View.GONE);
                buttonD.setVisibility(View.GONE);
                textD.setVisibility(View.GONE);
                textA.setVisibility(View.GONE);
                remove(notification);
            });
            buttonD.setOnClickListener(v -> {
                // Handle decline button click
                DatabaseManager.getInstance().rejectUserFollow(SessionManager.getInstance(getContext()).getUserId(), notification.getProfile().getUserId());
                buttonA.setVisibility(View.GONE);
                buttonD.setVisibility(View.GONE);
                textD.setVisibility(View.GONE);
                textA.setVisibility(View.GONE);
                remove(notification);
            });

        }
        else {
            // Set the text views of the regular notification view.
            buttonA.setVisibility(View.GONE);
            buttonD.setVisibility(View.GONE);
            textD.setVisibility(View.GONE);
            textA.setVisibility(View.GONE);
            emotionView.setVisibility(View.VISIBLE);
            actionView.setText("has posted a mood update:");
            emotionView.setText(notification.getPost().getEmotion().toString());
            emotionView.setTextColor(ContextCompat.getColor(getContext(), notification.getPost().getEmotion().getColor()));
        }
        nameView.setText(notification.getProfile().getUserId());
        dateView.setText(notification.getFormattedPostedDate());
        timeView.setText(notification.getFormattedPostedTime());

        return view;
    }
}

