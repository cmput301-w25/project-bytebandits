package com.github.bytebandits.bithub.model;

import com.github.bytebandits.bithub.R;

/**
 * This is a enumeration that represents each possible emotion for a mood post
 * @author Tony Yang
 */

public enum Emotion {
    ANGER("Anger", "intense feeling of displeasure", R.drawable.anger_logo),
    CONFUSION("Confusion", "state of being uncertain or baffled", R.drawable.confusion_logo),
    DISGUST("Disgust", "feeling of revulsion and or disapproval", R.drawable.disgust_logo),
    FEAR("Fear", "  anxiousness from a perceived threat", R.drawable.fear_logo),
    HAPPINESS("Happiness", "feeling of pleasure or contentment", R.drawable.happiness_logo),
    SADNESS("Sadness", "feeling or showing of sorrow", R.drawable.sadness_logo),
    SHAME("Shame", "feeling of humiliation and guilt", R.drawable.shame_logo),
    SURPRISE("Surprise", "state of shock from a sudden event", R.drawable.surprise_logo);

    private String state;
    private String description;
    private int logoID; // ID to reference the mood logo

    /**
     * Enumerator constructor to initialize the different emotions
     * @param state
     *      String representing the emotion's name
     * @param description
     *      String representing a short description describing the emotion (max 40 char.)
     * @param logoID
     *      Int representing the ID to use to reference the emotion's logo
     */
    Emotion(String state, String description, int logoID) {
        this.state = state;
        this.description = description;
        this.logoID = logoID;
    }

    /**
     * Returns the name of the emotion
     * @return
     *      Returns a string representing the emotion's name
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the description of the emotion
     * @return
     *      Returns a string representing a short description describing the emotion
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the ID of the emotion's logo
     * @return
     *      Returns a int representing the ID to use to reference the emotion's logo
     */
    public int getLogoID() {
        return logoID;
    }
}
