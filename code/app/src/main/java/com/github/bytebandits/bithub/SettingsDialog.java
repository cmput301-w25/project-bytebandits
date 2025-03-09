package com.github.bytebandits.bithub;

import android.app.AlertDialog;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;

/**
 * This class handles the display and functionality of the settings dialog in the Bithub application.
 * It is responsible for:
 * - Displaying a custom settings dialog using an AlertDialog.
 * - Providing a logout button to clear user data and redirect to the startup screen.
 */

 public class SettingsDialog {
    private Button logoutButton;

    private Context context;

    // constructor
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

    // load the startup activity and set the current user to null when the user clicks the logout button
    private void logoutUser() {
        // Clear the global profile (user logged out)


        // Redirect to login screen (or StartupActivity)

//        startupFragment();
    }
}

