package com.github.bytebandits.bithub.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.R;

/**
 * Fragment that displays the entry point of the application, can login or
 * register.
 *
 * @author Hanss Rivera
 */
public class StartupFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.startup, container, false);
        Button login = view.findViewById(R.id.loginBtn);
        Button signup = view.findViewById(R.id.registerBtn);

        login.setOnClickListener(v ->
            ((StartupActivity) requireActivity()).loginFragment()
        );

        signup.setOnClickListener(v ->
            ((StartupActivity) requireActivity()).signupFragment()
        );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // needed to override the back functionality when a user tries swipe back on
        // this fragment. If we did not finish the activity
        // the user will be stuck in frameLayout, which is a blank screen with no user
        // input, which "softlocks" the user.
        // Inspiration for code: https://www.youtube.com/watch?v=4rx3nLBwB0M
        // Retrieved by: Hanss Rivera, On: March 8 2025, Type: Youtube Video, Youtube
        // Channel Author: Coding Beast
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        });
    }
}
