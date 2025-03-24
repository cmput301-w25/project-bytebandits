package com.github.bytebandits.bithub;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

// The following imports where just copied from Lab 7 (UI Testing with Espresso)
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.view.LoginFragment;
import com.github.bytebandits.bithub.view.SignupFragment;
import com.github.bytebandits.bithub.view.StartupActivity;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class StartupActivityTest {
    @BeforeClass
    public static void setup(){
        DatabaseManager.getInstance(true);
        SessionManager sessionManager = SessionManager.getInstance(ApplicationProvider.getApplicationContext());
        sessionManager.logoutUser();
    }

    @Rule
    public ActivityScenarioRule<StartupActivity> scenario = new ActivityScenarioRule<>(StartupActivity.class);

    /**
     * Ensures clearing of logged in state from shared preferences, (logged in = true -> logged in = false) so the tests starts from startup page always.
     */
    @Before
    public void clearLoggedInState(){
        SessionManager sessionManager = SessionManager.getInstance(ApplicationProvider.getApplicationContext());
    }

    /**
     * Tests StartupFragment is displaying what it is supposed to
     */
    @Test
    public void startupFragmentTestDisplay(){
        onView(withId(R.id.appTitle)).check(matches(isDisplayed()));

        // copied first 2 lines and its idea from a Stackoverflow post
        // Link: https://stackoverflow.com/questions/58493104/how-can-i-access-a-string-resource-from-a-test
        // Retrieved by: Hanss Rivera, Post Author: Giorgio Antonioli, Date Retrieved: March 8 2025
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String expectedAppTitle = context.getResources().getString(R.string.app_name);
        onView(withId(R.id.appTitle)).check(matches(withText(expectedAppTitle)));

        String expectedFlavourText = context.getResources().getString(R.string.startup_title_flavour);
        onView(withId(R.id.flavourText)).check(matches(withText(expectedFlavourText)));

        String expectedLoginBtnText = context.getResources().getString(R.string.startup_login);
        onView(withId(R.id.loginBtn)).check(matches(withText(expectedLoginBtnText)));

        String expectedSignupBtnText = context.getResources().getString(R.string.startup_signup);
        onView(withId(R.id.registerBtn)).check(matches(withText(expectedSignupBtnText)));
    }

    /**
     * Tests the buttons of StartupFragment and checks to see if they are working and transitions
     */
    @Test
    public void startupFragmentTestButtons(){
        onView(withId(R.id.loginBtn)).perform(click());

        // Following code syntax, which allows us to retrieve the current fragment was suggested by Chatgpt LLM
        // Retrieved by: Hanss Rivera, On: March 8 2025
        scenario.getScenario().onActivity(activity -> {
            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.startupFrame);
            assertTrue(currentFragment instanceof LoginFragment);
        });

        onView(withId(R.id.backActionButton)).perform(click());

        onView(withId(R.id.registerBtn)).perform(click());
        scenario.getScenario().onActivity(activity -> {
            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.startupFrame);
            assertTrue(currentFragment instanceof SignupFragment);
        });
    }

    /**
     * Tests if signup page is displaying its components correctly
     */
    @Test
    public void signupFragmentTestDisplay(){
        onView(withId(R.id.registerBtn)).perform(click());

        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String expectedSignupTitle = context.getResources().getString(R.string.signup_title);
        onView(withId(R.id.signupText)).check(matches(withText(expectedSignupTitle)));

        String expectedSignupUserText = context.getResources().getString(R.string.signup_user);
        onView(withId(R.id.UserInputText)).check(matches(withHint(expectedSignupUserText)));

        String expectedSignupEmailText = context.getResources().getString(R.string.signup_email);
        onView(withId(R.id.EmailInputText)).check(matches(withHint(expectedSignupEmailText)));

        String expectedSignupPswrdText = context.getResources().getString(R.string.signup_password);
        onView(withId(R.id.PswrdInputText)).check(matches(withHint(expectedSignupPswrdText)));

        String expectedSignupPswrdConText = context.getResources().getString(R.string.signup_confirm_password);
        onView(withId(R.id.PswrdConInputText)).check(matches(withHint(expectedSignupPswrdConText)));

        String expectedSignupBtnText = context.getResources().getString(R.string.signup_signup);
        onView(withId(R.id.registerBtn)).check(matches(withText(expectedSignupBtnText)));

        String expectedAccExistText = context.getResources().getString(R.string.signup_existing);
        onView(withId(R.id.accountExists)).check(matches(withText(expectedAccExistText)));
    }

    /**
     * Tests if the account exist text on SignupFragment appropriately transitions to login fragment
     */
    @Test
    public void signupFragmentTestAccountExistButton(){
        onView(withId(R.id.registerBtn)).perform(click());
        onView(withId(R.id.accountExists)).perform(click());

        scenario.getScenario().onActivity(activity -> {
            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.startupFrame);
            assertTrue(currentFragment instanceof LoginFragment);
        });
    }

    /**
     * Tests if signup page input validation works, assuming user inputs correct info
     */
    @Test
    public void signupFragmentTestValidInput(){

        onView(withId(R.id.registerBtn)).perform(click());
        // remove this comment and the second one when db connection is implemented, for now assume query always succeeds
        // meaning that the username and email lookup is assumed to be unique
        onView(withId(R.id.UserInputText)).perform(typeText("usernameTest"));
        onView(withId(R.id.EmailInputText)).perform(typeText("email@test.com"));
        onView(withId(R.id.PswrdInputText)).perform(typeText("abc123"));
        onView(withId(R.id.PswrdConInputText)).perform(typeText("abc123"));
        onView(withId(R.id.registerBtn)).perform(click());
        scenario.getScenario().onActivity(activity -> {
            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.startupFrame);
            assertTrue(currentFragment instanceof LoginFragment);
        });
    }

    /**
     * Tests if signup page password matching error text displays
     */
    @Test
    public void signupFragmentTestPasswordMatchError(){

        onView(withId(R.id.registerBtn)).perform(click());
        // remove this comment and the second one when db connection is implemented, for now assume query always succeeds
        // meaning that the username and email lookup is assumed to be unique
        onView(withId(R.id.UserInputText)).perform(typeText("usernameTest"));
        onView(withId(R.id.EmailInputText)).perform(typeText("email@test.com"));
        onView(withId(R.id.PswrdInputText)).perform(typeText("abc123"));
        onView(withId(R.id.PswrdConInputText)).perform(typeText("123abc"));
        onView(withId(R.id.registerBtn)).perform(click());
        onView(withText("Invalid information! Or the provided username or email already has an account attached to it"))
                .inRoot(isDialog()).check(matches(isDisplayed()));
    }

    /**
     * Tests if signup page username requirements error text displays
     */
    @Test
    public void signupFragmentTestUsernameReqError(){

        onView(withId(R.id.registerBtn)).perform(click());
        // remove this comment and the second one when db connection is implemented, for now assume query always succeeds
        // meaning that the username and email lookup is assumed to be unique
        onView(withId(R.id.UserInputText)).perform(typeText("@usernameTest"));
        onView(withId(R.id.EmailInputText)).perform(typeText("email@test.com"));
        onView(withId(R.id.PswrdInputText)).perform(typeText("abc123"));
        onView(withId(R.id.PswrdConInputText)).perform(typeText("abc123"));
        onView(withId(R.id.registerBtn)).perform(click());
        onView(withText("Username cannot have '@' within it"))
                .inRoot(isDialog()).check(matches(isDisplayed()));
    }

    /**
     * Tests if signup page null/empty error text displays
     */
    @Test
    public void signupFragmentTestNullEmptyInput(){
        onView(withId(R.id.registerBtn)).perform(click());
        onView(withId(R.id.registerBtn)).perform(click());
        onView(withText("No null/empty strings allowed!"))
                .inRoot(isDialog()).check(matches(isDisplayed()));
    }

    /**
     * Tests if login page is displaying its components correctly
     */
    @Test
    public void loginFragmentTestDisplay(){
        onView(withId(R.id.loginBtn)).perform(click());

        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String expectedLoginTitle = context.getResources().getString(R.string.login_title);
        onView(withId(R.id.loginText)).check(matches(withText(expectedLoginTitle)));

        String expectedLoginUserEmailText = context.getResources().getString(R.string.login_user_email);
        onView(withId(R.id.UserEmailInputText)).check(matches(withHint(expectedLoginUserEmailText)));

        String expectedLoginPasswordText = context.getResources().getString(R.string.login_password);
        onView(withId(R.id.PswrdInputText)).check(matches(withHint(expectedLoginPasswordText)));
    }

    /**
     * Tests if correct login page input validation by switching to main activity from login page
     */
    @Test
    public void loginFragmentTestInputValidation(){
        // Parts a & b where retrieved from Claude LLM, had to ask an LLM due to not a lot of results coming up with the right syntax to reference an activity
        // and observe how it is transitioning. Retrived by: Hanss Rivera, On: March 8 2025.

        // Part a
        Instrumentation.ActivityMonitor monitor = InstrumentationRegistry.getInstrumentation()
                .addMonitor(MainActivity.class.getName(), null, false);


        onView(withId(R.id.loginBtn)).perform(click());
        // remove this comment and the second one when db connection is implemented, for now assume query always succeeds
        // meaning that the username OR email lookup is assumed to exist AND password lookup matches the provided password
        onView(withId(R.id.UserEmailInputText)).perform(typeText("usernameTest"));
        onView(withId(R.id.PswrdInputText)).perform(typeText("abc123"));
        onView(withId(R.id.loginBtn)).perform(click());


        // Part b
        Activity mainActivity = monitor.waitForActivityWithTimeout(5000);
        assertNotNull("MainActivity should have been launched", mainActivity);
        assertTrue(mainActivity instanceof MainActivity);
        SessionManager.getInstance(ApplicationProvider.getApplicationContext()).logoutUser();
    }
}
