package com.github.bytebandits.bithub.controller;

import com.github.bytebandits.bithub.model.MoodPost;

import java.util.ArrayList;
import java.util.List;

public class PostFilterManager {
    public static List<MoodPost> filterPostsByMood(List<MoodPost> posts, String mood) {
        List<MoodPost> filteredPosts = new ArrayList<>();

        if (mood.equals("all")) {
            filteredPosts.addAll(posts);
        } else {
            for (MoodPost post : posts) {
                if (post.getEmotionString().equalsIgnoreCase(mood)) {
                    filteredPosts.add(post);
                }
            }
        }
        return filteredPosts;
    }
}
