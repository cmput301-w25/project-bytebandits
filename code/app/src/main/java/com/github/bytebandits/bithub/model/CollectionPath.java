package com.github.bytebandits.bithub.model;

public enum CollectionPath {
    POSTS("posts"),
    USERS("users");

    private String collectionString;

    CollectionPath(String collectionString){
        this.collectionString = collectionString;
    }

    public String getCollectionString() {
        return this.collectionString;
    }
}
