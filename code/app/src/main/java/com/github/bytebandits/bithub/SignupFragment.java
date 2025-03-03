package com.github.bytebandits.bithub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment that displays the signup page
 */
public class SignupFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup, container, false);
        Button signup = view.findViewById(R.id.registerBtn);
        TextView accountExists = view.findViewById(R.id.accountExists);
        signup.setOnClickListener(v -> {
            ((StartupActivity) requireActivity()).loginFragment();
        });
        accountExists.setOnClickListener(v -> {
            ((StartupActivity) requireActivity()).loginFragment();
        });

        return view;
    }
}
