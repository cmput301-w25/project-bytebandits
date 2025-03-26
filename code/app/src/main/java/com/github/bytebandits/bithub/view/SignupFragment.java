package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.model.Profile;
import com.github.bytebandits.bithub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup, container, false);
        signup = view.findViewById(R.id.registerBtn);
        accountExists = view.findViewById(R.id.accountExists);
        back = view.findViewById(R.id.backActionButton);
        userText = view.findViewById(R.id.UserInputText);
        emailText = view.findViewById(R.id.EmailInputText);
        pswrdText = view.findViewById(R.id.PswrdInputText);
        pswrdContext = view.findViewById(R.id.PswrdConInputText);

        signup.setOnClickListener(v ->
            authenticate()
        );
        accountExists.setOnClickListener(v ->
            ((StartupActivity) requireActivity()).loginFragment()
        );
        back.setOnClickListener(v ->
            ((StartupActivity) requireActivity()).popBackStack("signupFragment")
        );

        return view;
    }

    /**
     * Authenticates user input, displays an error if something is wrong, or
     * switches to login fragment if everything is correct
     */
    private void authenticate() {
        if (!(isEmptyText(userText) || isEmptyText(emailText) || isEmptyText(emailText) || isEmptyText(pswrdContext))) {
            String userId = userText.getText().toString();
            String password = pswrdText.getText().toString();
            String email = emailText.getText().toString();

            // Run DB check in background thread
            executor.execute(() -> {
                DatabaseManager.getInstance().getUser(userId, user -> {
                    boolean userExists = (user != null);

                    // Switch to UI thread to handle results
                    mainHandler.post(() -> {
                        Log.d("SignupFragment", "User exists: " + userExists);
                        handleAuthenticationResult(userExists, userId, email, password);
                    });
                });
            });
        } else {
            AlertDialog dialog = createDialog("No null/empty strings allowed!");
            dialog.show();
        }

    }

    /**
     * Handles the authentication result after checking the database.
     */
    private void handleAuthenticationResult(boolean userExists, String userId, String email, String password) {
        boolean userIdReqsValid = !userId.contains("@");
        boolean pswrdMatch = password.equals(pswrdContext.getText().toString());

        if (!userExists && userIdReqsValid && pswrdMatch) {
            Log.d("SignupFragment", "Credentials are valid. Creating user...");

            HashMap<String, Object> userDetails = new HashMap<>();
            userDetails.put("userId", userId);
            userDetails.put("email", email);
            userDetails.put("password", password);
            userDetails.put("profile", new Profile(userId).toJson());

            DatabaseManager.getInstance().addUser(userId, userDetails, Optional.empty());

            ((StartupActivity) requireActivity()).loginFragment();
        } else if (!userIdReqsValid) {
            createDialog("Username cannot have '@' within it").show();
        } else {
            createDialog("Invalid information! Username or email may already exist.").show();
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
                userText.setText("");
                emailText.setText("");
                pswrdText.setText("");
                pswrdContext.setText("");
            }
        });
        return builder.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // Clean up executor when fragment is destroyed
    }
}
