package com.github.bytebandits.bithub;

import android.app.AlertDialog;

import android.content.Context;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;

public class SettingsDialog {
    private Button logoutButton;

    private Context context;

    public SettingsDialog(Context context) {
        this.context = context;
    }

    public void showSettingsDialog() {
        // Inflate your custom settings dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.settings_dialog, null);

        // Create the AlertDialog
        AlertDialog settingsDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();


        logoutButton = dialogView.findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(v -> {
            logoutUser();
            settingsDialog.dismiss();  // Close the dialog after logout
        });

        settingsDialog.show();
    }

    private void logoutUser() {
        // Clear the global profile (user logged out)


        // Redirect to login screen (or StartupActivity)

//        startupFragment();
    }
}

