package com.github.bytebandits.bithub;

import android.location.Location;
import android.util.Base64;

import java.text.SimpleDateFormat;
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

    //temp variable for username
    private String username; //REMOVE LATER
    private Date dateTime;
    private Location location;
    private SocialSituation situation;
    private String desc;
    private byte[] image;

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
     * @param image
     *      A byte array representing the image attached to the mood post
     *      When null is passed, it means that no image is attached to the mood post.
     */
    public MoodPost(Emotion emotion, String username, Location location, SocialSituation situation,
                    String desc, byte[] image) {
        this.postID = UUID.randomUUID();
        this.emotion = emotion;
        this.username = username;
        this.dateTime = new Date();
        this.location = location;
        this.situation = situation;
        this.desc = desc;
        this.image = image;
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
     * Returns the mood post's ID as a string for database storage
     * @return
     *      Returns a String object representing the mood post's ID
     */
    public String getPostIDString() {
        return getPostID().toString();
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
     * Returns the mood post's emotion's name as a string for database storage
     * @return
     *      Returns a String object representing the mood post's emotion's name (ex. "SADNESS")
     */
    public String getEmotionString() {
        return getEmotion().name();
    }

    //REMOVE LATER, temp getters and setters for username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
     * Returns the mood post's date and time posted as a string for database storage
     * @return
     *      Returns a String object representing the mood post's date and time posted
     */
    public String getPostedDateTimeString() {
        // Format the string before returning
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return formatter.format(getPostedDateTime());

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
     * Returns the mood post's attached location as a string for database storage
     * @return
     *      Returns a String object representing the mood post's attached location.
     *      Returns null when the mood post has no attached location.
     */
    public String getLocationString() {
        if (getLocation() == null) { return null; }
        return getLocation().getLatitude() + "," + getLocation().getLongitude();
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
     * Returns the mood post's social situation's name as a string for database storage
     * @return
     *      Returns a String object representing the mood post's social situation's name (ex. "ALONE")
     *      Returns null when the mood post has no attached social situation.
     */
    public String getSocialSituationString() {
        if (getSocialSituation() == null) { return null; }
        return getSocialSituation().name();
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
     * Returns the mood post's attached image
     *
     * @return
     *      Returns a byte array representing the image attached to the mood post
     *      Returns null when the mood post has no attached image.
     */
    public byte[] getMoodImage() {
        return image;
    }

    /**
     * Sets the mood post's attached image
     * @param image
     *      byte array representing the image attached to the mood post
     */
    public void setMoodImage(byte[] image) {
        this.image = image;
    }

    /**
     * Returns the mood post's attached image as a string for database storage
     *
     * @return
     *      Returns a Base64 String representing the image
     *      Returns null when the mood post has no attached image.
     */
    public String getMoodImageString() {
        // Check if there is an attached image
        if (getMoodImage() == null) { return null; }
        return Base64.encodeToString(getMoodImage(), Base64.DEFAULT);
    }
}
