package com.github.bytebandits.bithub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.concurrent.atomic.AtomicBoolean;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
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
    private void authenticate() {
        if (!(isEmptyText(userEmailText) || isEmptyText(passwordText))) {
            String username = userEmailText.getText().toString();
            String password = passwordText.getText().toString();

            Log.d("LoginFragment", "Attempting login for username: " + username);

            AtomicBoolean isValidAccount = new AtomicBoolean(false);
            DatabaseManager.getUser(username, user -> {
                if (user != null) {
                    String storedPassword = (String) user.get("password");
                    Log.d("LoginFragment", "Fetched user data: " + user.toString());
                    Log.d("LoginFragment", "Stored password: " + storedPassword);
                    Log.d("LoginFragment", "Entered password: " + password);

                    if (password.matches(storedPassword)) {
                        isValidAccount.set(true);
                        Log.d("LoginFragment", "Password match successful");
                    } else {
                        Log.d("LoginFragment", "Password mismatch");
                    }
                } else {
                    Log.d("LoginFragment", "User not found in database");
                }

                if (isValidAccount.get()) {
                    Log.d("LoginFragment", "Login successful, creating session");
                    SessionManager.getInstance(requireContext()).createLoginSession(username);
                    ((StartupActivity) requireActivity()).mainActivitySwitch();
                } else {
                    Log.d("LoginFragment", "Login failed, showing error dialog");
                    AlertDialog dialog = createDialog("Invalid information!");
                    dialog.show();
                }
            });
        } else {
            Log.d("LoginFragment", "Empty username or password");
            AlertDialog dialog = createDialog("No null/empty strings allowed!");
            dialog.show();
        }
    }

    /**
     * Helper function to check if text is not null nor empty
     * 
     * @param text the text to be checked if its is null or empty
     * @return true if it is null/empty, false if not
     */
    private boolean isEmptyText(TextInputEditText text) {
        return TextUtils.isEmpty(text.getText());
    }

    /**
     * Error text dialog logic
     * 
     * @param msg the message string to be displayed in the alert dialog
     * @return the dialog
     */
    AlertDialog createDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(msg);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userEmailText.setText("");
                passwordText.setText("");
            }
        });
        return builder.create();
    }

}
