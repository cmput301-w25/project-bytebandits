package com.github.bytebandits.bithub;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.DocumentReferences;
import com.github.bytebandits.bithub.model.Emotion;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Profile;
import com.github.bytebandits.bithub.model.SocialSituation;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    private DatabaseManager dbInstance;
    private Profile testProfile;
    private Profile testProfile2;

    @BeforeClass
    public static void setup() {
        DatabaseManager dbInstance = DatabaseManager.getInstance(true);
        CollectionReference usersCollectionRef = dbInstance.getUsersCollectionRef();

        // Add Users
        HashMap<String, Object> user1 = new HashMap<>();
        user1.put("username", "testUser1");
        user1.put("profile", "{\"userID\":\"testUser1\",\"locationServices\":true,\"image\":null}");
        user1.put("password", "1");
        user1.put("email", "testemail1@gmail.com");

        HashMap<String, Object> user2 = new HashMap<>();
        user2.put("username", "testUser2");
        user2.put("profile", "{\"userID\":\"testUser2\",\"locationServices\":false,\"image\":null}");
        user2.put("password", "2");
        user2.put("email", "testemail2@gmail.com");

        DocumentReference user1DocRef = usersCollectionRef.document((String) Objects.requireNonNull(user1.get("username")));
        DocumentReference user2DocRef = usersCollectionRef.document((String) Objects.requireNonNull(user2.get("username")));

        user1DocRef.set(user1);
        user2DocRef.set(user2);

        Profile testProfile = new Profile((String) Objects.requireNonNull(user1.get("username")));
        testProfile.enableLocationServices();
        SessionManager sessionManager = SessionManager.getInstance(ApplicationProvider.getApplicationContext());
        sessionManager.saveProfile(testProfile);

        // Idk why but for some reason adding a mood post in the set up makes things not break ¯\_(ツ)_/¯
        dbInstance.addPost(new MoodPost(Emotion.SURPRISE, new Profile("ツ"),
                false, null, null, null, false),
                testProfile.getUserID(), Optional.empty());
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        this.dbInstance = DatabaseManager.getInstance(true);
        SessionManager sessionManager = SessionManager.getInstance(ApplicationProvider.getApplicationContext());
        testProfile = sessionManager.getProfile();
        testProfile2 = new Profile("testUser2");

        // Add Posts
        MoodPost[] moodPosts = {
                new MoodPost(Emotion.HAPPINESS, testProfile, true, SocialSituation.ALONE, "This is a description", null, true),
                new MoodPost(Emotion.SADNESS, testProfile2, false, SocialSituation.ALONE, "Test Desc",
                        null, true),
                new MoodPost(Emotion.ANGER, testProfile, false, null, null, null, false),
        };

        for (MoodPost post : moodPosts) {
            if (Objects.equals(post.getProfile().getUserID(), testProfile.getUserID())) {
                dbInstance.addPost(post, testProfile.getUserID(), Optional.empty());
            }
            else {
                dbInstance.addPost(post, testProfile2.getUserID(), Optional.empty());
            }
        }

        // Delay so that movies added in seedDatabase() have a chance to update on
        // firebase's side before we test for them
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Caught exception
        }
    }

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void moodHistory() {
        // Click on the profile icon in the navigation bar
        onView(withId(R.id.profile)).perform(click());

        // Check if mood post list history is displayed
        onView(withId(R.id.mood_post_list_history)).check(matches(isDisplayed()));
    }

    @Test
    public void logout() {
        // Click on the profile icon in the navigation bar
        onView(withId(R.id.profile)).perform(click());
        onView(withId(R.id.settings_button)).perform(click());
        onView(withId(R.id.logout_button)).perform(click());

        // Check if startup activity is displayed
        onView(withId(R.layout.activity_startup)).check(matches(isDisplayed()));
    }

    @Test
    public void navbar() {
        // Click on the Home icon in the navigation bar
        onView(withId(R.id.home)).perform(click());
        // Check if a unique view within the home fragment is displayed
        onView(withId(R.id.moodIcon)).check(matches(isDisplayed()));

        // Click on the Profile icon in the navigation bar
        onView(withId(R.id.profile)).perform(click());
        // Check if a unique view within the profile fragment is displayed
        // onView(withId(R.id.history_textview)).check(matches(isDisplayed()));

        // Click on the Create icon in the navigation bar
        onView(withId(R.id.create)).perform(click());
        // Check if a unique view within the post mood fragment is displayed
        onView(withId(R.id.postMoodCancelButton)).check(matches(isDisplayed()));
    }

    @Test
    public void appShouldDisplayExistingMoodPostsOnLaunch() {
        // Check that the initial data is loaded
        onView(withText("Happiness")).check(matches(isDisplayed()));
        onView(withText("Sadness")).check(matches(isDisplayed()));
        onView(withText("Anger")).check(matches(isDisplayed()));
        // Click on Happiness and check movie details are displayed properly
        onView(withText("Happiness")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Happiness")));
        onView(withId(R.id.detailedViewName)).check(matches(withText("testUser1")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("ALONE")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("This is a description")));
        onView(withText("Back")).perform(click());
        // Click on Sadness and check if movie details are displayed properly
        onView(withText("Sadness")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Sadness")));
        onView(withId(R.id.detailedViewName)).check(matches(withText("testUser2")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("ALONE")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("Test Desc")));
        onView(withText("Back")).perform(click());
        // Click on Anger and check if movie details are displayed properly
        onView(withText("Anger")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Anger")));
        onView(withId(R.id.detailedViewName)).check(matches(withText("testUser1")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("")));
        onView(withText("Back")).perform(click());
    }

    @Test
    public void addMoodPost() {
        // Click on button to open add mood post dialog
        onView(withId(R.id.create)).perform(click());

        // Test invalid description input
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("This is a invalid desc"));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());
        onView(withId(R.id.postMoodDescription))
                .check(matches(hasErrorText("Description can be max 20 characters or 3 words")));

        // Input proper mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("SURPRISE")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("GROUP")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("test desc"));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());

        // Check that our mood post list has our new mood post
        onView(withText("Surprise")).check(matches(isDisplayed()));

        // Check mood post for right details
        onView(withText("Surprise")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Surprise")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("GROUP")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("test desc")));
    }

    @Test
    public void addMoodPostMinData() {
        // Click on button to open add mood post dialog
        onView(withId(R.id.create)).perform(click());

        // Input proper mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("SURPRISE")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("prefer not to say")).perform(click());
        onView(withId(R.id.postMoodConfirmButton)).perform(click());

        // Check that our mood post list has our new mood post
        onView(withText("Surprise")).check(matches(isDisplayed()));

        // Check mood post for right details
        onView(withText("Surprise")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Surprise")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("")));
    }

    @Test
    public void addMoodPostCancel() {
        // Click on button to open add mood post dialog
        onView(withId(R.id.create)).perform(click());

        // Test invalid description input
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("This is a invalid desc"));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());
        onView(withId(R.id.postMoodDescription))
                .check(matches(hasErrorText("Description can be max 20 characters or 3 words")));

        // Input proper mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("SURPRISE")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("GROUP")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("test desc"));
        // Cancel
        onView(withId(R.id.postMoodConfirmButton)).perform(click());

        // Check that our mood post list DOESN'T have our new mood post
        try {
            onView(withText("Surprise")).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Mood post added after cancel");
        } catch (NoMatchingViewException e) {
            // View is not in hierarchy
        }
    }

    @Test
    public void editMoodPostRemoveDetails() {
        // Click on buttons to open edit mood post
        onView(withText("Sadness")).perform(click());
        onView(withText("Edit")).perform(click());

        // Check all details are properly shown
        onView(withId(R.id.postMoodEmotion)).check(matches(withText("SADNESS")));
        onView(withId(R.id.postMoodSocialSituation)).check(matches(withText("ALONE")));
        onView(withId(R.id.postMoodDescription)).check(matches(withText("Test Desc")));

        // Test invalid description input
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("This is a invalid desc"));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());
        onView(withId(R.id.postMoodDescription))
                .check(matches(hasErrorText("Description can be max 20 characters or 3 words")));

        // Input new mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("SURPRISE")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("prefer not to say")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText(""));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());

        // Check that our mood post list has our new mood post
        onView(withText("Surprise")).check(matches(isDisplayed()));

        // Check mood post for right details
        onView(withText("Surprise")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Surprise")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("")));
    }

    @Test
    public void editMoodPostAddDetails() {
        // Click on buttons to open edit mood post
        onView(withText("Happiness")).perform(click());
        onView(withText("Edit")).perform(click());

        // Check all details are properly shown
        onView(withId(R.id.postMoodEmotion)).check(matches(withText("HAPPINESS")));
        onView(withId(R.id.postMoodSocialSituation)).check(matches(withText("prefer not to say")));
        onView(withId(R.id.postMoodDescription)).check(matches(withText("")));

        // Test invalid description input
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("This is a invalid desc"));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());
        onView(withId(R.id.postMoodDescription))
                .check(matches(hasErrorText("Description can be max 20 characters or 3 words")));

        // Input new mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("SURPRISE")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("GROUP")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("test desc"));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());

        // Check that our mood post list has our new mood post
        onView(withText("Surprise")).check(matches(isDisplayed()));

        // Check mood post for right details
        onView(withText("Surprise")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Surprise")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("GROUP")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("test desc")));
    }

    @Test
    public void editMoodPostCancel() {
        // Click on buttons to open edit mood post
        onView(withText("Happiness")).perform(click());
        onView(withText("Edit")).perform(click());

        // Check all details are properly shown
        onView(withId(R.id.postMoodEmotion)).check(matches(withText("HAPPINESS")));
        onView(withId(R.id.postMoodSocialSituation)).check(matches(withText("prefer not to say")));
        onView(withId(R.id.postMoodDescription)).check(matches(withText("")));

        // Test invalid description input
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("This is a invalid desc"));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());
        onView(withId(R.id.postMoodDescription))
                .check(matches(hasErrorText("Description can be max 20 characters or 3 words")));

        // Input new mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("SURPRISE")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("GROUP")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("test desc"));
        onView(withId(R.id.postMoodCancelButton)).perform(click());

        // Check that our mood post list DOESN'T have edited mood post
        try {
            onView(withText("Surprise")).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Mood post edited after cancel");
        } catch (NoMatchingViewException e) {
            // View is not in hierarchy
        }
        onView(withText("Happiness")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Happiness")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("")));
    }

    @Test
    public void deleteMoodPost() {
        // Click on buttons to open and delete mood post
        onView(withText("Happiness")).perform(click());
        onView(withText("Edit")).perform(click());

        // Check that our mood post list DOESN'T have edited mood post
        try {
            onView(withText("Happiness")).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Mood post not deleted");
        } catch (NoMatchingViewException e) {
            // View is not in hierarchy
        }
    }

    @After
    public void cleanUp() {
        // Delete the posts of testUser1 and testUser2
        dbInstance.getUserPosts("testUser1", posts -> {
            for (int i = 0; i < posts.size(); i++) {
                dbInstance.deletePost(posts.get(i).getPostID(), "testUser1", Optional.empty());
            }
            return null;
        });
        dbInstance.getUserPosts("testUser2", posts -> {
            for (int i = 0; i < posts.size(); i++) {
                dbInstance.deletePost(posts.get(i).getPostID(), "testUser2", Optional.empty());
            }
            return null;
        });
    }

    @AfterClass
    public static void tearDown() {
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
