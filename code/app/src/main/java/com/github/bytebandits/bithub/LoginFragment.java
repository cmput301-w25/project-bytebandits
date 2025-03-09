package com.github.bytebandits.bithub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

            // determine if text input is email or username by checking to see if it has an '@'
            // call db class
            // make query -> does provided email OR username exist? -> if so, does password input match in db?
            // return true if all passes otherwise false

            boolean querySuccess = true; // placeholder and for testing, set to false if you want to see error text, true for main activity switch

            if (querySuccess){
                SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("LoggedIn", true);
                editor.commit();
                ((StartupActivity) requireActivity()).mainActivitySwitch();
            }

            else{
                AlertDialog dialog = createDialog("Invalid information!");
                dialog.show();
            }
        }

        else{
            AlertDialog dialog = createDialog("No null/empty strings allowed!");
            dialog.show();
        }
    }

    /**
     * Helper function to check if text is not null nor empty
     * @param text
     * @return true if it is null/empty, false if not
     */
    private boolean isEmptyText(TextInputEditText text){
        return TextUtils.isEmpty(text.getText());
    }

    /**
     * Error text dialog logic
     * @param msg
     * @return the dialog
     */
    AlertDialog createDialog(String msg){
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
