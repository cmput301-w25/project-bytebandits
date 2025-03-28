package com.github.bytebandits.bithub.model;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
public class MoodMarker implements ClusterItem{
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final MoodPost moodPost;
    public MoodMarker(double lat, double lng, String title, String snippet, MoodPost moodPost) {
        this.moodPost = moodPost;
        this.position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }

    public String getUserId() {
        return this.moodPost.getProfile().getUserId();
    }

    public Emotion getEmotion() {
        return this.moodPost.getEmotion();

    }

    public MoodPost getMoodPost() {
        return this.moodPost;
    }
    @NonNull
    @Override
    public LatLng getPosition() {
        return this.position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return this.title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return this.snippet;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
    }
}
