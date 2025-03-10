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

import com.github.bytebandits.bithub.databinding.ActivityMainBinding;

/**
 * MainActivity serves as the primary hub of the Bithub application after user authentication.
 * It is responsible for:
 * - Displaying and managing the main features of the app through different fragments
 *   (Home, Explore, Create, Notifications, Profile) using a BottomNavigationView.
 * - Initializing the UI with View Binding for improved performance and cleaner code.
 * - Handling fragment transactions to switch between different sections of the app.
 */

public class MainActivity extends AppCompatActivity {
    SessionManager sessionManager;

//    public static Profile globalProfile;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Possibly remove later
//        Profile loggedInProfile = Database.loggedInUser();
//
//        // Check if user is logged in
//        if (loggedInProfile != null) {
//            // There is some user logged in
//            globalProfile = loggedInProfile;
//            setContentView(R.layout.activity_main);
//        } else {
//            // If no user is logged in, go to the start activity
//            Intent intent = new Intent(this, StartupActivity.class);
//            startActivity(intent);
//            finish();  // Prevents the user from going back to this activity by pressing the back button
//        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomepageFragment());  // display the home fragment first when logged in

        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            // call replaceFragment when user selects one of the bottom navigaton bar's icons
            if (item.getItemId() == R.id.home) {
                replaceFragment(new Homepageragment());
            } else if (item.getItemId() == R.id.explore) {
                replaceFragment(new ExploreFragment());
            } else if (item.getItemId() == R.id.create) {
                replaceFragment(new PostMoodFragment());
            } else if (item.getItemId() == R.id.notifications) {
                replaceFragment(new NotificationsFragment());
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Replaces the current fragment with the selected fragment based on the clicked
    // icon.
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Launches the editMoodPost fragment
     */
    public void editMoodFragment(MoodPost moodPost) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PostMoodFragment postMoodFragment = PostMoodFragment.newInstance(moodPost);
        fragmentTransaction.add(R.id.frameLayout, postMoodFragment, "edit mood post");
        fragmentTransaction.commit();
    }

    // Possibly remove later
    // Access the global profile anywhere in the app
//    public static Profile getGlobalProfile() {
//        return globalProfile;
//    }
//
//    // Clear the global profile
//    public static void clearGlobalProfile() {
//        globalProfile = null;
//    }

}