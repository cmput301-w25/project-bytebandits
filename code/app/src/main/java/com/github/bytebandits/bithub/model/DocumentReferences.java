package com.github.bytebandits.bithub.model;

public enum DocumentReferences {
    FOLLOWERS("followerRefs"),
    FOLLOWINGS("followingRefs"),
    NOTIFICATION_POSTS("notificationRefs.posts"),
    NOTIFICATION_REQS("notificationRefs.requests"),
    POSTS("postRefs");

    private String docRefString;

    DocumentReferences(String docRefString){
        this.docRefString = docRefString;
    }

    public String getDocRefString() {
        return this.docRefString;
    }
}
