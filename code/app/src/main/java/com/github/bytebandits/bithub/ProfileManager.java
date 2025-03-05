package com.github.bytebandits.bithub;

import java.util.ArrayList;

public class ProfileManager {
    private ArrayList<Profile> followers;
    private ArrayList<Profile> following;

    public void notifyFollowers(MoodPost post) {

    }

    public void requestToFollowProfile(Profile profile) {

    }

    public ArrayList<Profile> getFollowing() {
        return following;
    }

    public ArrayList<Profile> getFollowers() {
        return followers;
    }

}
