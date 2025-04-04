package com.github.bytebandits.bithub.model;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * This is a class that represents a mood post
 * @author Tony Yang
 */

public class MoodPost implements Serializable {
    private String postID;
    private Emotion emotion;
    private Profile profile;
    private Date dateTime;
    private Boolean location;
    private Double latitude;
    private Double longitude;
    private SocialSituation situation;
    private String desc;
    private String image;
    private ArrayList<Comment> comments;
    private boolean isPrivate;

    public MoodPost() {}

    /**
     * Constructor to make a mood post
     * @param emotion
     *      Emotion object representing the emotion of the mood post.
     *      Cannot be null.
     * @param profile
     *      Profile object representing the profile of the user who posted the mood post
     *      Cannot be null.
     * @param showLocation
     *      boolean representing whether or not we should attach our current location to the post
     * @param situation
     *      SocialSituation object representing the social situation of the mood post.
     *      When null is passed, it means that no social situation is attached to the mood post.
     * @param desc
     *      String object representing a short description of the mood post.
     *      Can be a max of 20 characters or 3 words.
     *      When null is passed, it means that no description is attached to the mood post.
     * @param image
     *      A Base64 string representing the image attached to the mood post.
     *      When null is passed, it means that no image is attached to the mood post.
     * @param isPrivate
     *      boolean representing whether or not this mood is public or private.
     */
    public MoodPost(Emotion emotion, Profile profile, boolean showLocation, SocialSituation situation,
                    String desc, String image, boolean isPrivate) {
        this.postID = UUID.randomUUID().toString();
        this.emotion = emotion;
        this.profile = profile;
        this.dateTime = new Date();
        this.location = showLocation;
        this.situation = situation;
        this.desc = desc;
        this.image = image;
        this.comments = new ArrayList<>();
        this.isPrivate = isPrivate;
    }

    /**
     * Returns the mood post's ID as a string
     * @return
     *      Returns a String object representing the mood post's ID
     */
    public String getPostID() {
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
     * Returns the profile of the mood post
     * @return
     *      Returns a Profile object representing the profile of the user who posted the mood post
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * Sets the mood post's profile
     * @param profile
     *      Profile object representing the profile of the user who posted the mood post
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
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
     * Returns the mood post's date in a formatted string
     * @return
     *      Returns a formatted String object representing the mood post's date posted
     */
    public String getFormattedPostedDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM. dd, yyyy");
        return dateFormatter.format(getPostedDateTime());
    }

    /**
     * Returns the mood post's time in a formatted string
     * @return
     *      Returns a formatted String object representing the mood post's time posted
     */
    public String getFormattedPostedTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mma");
        return timeFormatter.format(getPostedDateTime());
    }

    /**
     * Returns the mood post's attached latitude
     * @return
     *      Returns a Double object representing the mood post's attached latitude.
     *      Returns null when the mood post has no attached location.
     */
    public Double getLatitude() {
        if (location) { return latitude; }
        else { return null; }
    }

    /**
     * Returns the mood post's attached longitude
     * @return
     *      Returns a Double object representing the mood post's attached latitude.
     *      Returns null when the mood post has no attached location.
     */
    public Double getLongitude() {
        if (location) { return longitude; }
        else { return null; }
    }

    /**
     * Sets the mood post's attached Latitude
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Sets the mood post's attached Longitude
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the mood post's show location status
     * @return
     *      Returns a Boolean object representing the mood post's show location status.
     */
    public boolean getLocation() {
        return location;
    }


    /**
     * Enables the mood post's attached location
     */
    public void showLocation() {
        this.location = true;
    }

    /**
     * Disables the mood post's attached location
     */
    public void hideLocation() {
        this.location = false;
    }

    /**
     * Returns the mood post's attached location as a string for display
     * @return
     *      Returns a String object representing the mood post's attached location.
     *      Returns null when the mood post has no attached location.
     */
    public String getLocationString() {
        if (getLatitude() == null || getLongitude() == null) { return null; }
        else { return getLatitude() + "," + getLongitude(); }
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
     * Returns the mood post's attached image as a Base 64 String
     *
     * @return
     *      Returns a Base 64 String representing the image attached to the mood post
     *      Returns null when the mood post has no attached image.
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the mood post's attached image
     * @param image
     *      Base64 String representing the image attached to the mood post
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Returns the mood post's comments
     *
     * @return
     *      Returns a ArrayList of Comment objects representing the comments of the mood post
     */
    public ArrayList<Comment> getComments() { return comments; }

    /**
     * Adds a comment to the mood post's comments
     * @param comment
     *      Comment object representing the comment we want to add
     */
    public void addComment(Comment comment) {
        comments.add(comment);
    }

    /**
     * Deletes a comment on the mood post's comments
     * @param comment
     *      Comment object representing the comment we want to delete
     */
    public void deleteComment(Comment comment) {
        // Loop through comments and find matching comment id and delete it
        for (int i = 0; i < comments.size(); i++) {
            if (Objects.equals(comments.get(i).getCommentID(), comment.getCommentID())) {
                comments.remove(i);
                break;
            }
        }
    }

    /**
     * Returns if the mood post is private or not
     * @return
     *      A boolean representing if the mood post is public or private
     */
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * Sets the mood post to be public or private
     * @param aPublic
     *      A boolean representing if we want the mood post to be public or private. True for public, false for private
     */
    public void setPrivate(boolean aPublic) {
        isPrivate = aPublic;
    }
}
