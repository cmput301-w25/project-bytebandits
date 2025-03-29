package com.github.bytebandits.bithub.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Notification implements Serializable {
    private String notificationID;
    private MoodPost post;
    private boolean followRequest;
    private Profile profile;
    private Date dateTime;

    public Notification() {}

    public Notification(String message, MoodPost post, boolean notificationType, Profile profile) {
        this.notificationID = UUID.randomUUID().toString();
        this.post = post;
        this.followRequest = notificationType;
        this.profile = profile;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public boolean getNotificationType() {
        return followRequest;
    }

    public void followRequest(boolean notificationType) {
        this.followRequest = notificationType;
    }

    public MoodPost getPost() {
        return post;
    }

    public Profile getProfile() {
        return profile;
    }

    public Date getDateTime() {
        return dateTime;
    }

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

    public void setMoodPost(MoodPost post) {
        this.post = post;
        this.dateTime = new Date();
        this.followRequest = false;
        this.notificationID = UUID.randomUUID().toString();
        this.profile = post.getProfile();
    }
}
