package com.github.bytebandits.bithub.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * This class represents a user profile in the Bithub application.
 * It stores and manages user-related information, including:
 * - A unique user ID.
 * - Location service preferences.
 * - The user's profile image.
 * Provides methods to access and modify these attributes.
 */

public class Profile implements Serializable {
    private String userID;
    private Boolean locationServices = false;
    private Bitmap image = null;

    public Profile() {
    }

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
     * Returns the location service status.
     *
     * @return a boolean representing the location service status
     */
    public boolean getLocationServices() {
        return locationServices;
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


    /**
     * Converts the profile details to JSON
     *
     * @return profile JSON
     */
    public String toJson() {
        JSONObject json = new JSONObject();
        try {
            // Add userID if it's not null, otherwise use JSONObject.NULL
            json.put("userID", userID != null ? userID : JSONObject.NULL);

            // Add locationServices if it's not null, otherwise use JSONObject.NULL
            json.put("locationServices", locationServices != null ? locationServices : JSONObject.NULL);

            // Convert Bitmap to Base64 if it's not null
            if (image != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                json.put("image", encodedImage);
            } else {
                json.put("image", JSONObject.NULL);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


        /**
         * Converts a JSON string into a Profile object.
         * @param jsonString JSON representation of the Profile.
         * @return A Profile object or null if parsing fails.
         */
    public Profile fromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);

            // Update locationServices
            this.locationServices = json.has("locationServices") && !json.isNull("locationServices")
                    ? json.getBoolean("locationServices") : null;

            // Decode and update image if present
            if (json.has("image") && !json.isNull("image")) {
                String encodedImage = json.getString("image");
                byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                this.image = BitmapFactory.decodeStream(new ByteArrayInputStream(decodedBytes));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }
}
