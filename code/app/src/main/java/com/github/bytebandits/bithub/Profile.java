package com.github.bytebandits.bithub;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Profile {
    private User user;
    private HashMap<UUID, MoodPost> posts;
    private ArrayList<Object> inbox;
    private ProfileManager pManager;
    private Bitmap image = null;

    public Profile(User user) {
        this.user = user;
    }
    public Profile(User user, Bitmap image) {
        this.user = user;
        this.image = image;
    }

    public User getUser() {
        return user;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public ProfileManager getPManager() {
        return pManager;
    }
    public void editPost(UUID postID, HashMap<String, Object> options) {

    }
    public void addPost(MoodPost post) {

    }
    public void deletePost(UUID postID) {

    }
}
