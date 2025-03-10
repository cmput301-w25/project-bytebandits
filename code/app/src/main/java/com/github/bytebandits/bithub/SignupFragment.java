package com.github.bytebandits.bithub;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment that displays the signup page
 */
public class SignupFragment extends Fragment {
    Button signup;
    TextView accountExists;
    FloatingActionButton back;
    TextInputEditText userText;
    TextInputEditText emailText;
    TextInputEditText pswrdText;
    TextInputEditText pswrdContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup, container, false);
        signup = view.findViewById(R.id.registerBtn);
        accountExists = view.findViewById(R.id.accountExists);
        back = view.findViewById(R.id.backActionButton2);
        userText = view.findViewById(R.id.UserInputText);
        emailText = view.findViewById(R.id.EmailInputText);
        pswrdText = view.findViewById(R.id.PswrdInputText);
        pswrdContext = view.findViewById(R.id.PswrdConInputText);

        signup.setOnClickListener(v -> {
            authenticate();
        });
        accountExists.setOnClickListener(v -> {
            ((StartupActivity) requireActivity()).loginFragment();
        });
        back.setOnClickListener(v -> {
            ((StartupActivity) requireActivity()).popBackStack("signupFragment");
        });

        return view;
    }

    /**
     * Authenticates user input, displays an error if something is wrong, or
     * switches to login fragment if everything is correct
     */
    private void authenticate() {
        if (!(isEmptyText(userText) || isEmptyText(emailText) || isEmptyText(emailText) || isEmptyText(pswrdContext))) {
            AtomicBoolean userExists = new AtomicBoolean(false);
            String username = userText.getText().toString();
            String password = pswrdText.getText().toString();
            String email = emailText.getText().toString();
            DatabaseManager.getUser(username, user -> {
                // TODO: Need extra logic for emails
                if (user != null) {
                    userExists.set(true);
                }
            });

            boolean usernameReqsValid = !username.contains("@");
            boolean pswrdMatch = password.equals(pswrdContext.getText().toString());

            boolean areCredentialsValid = !userExists.get() && usernameReqsValid && pswrdMatch;
            if (areCredentialsValid) {
                HashMap<String, Object> userDetails = new HashMap<>();
                userDetails.put("username", username);
                userDetails.put("email", email);
                userDetails.put("password", password);

                DatabaseManager.addUser(username, userDetails, Optional.empty());

                ((StartupActivity) requireActivity()).loginFragment();
            }

            else if (!usernameReqsValid) {
                AlertDialog dialog = createDialog("Username cannot have '@' within it");
                dialog.show();
            }

            else {
                AlertDialog dialog = createDialog(
                        "Invalid information! Or the provided username or email already has an account attached to it");
                dialog.show();
            }
        }

        else {
            AlertDialog dialog = createDialog("No null/empty strings allowed!");
            dialog.show();
        }

    }

    /**
     * Helper function to check if text is not null nor empty
     * 
     * @param text
     * @return true if it is null/empty, false if not
     */
    private boolean isEmptyText(TextInputEditText text) {
        return TextUtils.isEmpty(text.getText());
    }

    /**
     * Error text dialog logic
     * 
     * @param msg
     * @return the dialog
     */
    AlertDialog createDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(msg);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userText.setText("");
                emailText.setText("");
                pswrdText.setText("");
                pswrdContext.setText("");
            }
        });
        return builder.create();
    }
}
