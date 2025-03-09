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
    TextInputEditText pswrdConText;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup, container, false);
        signup = view.findViewById(R.id.registerBtn);
        accountExists = view.findViewById(R.id.accountExists);
        back = view.findViewById(R.id.backActionButton2);
        userText = view.findViewById(R.id.UserInputText);
        emailText = view.findViewById(R.id.EmailInputText);
        pswrdText = view.findViewById(R.id.PswrdInputText);
        pswrdConText = view.findViewById(R.id.PswrdConInputText);

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

    private void authenticate(){

        if (!(isEmptyText(userText) || isEmptyText(emailText) || isEmptyText(emailText) || isEmptyText(pswrdConText))){

            // call db class
            // make query -> does provided email exist? does username exist?
            // return true if both does not exist

            boolean querySuccess = true; // placeholder and for testing, set to false if you want to see error text, true for login switch
            boolean pswrdMatch = pswrdText.getText().toString().equals(pswrdConText.getText().toString());
            boolean pswrdReqsValid = !pswrdText.getText().toString().contains("@");

            if (querySuccess && pswrdMatch && pswrdReqsValid){
                ((StartupActivity) requireActivity()).loginFragment();
            }

            else if (!pswrdReqsValid){
                AlertDialog dialog = createDialog("Password cannot have '@' within it");
                dialog.show();
            }

            else{
                AlertDialog dialog = createDialog("Invalid information! Or the provided username or email already has an account attached to it");
                dialog.show();
            }
        }

        else{
            AlertDialog dialog = createDialog("No null/empty strings allowed!");
            dialog.show();
        }

    }

    private boolean isEmptyText(TextInputEditText text){
        return TextUtils.isEmpty(text.getText());
    }

    AlertDialog createDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(msg);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userText.setText("");
                emailText.setText("");
                pswrdText.setText("");
                pswrdConText.setText("");
            }
        });
        return builder.create();
    }
}
