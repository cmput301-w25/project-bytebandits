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

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.explore) {
                replaceFragment(new ExploreFragment());
            } else if (item.getItemId() == R.id.create) {
                replaceFragment(new CreateFragment());
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

}