package com.github.bytebandits.bithub;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Fragment that displays the login page
 */
public class LoginFragment extends Fragment {

    TextInputEditText userEmailText;
    TextInputEditText passwordText;
    Button login;
    TextView pswrdReset;
    FloatingActionButton back;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        login = view.findViewById(R.id.loginBtn);
        userEmailText = view.findViewById(R.id.UserEmailInputText);
        passwordText = view.findViewById(R.id.PswrdInputText);
        pswrdReset = view.findViewById(R.id.pswrdReset);
        back = view.findViewById(R.id.backActionButton);

        login.setOnClickListener(v -> {
            authenticate();
        });

        pswrdReset.setOnClickListener(v -> {
            int placeholder = 0; // remove when done implementing
            // StartupActivity switches to PasswordRestFragment
        });

        back.setOnClickListener(v -> {
            ((StartupActivity) requireActivity()).popBackStack("loginFragment");
        });

        return view;
    }

    /**
     * Checks to see if provided information is valid
     */
    private void authenticate(){
        if (!(isEmptyText(userEmailText) || isEmptyText(passwordText))){

            // call db class
            // make query and compare username or email, and password to the db info
            // return boolean

            // if success
            SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("LoggedIn", true);
            editor.commit();
            ((StartupActivity) requireActivity()).mainActivitySwitch();

            // if fail
            // show error text about invalid info and clear both input boxes
        }

        else{
            int placeholder = 0; // remove when done implementing
            // show error text about not allowing empty inputs
        }
    }

    private boolean isEmptyText(TextInputEditText text){
        return TextUtils.isEmpty(text.getText());
    }

}
