package com.github.bytebandits.bithub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment that displays the entry point of the application, can login or register.
 */
public class StartupFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.startup, container, false);
        Button login = view.findViewById(R.id.loginBtn);
        Button signup = view.findViewById(R.id.registerBtn);

        login.setOnClickListener(v -> {
            ((StartupActivity) requireActivity()).loginFragment();
        });

        signup.setOnClickListener(v -> {
            ((StartupActivity) requireActivity()).signupFragment();
        });

        return view;
    }
}
