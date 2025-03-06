package com.github.bytebandits.bithub;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Profile {
    private String userID;
    private Boolean locationServices;
    private Bitmap image = null;

    public String getUserID() {
        return userID;
    }

    public void enableLocationServices() {
        this.locationServices = true;
    }

    public void disableLocationServices() {
        this.locationServices = false;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
