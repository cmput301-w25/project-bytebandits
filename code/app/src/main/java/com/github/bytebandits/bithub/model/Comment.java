package com.github.bytebandits.bithub.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * This is a class that represents a comment on a mood post
 * @author Tony Yang
 */
public class Comment{
    private String commentID;
    private Profile profile;
    private Date dateTime;
    private String text;

    public Comment() {}

    /**
     * Constructor to make a comment
     * @param profile
     *      Profile object representing the profile of the user who posted the comment
     *      Cannot be null.
     * @param text
     *      String object representing the text of the comment
     *      Cannot be null
     */
    public Comment(Profile profile, String text) {
        this.commentID = UUID.randomUUID().toString();
        this.profile = profile;
        this.dateTime = new Date();
        this.text = text;
    }

    /**
     * Returns the comment's ID for database storage
     * @return
     *      Returns a String object representing the comment's ID
     */
    public String getCommentID() {
        return commentID;
    }

    /**
     * Returns the profile of the comment
     * @return
     *      Returns a Profile object representing the profile of the user who posted the comment
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * Sets the comment's profile
     * @param profile
     *      Profile object representing the profile of the user who posted the comment
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    /**
     * Returns the comment's profile's UserID/Username for database storage and display
     * @return
     *      Returns a String object representing the comment's profile's username
     */
    public String getUsername() {
        return getProfile().getUserID();
    }

    /**
     * Returns the comment's date and time posted
     * @return
     *      Returns a Date object representing the date and time the comment was posted.
     */
    public Date getPostedDateTime() {
        return dateTime;
    }

    /**
     * Sets the comment's date and time posted
     * @param dateTime
     *      Date object representing the date and time the comment was posted.
     */
    public void setPostedDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Returns the comment's date in a formatted string
     * @return
     *      Returns a formatted String object representing the comment's date posted
     */
    public String getFormattedPostedDate() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM. dd, yyyy");
        return dateFormatter.format(getPostedDateTime());
    }

    /**
     * Returns the comment's time in a formatted string
     * @return
     *      Returns a formatted String object representing the comment's time posted
     */
    public String getFormattedPostedTime() {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mma");
        return timeFormatter.format(getPostedDateTime());
    }

    /**
     * Returns the comment's attached text
     * @return
     *      Returns a String object representing the text of the comment.
     */
    public String getText() { return text; }

    /**
     * Sets the comment's attached text
     * @param text
     *      String object representing the text of the comment.
     */
    public void setText(String text) {
        this.text = text;
    }

}
