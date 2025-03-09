package com.github.bytebandits.bithub;

import android.graphics.Bitmap;

/**
 * This class represents a user profile in the Bithub application.
 * It stores and manages user-related information, including:
 * - A unique user ID.
 * - Location service preferences.
 * - The user's profile image.
 * Provides methods to access and modify these attributes.
 */

public class Profile {
    private String userID;
    private Boolean locationServices;
    private Bitmap image = null;

    // returns the current user ID
    public String getUserID() {
        return userID;
    }

    // enables user's location services
    public void enableLocationServices() {
        this.locationServices = true;
    }

    // disables user's location services
    public void disableLocationServices() {
        this.locationServices = false;
    }

    // returns the user's profile picture
    public Bitmap getImage() {
        return image;
    }

    // sets te user's profile picture
    public void setImage(Bitmap image) {
        this.image = image;
    }
}
