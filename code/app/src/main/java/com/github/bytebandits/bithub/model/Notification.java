package com.github.bytebandits.bithub.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Represents a notification
 */
public class Notification implements Serializable {
    private String notificationID;
    private MoodPost post;
    private boolean followRequest;
    private Profile profile;
    private Date dateTime;

    public Notification() {}

    /**
     * Constructor for notifications
     *
     * @param message message
     * @param post post
     * @param notificationType notificationType
     * @param profile profile
     */
    public Notification(String message, MoodPost post, boolean notificationType, Profile profile) {
        this.notificationID = UUID.randomUUID().toString();
        this.post = post;
        this.followRequest = notificationType;
        this.profile = profile;
    }

    /**
     * Constructor for notifications
     * @return notificationID String
     */
    public String getNotificationID() {
        return notificationID;
    }

    /**
     * Constructor for notifications
     * @return followRequest boolean
     */
    public boolean getNotificationType() {
        return followRequest;
    }

    /**
     * Constructor for notifications
     * @param notificationType notificationType
     */
    public void followRequest(boolean notificationType) {
        this.followRequest = notificationType;
    }

    /**
     * Constructor for notifications
     * @return post MoodPost
     */
    public MoodPost getPost() {
        return post;
    }

    /**
     * Constructor for notifications
     * @return profile Profile
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * Constructor for notifications
     * @return dateTime Date
     */
    public Date getDateTime() {
        return dateTime;
    }

    /**
     * Constructor for notifications
     * @param dateTime Date
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Returns the mood post's date in a formatted string
     * @return
     *      Returns a formatted String object representing the mood post's date posted
     */
    public String getFormattedPostedDate() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM. dd, yyyy");
        return dateFormatter.format(getDateTime());
    }

    /**
     * Returns the mood post's time in a formatted string
     * @return
     *      Returns a formatted String object representing the mood post's time posted
     */
    public String getFormattedPostedTime() {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mma");
        return timeFormatter.format(getDateTime());
    }

    /**
     * Sets the mood post
     * @param post MoodPost
     */
    public void setMoodPost(MoodPost post) {
        this.post = post;
        this.dateTime = new Date();
        this.followRequest = false;
        this.notificationID = UUID.randomUUID().toString();
        this.profile = post.getProfile();
    }

    /**
     * Sets the request
     * @param request HashMap<String, Object>
     */
    public void setRequest(HashMap<String, Object> request) {
        this.followRequest = true;
        this.dateTime = new Date();
        this.notificationID = UUID.randomUUID().toString();
        this.profile = new Profile(request.get("userId").toString());
    }
}
