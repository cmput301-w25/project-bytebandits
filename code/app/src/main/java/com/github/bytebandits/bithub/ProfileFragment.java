package com.github.bytebandits.bithub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * This fragment represents the user's profile screen in the Bithub application.
 * It is responsible for:
 * - Displaying the profile layout.
 * - Providing access to the settings dialog via a settings button.
 */

public class ProfileFragment extends Fragment {

    ImageButton settingsButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);  // display profile fragment layout

        settingsButton = view.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> openSettings());

        return view;
    }

    // Displays the settings dialog if settings icon clicked
    private void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(requireContext());
        settingsDialog.showSettingsDialog();
    }
}