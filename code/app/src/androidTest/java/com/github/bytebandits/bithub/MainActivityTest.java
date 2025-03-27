package com.github.bytebandits.bithub;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.Comment;
import com.github.bytebandits.bithub.model.Emotion;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Profile;
import com.github.bytebandits.bithub.model.SocialSituation;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import junit.framework.AssertionFailedError;

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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    private DatabaseManager dbInstance;
    private Context testContext;
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

        // Idk why but for some reason adding a mood post in the set up makes things not break ¯\_(ツ)_/¯
        Profile randProfile = new Profile("ツ");
        dbInstance.addPost(new MoodPost(Emotion.SURPRISE, randProfile,
                false, null, null, null, false),
                randProfile.getUserID(), Optional.empty());
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        this.dbInstance = DatabaseManager.getInstance(true);
        testContext = ApplicationProvider.getApplicationContext();
        SessionManager sessionManager = SessionManager.getInstance(testContext);
        testProfile = new Profile("testUser1");
        testProfile.enableLocationServices();
        testProfile2 = new Profile("testUser2");
        sessionManager.saveProfile(testProfile);

        // Add Posts
        MoodPost[] moodPosts = {
                new MoodPost(Emotion.HAPPINESS, testProfile, true, SocialSituation.ALONE, "This is a description", null, true),
                new MoodPost(Emotion.SADNESS, testProfile2, false, SocialSituation.ALONE, "Test Desc",
                        null, true),
                new MoodPost(Emotion.ANGER, testProfile, false, null, null, null, false),
        };

        // Add a comment to one of the mood posts
        moodPosts[0].addComment(new Comment(testProfile2, "test comment"));

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

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    );

    /*
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
    */

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
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("This is a invalid description that has over 200 characters. " +
                "This is a invalid description that has over 200 characters. This is a invalid description that has over 200 characters. " +
                "This is a invalid description that has over 200 characters."));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());
        onView(withId(R.id.postMoodDescription))
                .check(matches(hasErrorText("Description can be max 200 characters")));

        // Input proper mood post details
        onView(withId(R.id.postMoodLocation)).perform(click());
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("SHAME")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("GROUP")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(clearText());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("test desc"));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());

        // Check that our mood post list has our new mood post
        onView(withText("Shame")).check(matches(isDisplayed()));

        // Check mood post for right details
        onView(withText("Shame")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Shame")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("GROUP")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("test desc")));
    }

    @Test
    public void addMoodPostMinData() {
        // Click on button to open add mood post dialog
        onView(withId(R.id.create)).perform(click());

        // Input proper mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("SHAME")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("prefer not to say")).perform(click());
        onView(withId(R.id.postMoodConfirmButton)).perform(click());

        // Check that our mood post list has our new mood post
        onView(withText("Shame")).check(matches(isDisplayed()));

        // Check mood post for right details
        onView(withText("Shame")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Shame")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("")));
    }

    @Test
    public void addMoodPostCancel() {
        // Click on button to open add mood post dialog
        onView(withId(R.id.create)).perform(click());

        // Input proper mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("SHAME")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("GROUP")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("test desc"));
        // Cancel
        onView(withId(R.id.postMoodCancelButton)).perform(click());

        // Check that our mood post list DOESN'T have our new mood post
        try {
            onView(withText("Shame")).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Mood post added after cancel");
        } catch (NoMatchingViewException e) {
            // View is not in hierarchy
        }
    }

    @Test
    public void editMoodPostInvalidPost() {
        // Click on buttons to open edit mood post
        onView(withText("Sadness")).perform(click());

        // Check that we can't edit a mood post that isn't ours
        try {
            onView(withText("Edit")).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Can edit a mood post that isn't ours");
        } catch (AssertionFailedError e) {
            // View is not in hierarchy
        }
    }

    @Test
    public void editMoodPostRemoveDetails() {
        // Click on buttons to open edit mood post
        onView(withText("Happiness")).perform(click());
        onView(withText("Edit")).perform(click());

        // Check all details are properly shown
        onView(withId(R.id.postMoodEmotion)).check(matches(withSpinnerText("HAPPINESS")));
        onView(withId(R.id.postMoodSocialSituation)).check(matches(withSpinnerText("ALONE")));
        onView(withId(R.id.postMoodDescription)).check(matches(withText("This is a description")));
        onView(withId(R.id.postMoodLocation)).check(matches(isChecked()));
        onView(withId(R.id.postMoodPublic)).check(matches(isChecked()));

        // Test invalid description input
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("This is a invalid description that has over 200 characters. " +
                "This is a invalid description that has over 200 characters. This is a invalid description that has over 200 characters. " +
                "This is a invalid description that has over 200 characters."));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());
        onView(withId(R.id.postMoodDescription))
                .check(matches(hasErrorText("Description can be max 200 characters")));

        // Input new mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("DISGUST")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("prefer not to say")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(clearText());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText(""));
        onView(withId(R.id.postMoodLocation)).perform(click());
        onView(withId(R.id.postMoodPublic)).perform(click());
        onView(withId(R.id.postMoodConfirmButton)).perform(click());

        // Check that our mood post list has our new mood post
        onView(withText("Disgust")).check(matches(isDisplayed()));

        // Check mood post for right details
        onView(withText("Disgust")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Disgust")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("")));
    }

    @Test
    public void editMoodPostAddDetails() {
        // Click on buttons to open edit mood post
        onView(withText("Anger")).perform(click());
        onView(withText("Edit")).perform(click());

        // Check all details are properly shown
        onView(withId(R.id.postMoodEmotion)).check(matches(withSpinnerText("ANGER")));
        onView(withId(R.id.postMoodSocialSituation)).check(matches(withSpinnerText("prefer not to say")));
        onView(withId(R.id.postMoodDescription)).check(matches(withText("")));

        // Test invalid description input
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("This is a invalid description that has over 200 characters. " +
                "This is a invalid description that has over 200 characters. This is a invalid description that has over 200 characters. " +
                "This is a invalid description that has over 200 characters."));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());
        onView(withId(R.id.postMoodDescription))
                .check(matches(hasErrorText("Description can be max 200 characters")));

        // Input new mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("FEAR")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("GROUP")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(clearText());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("test desc"));
        onView(withId(R.id.postMoodLocation)).perform(click());
        onView(withId(R.id.postMoodConfirmButton)).perform(click());

        // Check that our mood post list has our new mood post
        onView(withText("Fear")).check(matches(isDisplayed()));

        // Check mood post for right details
        onView(withText("Fear")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Fear")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("GROUP")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("test desc")));
    }

    @Test
    public void editMoodPostCancel() {
        // Click on buttons to open edit mood post
        onView(withText("Anger")).perform(click());
        onView(withText("Edit")).perform(click());

        // Check all details are properly shown
        onView(withId(R.id.postMoodEmotion)).check(matches(withSpinnerText("ANGER")));
        onView(withId(R.id.postMoodSocialSituation)).check(matches(withSpinnerText("prefer not to say")));
        onView(withId(R.id.postMoodDescription)).check(matches(withText("")));

        // Test invalid description input
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("This is a invalid description that has over 200 characters. " +
                "This is a invalid description that has over 200 characters. This is a invalid description that has over 200 characters. " +
                "This is a invalid description that has over 200 characters."));
        onView(withId(R.id.postMoodConfirmButton)).perform(click());
        onView(withId(R.id.postMoodDescription))
                .check(matches(hasErrorText("Description can be max 200 characters")));

        // Input new mood post details
        onView(withId(R.id.postMoodEmotion)).perform(click());
        onView(withText("FEAR")).perform(click());
        onView(withId(R.id.postMoodSocialSituation)).perform(click());
        onView(withText("GROUP")).perform(click());
        onView(withId(R.id.postMoodDescription)).perform(clearText());
        onView(withId(R.id.postMoodDescription)).perform(ViewActions.typeText("test desc"));
        onView(withId(R.id.postMoodLocation)).perform(click());
        onView(withId(R.id.postMoodCancelButton)).perform(click());

        // Check that our mood post list DOESN'T have edited mood post
        try {
            onView(withText("Fear")).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Mood post edited after cancel");
        } catch (NoMatchingViewException e) {
            // View is not in hierarchy
        }

        onView(withText("Anger")).perform(click());
        onView(withId(R.id.detailedViewEmotion)).check(matches(withText("Anger")));
        onView(withId(R.id.detailedViewSocialSituation)).check(matches(withText("")));
        onView(withId(R.id.detailedViewDescription)).check(matches(withText("")));
    }

    @Test
    public void deleteMoodPost() {
        // Click on buttons to open and delete mood post
        onView(withText("Happiness")).perform(click());
        onView(withText("Delete")).perform(click());

        // Check that our mood post list DOESN'T have deleted mood post
        try {
            onView(withText("Happiness")).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Mood post not deleted");
        } catch (NoMatchingViewException e) {
            // View is not in hierarchy
        }
    }

    @Test
    public void deleteMoodPostInvalidPost() {
        // Click on button to open delete mood post
        onView(withText("Sadness")).perform(click());

        // Check that we can't edit a mood post that isn't ours
        try {
            onView(withText("Delete")).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Can delete a mood post that isn't ours");
        } catch (AssertionFailedError e) {
            // View is not in hierarchy
        }
    }

    @Test
    public void viewOtherUsersComment() {
        // Click on buttons to open the comments
        onView(withText("Happiness")).perform(click());
        onView(withText("Comments")).perform(click());

        // Make sure comment is there with right info
        onView(withText("testUser2")).check(matches(isDisplayed()));
        onView(withText("test comment")).check(matches(isDisplayed()));

        // Make sure that you can't delete the comment
        // Check that we can't edit a mood post that isn't ours
        try {
            onView(withId(R.id.deleteCommentButton)).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Can delete a comment that isn't ours");
        } catch (AssertionFailedError e) {
            // View is not in hierarchy
        }
    }

    @Test
    public void addComment() {
        // Click on buttons to open the comments
        onView(withText("Sadness")).perform(click());
        onView(withText("Comments")).perform(click());

        // Add a comment
        onView(withText("Add")).perform(click());
        onView(withId(R.id.addCommentText)).perform(ViewActions.typeText("this is a test comment"));
        onView(withText("Confirm")).perform(click());

        // Make sure comment is there with right info
        onView(withText("testUser1")).check(matches(isDisplayed()));
        onView(withText("this is a test comment")).check(matches(isDisplayed()));

        // Delete it and make sure it gets deleted properly
        onView(withId(R.id.deleteCommentButton)).perform(click());
        // Check that our comment isn't there
        try {
            onView(withText("this is a test comment")).check(matches(isDisplayed()));
            // View is in hierarchy
            throw new AssertionError("Comment there after deletion");
        } catch (NoMatchingViewException e) {
            // View is not in hierarchy
        }
    }

    @After
    public void cleanUp() {
        // Delay so that movies added in seedDatabase() have a chance to update on
        // firebase's side before we delete them
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Caught exception
        }

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
