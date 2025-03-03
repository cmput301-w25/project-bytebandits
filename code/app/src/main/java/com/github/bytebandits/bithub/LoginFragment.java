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
 * Fragment that displays the login page
 */
public class LoginFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        Button login = view.findViewById(R.id.loginBtn);
        login.setOnClickListener(v -> {
            ((StartupActivity) requireActivity()).mainActivitySwitch();
        });

        return view;
    }
}
