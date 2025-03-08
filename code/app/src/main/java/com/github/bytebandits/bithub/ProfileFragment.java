package com.github.bytebandits.bithub;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class ProfileFragment extends Fragment {

    ImageButton settingsButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        settingsButton = view.findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(v -> openSettings());


        return view;
    }

    // Displays the settings dialog
    private void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(requireContext());
        settingsDialog.showSettingsDialog();
    }

}