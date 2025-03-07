package com.github.bytebandits.bithub;

import com.google.gson.annotations.SerializedName;


public class Profile {
    @SerializedName("userId")
    private String userId;
    @SerializedName("name")
    private String name;
    @SerializedName("email")
    private String email;

    // Constructor
    public Profile(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}