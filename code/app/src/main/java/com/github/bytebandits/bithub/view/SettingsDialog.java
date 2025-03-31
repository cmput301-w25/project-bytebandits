package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.Profile;

/**
 * This class handles the display and functionality of the settings dialog in the Bithub application.
 * It is responsible for:
 * - Displaying a custom settings dialog using an AlertDialog.
 * - Providing a logout button to clear user data and redirect to the startup screen.
 */

public class SettingsDialog {
    private Button logoutButton;
    private CheckBox locationServices;
    private Context context;
    private Profile userProfile;
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

        logoutButton = dialogView.findViewById(R.id.logout_button);
        locationServices = dialogView.findViewById(R.id.location_services_checkbox);

        logoutButton.setOnClickListener(v -> {
            logoutUser();
            settingsDialog.dismiss();  // Close the dialog after logout
        });

        // Set checkbox state based on user's current location service setting
        userProfile = SessionManager.getInstance(context).getProfile();
        if (userProfile != null) {
            // Set checkbox state based on user's current location service setting
            locationServices.setChecked(userProfile.getLocationServices());
        }
        locationServices.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SessionManager sessionManagerIns = SessionManager.getInstance(context);
                if (isChecked) {
                    // Location Services Enabled
                    userProfile.enableLocationServices();
                    Toast.makeText(context, "Location Services Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    // Location Services Disabled
                    userProfile.disableLocationServices();
                    Toast.makeText(context, "Location Services Disabled", Toast.LENGTH_SHORT).show();
                }
                sessionManagerIns.saveProfile(userProfile);
                DatabaseManager.getInstance().getUsersCollectionRef().document(sessionManagerIns.getUserId()).update(
                        "profile", userProfile.toJson()
                );
            }
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