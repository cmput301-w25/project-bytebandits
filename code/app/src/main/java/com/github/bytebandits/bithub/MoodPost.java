package com.github.bytebandits.bithub;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.Date;
import java.util.UUID;

/**
 * This is a class that represents a mood post
 * @author Tony Yang
 */

public class MoodPost {
    private UUID postID;
    private Emotion emotion;
    // private Profile profile; // Profile class not implemented yet
    // REMEMBER TO ADD PROFILE GETTERS AND SETTERS AND ADD IT TO THE CONSTRUCTOR AND CONSTRUCTOR JAVADOC
    private Date dateTime;
    private Location location;
    private SocialSituation situation;
    private String desc;
    private String imageURL; // URL to download the attached image from the Firestore Storage

    /**
     * Constructor to make a mood post
     * @param emotion
     *      Emotion object representing the emotion of the mood post.
     *      Cannot be null.
     * @param location
     *      Location object representing the location of the mood post.
     *      When null is passed, it means that no location is attached to the mood post.
     * @param situation
     *      SocialSituation object representing the social situation of the mood post.
     *      When null is passed, it means that no social situation is attached to the mood post.
     * @param desc
     *      String object representing a short description of the mood post.
     *      Can be a max of 20 characters or 3 words.
     *      When null is passed, it means that no description is attached to the mood post.
     * @param imageURL
     *      String object representing the URL to download the attached mood post image from the
     *      Firestore storage.
     *      When null is passed, it means that no image is attached to the mood post.
     */
    public MoodPost(Emotion emotion, Location location, SocialSituation situation,
                    String desc, String imageURL) {
        this.postID = UUID.randomUUID();
        this.emotion = emotion;
        this.dateTime = new Date();
        this.location = location;
        this.situation = situation;
        this.desc = desc;
        this.imageURL = imageURL;
    }

    /**
     * Returns the mood post's ID
     * @return
     *      Returns a UUID representing the mood post's ID.
     */
    public UUID getPostID() {
        return postID;
    }

    /**
     * Returns the emotion of the mood post
     * @return
     *      Returns a Emotion object representing the emotion of the mood post.
     */
    public Emotion getEmotion() {
        return emotion;
    }

    /**
     * Sets the mood post's emotion
     * @param emotion
     *      Emotion object representing the emotion of the mood post.
     */
    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    /**
     * Returns the mood post's date and time posted
     * @return
     *      Returns a Date object representing the date and time the mood post was posted.
     */
    public Date getPostedDateTime() {
        return dateTime;
    }

    /**
     * Sets the mood post's date and time posted
     * @param dateTime
     *      Date object representing the date and time the mood post was posted.
     */
    public void setPostedDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Returns the mood post's attached location
     * @return
     *      Returns a Location object representing the mood post's attached location.
     *      Returns null when the mood post has no attached location.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the mood post's attached location
     * @param location
     *      Location object representing the mood post's attached location.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Returns the mood post's social situation
     * @return
     *      Returns a SocialSituation object representing the mood post's social situation.
     *      Returns null when the mood post has no attached social situation.
     */
    public SocialSituation getSocialSituation() {
        return situation;
    }

    /**
     * Sets the mood post's social situation
     * @param situation
     *      SocialSituation object representing the mood post's social situation.
     */
    public void setSocialSituation(SocialSituation situation) {
        this.situation = situation;
    }

    /**
     * Returns the mood post's attached description
     * @return
     *      Returns a String object representing a short description of the mood post.
     *      Returns null when the mood post has no attached description.
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Sets the mood post's description
     * @param desc
     *      String object representing a short description of the mood post.
     *      Can be a max of 20 characters or 3 words.
     */
    public void setDescription(String desc) {
        this.desc = desc;
    }

    /**
     * Returns the mood post's attached image download URL
     * @return
     *      Returns a String object representing the URL to download the
     *      attached mood post image from the Firestore storage.
     *      Returns null when the mood post has no attached image.
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * Sets the mood post's attached image download URL
     * @param imageURL
     *      String object representing the URL to download the
     *      attached mood post image from the Firestore storage.
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    /**
     * Returns the mood post's attached image
     * @return
     *      Returns a bitmap of the mood post's attached image.
     *      Returns null when the mood post has no attached image.
     */
    public Bitmap getImage() {
        // Check if there is an attached image
        if (getImageURL() == null) { return null; }
        // TODO: get image from url and return as bitmap
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8); // Return arbitrary bitmap for now
    }
}
