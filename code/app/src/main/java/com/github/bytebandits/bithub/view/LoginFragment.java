package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.model.Profile;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment that displays the login page
 *
 * @author Hanss Rivera
 */
public class LoginFragment extends Fragment {

    TextInputEditText usernameText;
    TextInputEditText passwordText;
    Button login;
    FloatingActionButton back;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        login = view.findViewById(R.id.loginBtn);
        usernameText = view.findViewById(R.id.UserLoginInputText);
        passwordText = view.findViewById(R.id.PswrdInputText);
        back = view.findViewById(R.id.backActionButton);

        login.setOnClickListener(v -> {
            authenticate();
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
        if (!(isEmptyText(usernameText) || isEmptyText(passwordText))) {
            String userId = usernameText.getText().toString();
            String password = passwordText.getText().toString();

            Log.d("LoginFragment", "Attempting login for userId: " + userId);

            AtomicBoolean isValidAccount = new AtomicBoolean(false);
            DatabaseManager.getInstance().getUser(userId, user -> {
                if (user != null) {
                    String storedPassword = (String) user.get("password");

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
                    SessionManager sessionManagerIns = SessionManager.getInstance(requireContext());
                    sessionManagerIns.createLoginSession(userId);
                    String profileJson = (String) user.get("profile");
                    Profile profile = new Profile(userId).fromJson(profileJson);

                    sessionManagerIns.saveProfile(profile);
                    ((StartupActivity) requireActivity()).mainActivitySwitch();
                } else {
                    Log.d("LoginFragment", "Login failed, showing error dialog");
                    AlertDialog dialog = createDialog(getString(R.string.startup_invalid));
                    dialog.show();
                }
            });
        } else {
            Log.d("LoginFragment", "Empty username or password");
            AlertDialog dialog = createDialog(getString(R.string.startup_null));
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
                usernameText.setText("");
                passwordText.setText("");
            }
        });
        return builder.create();
    }
}
