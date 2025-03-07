package com.github.bytebandits.bithub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * The Startup activity, entry point of the application. Governs anything related to starting up/authenticating your BitHub account.
 */
public class StartupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        testingLogOut(); // comment this line to see logged in state functionality

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean loggedIn = sharedPref.getBoolean("LoggedIn", false); // second param is default val if key does not exist
        if (loggedIn){
            mainActivitySwitch();
        }
        else{
            startupFragment();
        }
    }

    /**
     * Initializes the startup fragment
     */
    public void startupFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        StartupFragment startupFragment = new StartupFragment();
        fragmentTransaction.add(R.id.startupFrame, startupFragment);
        fragmentTransaction.commit();
    }

    /**
     * Initializes the login fragment, replacing whatever is within the frame layout of activity_startup
     */
    public void loginFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LoginFragment loginFragment = new LoginFragment();
        fragmentTransaction.replace(R.id.startupFrame, loginFragment);
        fragmentTransaction.commit();
    }

    /**
     * Initializes the signup fragment, replacing whatever is within the frame layout of activity_startup
     */
    public void signupFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignupFragment signupFragment = new SignupFragment();
        fragmentTransaction.replace(R.id.startupFrame, signupFragment);
        fragmentTransaction.commit();
    }

    /**
     * Initializes the password reset fragment, replacing whatever is within the frame layout of activity_startup
     */
    public void passwordResetFragment(){
        // to be implemented
    }

    /**
     * Switches activities: from StartupActivity to MainActivity
     */
    public void mainActivitySwitch(){
        Intent intent = new Intent(StartupActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Purely for testing, will make user log out, so you can see startup page again
     */
    private void testingLogOut(){
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("LoggedIn", false);
        editor.commit();
    }
}
