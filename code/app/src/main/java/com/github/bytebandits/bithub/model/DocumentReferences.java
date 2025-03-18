package com.github.bytebandits.bithub.model;

public enum DocumentReferences {
    FOLLOWERS("followerRefs"),
    NOTIFICATIONS("notificationRefs"),
    POSTS("postRefs");

    private String docRefString;

    DocumentReferences(String docRefString){
        this.docRefString = docRefString;
    }

    public String getDocRefString() {
        return this.docRefString;
    }
}
