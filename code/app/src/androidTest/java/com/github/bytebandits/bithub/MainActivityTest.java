package com.github.bytebandits.bithub;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @BeforeClass
    public static void setup(){
        //  address for emulated device
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        CollectionReference moodPostRef = db.collection("posts");

        // mock posts
        MoodPost[] moodPosts = {
                new MoodPost(Emotion.HAPPINESS, new Profile("Zaid"), false, SocialSituation.ALONE, "Test", null),
                new MoodPost(Emotion.SADNESS, new Profile("Zaid"), false, SocialSituation.ALONE, "Test", null),
        };
        for (MoodPost moodPost : moodPosts) {
            moodPostRef.document().set(moodPost);
        }
    }

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void moodHistory(){
        // navigate to profile page
        onView(withId(R.id.profile)).perform(click());

        // check if mood post list history is displayed
        onView(withId(R.id.mood_post_list_history)).check(matches(isDisplayed()));
    }

    @Test
    public void logout() {
        // navigate to the settings dialog and click logout button
        onView(withId(R.id.profile)).perform(click());
        onView(withId(R.id.settings_button)).perform(click());
        onView(withId(R.id.logout_button)).perform(click());

        // check if startup actvity is displayed
        onView(withId(R.layout.activity_startup)).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        String projectId = "byte-bandits-project";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
