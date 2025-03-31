package com.github.bytebandits.bithub.model;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Represents a mood marker
 */
public class MoodMarker implements ClusterItem{
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final MoodPost moodPost;

    /**
     * Constructor for mood markers
     * @param lat double
     * @param lng double
     * @param title String
     * @param snippet String
     * @param moodPost MoodPost
     */
    public MoodMarker(double lat, double lng, String title, String snippet, MoodPost moodPost) {
        this.moodPost = moodPost;
        this.position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }

    /**
     * Constructor for mood markers
     * @return String
     */

    public String getUserId() {
        return this.moodPost.getProfile().getUserId();
    }

    /**
     * Constructor for mood markers
     * @return String
     */
    public Emotion getEmotion() {
        return this.moodPost.getEmotion();

    }

    /**
     * Constructor for mood markers
     * @return MoodPost
     */
    public MoodPost getMoodPost() {
        return this.moodPost;
    }

    /**
     * Constructor for mood markers
     * @return LatLng
     */
    @NonNull
    @Override
    public LatLng getPosition() {
        return this.position;
    }

    /**
     * Constructor for mood markers
     * @return String
     */
    @Nullable
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * Constructor for mood markers
     * @return String
     */
    @Nullable
    @Override
    public String getSnippet() {
        return this.snippet;
    }

    /**
     * Constructor for mood markers
     * @return Float
     */
    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
    }
}
