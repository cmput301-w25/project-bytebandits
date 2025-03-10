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

    /**
     * Constructs a Profile object with the specified user ID.
     *
     * @param userID The unique identifier for the user.
     */
    public Profile(String userID) {
        this.userID = userID;
    }

    /**
     * Constructs a Profile object with the specified user ID and profile picture.
     *
     * @param userID The unique identifier for the user.
     * @param image The user's profile picture.
     */
    public Profile(String userID, Bitmap image) {
        this.userID = userID;
        this.image = image;
    }


    /**
     * Returns the current user ID.
     *
     * @return The user's unique identifier.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Enables the user's location services.
     */
    public void enableLocationServices() {
        this.locationServices = true;
    }

    /**
     * Disables the user's location services.
     */
    public void disableLocationServices() {
        this.locationServices = false;
    }

    /**
     * Returns the user's profile picture.
     *
     * @return The user's profile picture
     */
    public Bitmap getImage() {
        return image;
    }

    /**
     * Sets the user's profile picture.
     *
     * @param image The new profile picture
     */
    public void setImage(Bitmap image) {
        this.image = image;
    }
}
