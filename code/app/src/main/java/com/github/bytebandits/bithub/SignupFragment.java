package com.github.bytebandits.bithub;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

/**
 * Fragment that displays the signup page
 */
public class SignupFragment extends Fragment {
    Button signup;
    TextView accountExists;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup, container, false);
        signup = view.findViewById(R.id.registerBtn);
        accountExists = view.findViewById(R.id.accountExists);

        signup.setOnClickListener(v -> {
            authenticate();
        });
        accountExists.setOnClickListener(v -> {
            ((StartupActivity) requireActivity()).loginFragment();
        });

        return view;
    }

    private void authenticate(){
        // authenticate all info
        // call db class and compare for: unique username, unique email
        // make sure no empty texts
        // password requirements (maybe): 1 special char, 1 capital, 1 number, at least 5>=words, no whitespace
        // make sure confirmpass == pass

        // if success
        ((StartupActivity) requireActivity()).loginFragment();

        // if fail
        // show error text, be a little specific on what was wrong-> unique username/unique email/password requirements etc.
    }

    private boolean isEmptyText(TextInputEditText text){
        return TextUtils.isEmpty(text.getText());
    }
}
