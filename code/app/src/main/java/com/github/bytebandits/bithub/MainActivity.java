package com.github.bytebandits.bithub;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.databinding.ActivityMainBinding;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.model.Profile;
import com.github.bytebandits.bithub.view.ExploreFragment;
import com.github.bytebandits.bithub.view.HomepageFragment;
import com.github.bytebandits.bithub.view.NotificationsFragment;
import com.github.bytebandits.bithub.view.PostMoodFragment;
import com.github.bytebandits.bithub.view.ProfileFragment;

/**
 * MainActivity serves as the primary hub of the Bithub application after user
 * authentication.
 * It is responsible for:
 * - Displaying and managing the main features of the app through different
 * fragments
 * (Home, Explore, Create, Notifications, Profile) using a BottomNavigationView.
 * - Initializing the UI with View Binding for improved performance and cleaner
 * code.
 * - Handling fragment transactions to switch between different sections of the
 * app.
 */

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    /**
     * Called when the activity is first created.
     * Initializes the UI, sets up the BottomNavigationView, and manages fragment
     * transactions.
     *
     * @param savedInstanceState Bundle contains the data it most recently supplied.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Display the home fragment first when the user is logged in
        replaceFragment(new HomepageFragment());

        // Set up BottomNavigationView item selection listener
        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            // Replace fragment based on selected navigation item
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomepageFragment());
            } else if (item.getItemId() == R.id.explore) {
                replaceFragment(new ExploreFragment());
            } else if (item.getItemId() == R.id.create) {
                replaceFragment(new PostMoodFragment());
            } else if (item.getItemId() == R.id.notifications) {
                replaceFragment(new NotificationsFragment());
            } else if (item.getItemId() == R.id.profile) {
                Profile userProfile = SessionManager.getInstance(this).getProfile(); // get currently logged in profile
                replaceFragment(ProfileFragment.newInstance(userProfile));
            }
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Replaces the current fragment with the specified fragment.
     *
     * @param fragment The fragment to be displayed.
     */
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
//        binding.bottomNavigation.setSelectedItemId(R.id.profile);
        fragmentTransaction.commit();
    }

    /**
     * Launches the editMoodPost fragment
     */
    public void editMoodFragment(MoodPost moodPost) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PostMoodFragment postMoodFragment = PostMoodFragment.newInstance(moodPost);
        fragmentTransaction.replace(R.id.frameLayout, postMoodFragment, "edit mood post");
        fragmentTransaction.commit();
    }

}
