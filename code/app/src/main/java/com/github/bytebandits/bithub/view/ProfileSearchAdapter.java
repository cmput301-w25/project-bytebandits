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
import com.github.bytebandits.bithub.model.Profile;

import java.util.ArrayList;

/**
 * Adapter that binds profile data and renders it to some layout and view
 */
public class ProfileSearchAdapter extends ArrayAdapter<Profile> {

    public ProfileSearchAdapter(@NonNull Context context, @NonNull ArrayList<Profile> profiles) {
        super(context, 0, profiles);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.profile_text_result,
                    parent, false);
        } else {
            view = convertView;
        }
        Profile profile = getItem(position);
        TextView username = view.findViewById(R.id.profileUsername);
        username.setText(profile.getUserID());

        return view;
    }
}