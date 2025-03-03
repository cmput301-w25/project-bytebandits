package com.github.bytebandits.bithub;

import android.content.Intent;
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
        startupFragment();
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
     * Switches activities: from StartupActivity to MainActivity
     */
    public void mainActivitySwitch(){
        Intent intent = new Intent(StartupActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
