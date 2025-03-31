package com.github.bytebandits.bithub.model;

/**
 * DocumentReferences enum used to have consistent values for the database functions
 *
 * @author Michael Tran
 */
public enum DocumentReferences {
    FOLLOWERS("followerRefs"),
    FOLLOWINGS("followingRefs"),
    NOTIFICATION_POSTS("notificationRefs.posts"),
    NOTIFICATION_REQS("notificationRefs.requests"),
    POSTS("postRefs");

    private final String docRefString;

    DocumentReferences(String docRefString){
        this.docRefString = docRefString;
    }

    public String getDocRefString() {
        return this.docRefString;
    }
}
