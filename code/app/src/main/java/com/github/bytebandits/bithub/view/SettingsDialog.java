package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.SessionManager;

/**
 * This class handles the display and functionality of the settings dialog in the Bithub application.
 * It is responsible for:
 * - Displaying a custom settings dialog using an AlertDialog.
 * - Providing a logout button to clear user data and redirect to the startup screen.
 */

public class SettingsDialog {
    private Button logoutButton;
    private EditText nameText;
    private EditText emailText;
    private Context context;

    /**
     * Constructs a SettingsDialog instance with the provided context.
     *
     * @param context The context used to display the dialog.
     */
    public SettingsDialog(Context context) {
        this.context = context;
    }

    /**
     * Displays the custom settings dialog.
     */
    public void showSettingsDialog() {
        // Inflate your custom settings dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.settings_dialog, null);

        // Create the AlertDialog
        AlertDialog settingsDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        nameText = dialogView.findViewById(R.id.name_edit_text);
        emailText = dialogView.findViewById(R.id.email_edit_text);
        logoutButton = dialogView.findViewById(R.id.logout_button);

//        String currentUsername = SessionManager.getUsername();
//        String currentEmail = SessionManager;

        logoutButton.setOnClickListener(v -> {
            logoutUser();
            settingsDialog.dismiss();  // Close the dialog after logout
        });

        settingsDialog.show();
    }

    /**
     * Handles user logout by clearing user data and redirecting to the startup screen.
     */
    private void logoutUser() {
        SessionManager.getInstance(context).logoutUser();
    }
}