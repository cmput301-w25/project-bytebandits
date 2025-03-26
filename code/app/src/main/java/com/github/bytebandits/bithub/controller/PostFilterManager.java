package com.github.bytebandits.bithub.controller;

import com.github.bytebandits.bithub.model.MoodPost;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages filtering of mood posts based on specific criteria.
 */
public class PostFilterManager {
    /**
     * Filters a list of mood posts based on the specified mood.
     *
     * @param posts The list of Moodpost objects.
     * @param mood  The mood filter to apply.
     * @return A list of mood posts that match the specified mood.
     */
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
