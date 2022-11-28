package com.zeeshanvirani.projectcelia;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class ChangeEmailDialogFragment extends DialogFragment {

    private static final String TAG = "ProjectCelia:ChangeEmailDialogFragment";
    View view;

    // Necessary constructor
    public ChangeEmailDialogFragment() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get layout for dialog
        view = getLayoutInflater().inflate( R.layout.dialog_change_email, null );

        // Setup dialog builder
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.Theme_ProjectCelia_MaterialAlertDialog);
        builder.setMessage("Enter your new email.")
                .setPositiveButton("Submit", (dialog, id) -> {} )
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss() )
                .setView( view );

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog dialog = (AlertDialog) getDialog();
        EditText email = view.findViewById(R.id.che_email);
        EditText password = view.findViewById(R.id.che_pw);
        email.setText(PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .getString("account_email", ""));

        // Check if dialog is null
        if ( dialog != null ) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                // Submit button clicked
                // Check if email field are empty
                if ( email.getText().toString().equals("") ) {
                    Log.d(TAG, "Email field cannot be empty.");
                    email.setError( "Email cannot be empty" );
                    return;
                }
                if ( password.getText().toString().equals("") ) {
                    Log.d(TAG, "Password field cannot be empty.");
                    password.setError( "Password cannot be empty" );
                    return;
                }
                // Check if email is valid
                if (isValidEmail(email.getText().toString())) {
                    assert FirebaseAuth.getInstance().getCurrentUser() != null;
                    AuthCredential credential = EmailAuthProvider.getCredential(
                            PreferenceManager.getDefaultSharedPreferences(requireActivity())
                            .getString("account_email", ""),
                            password.getText().toString()
                    );
                    // Use provided password to allow for an email change on the account
                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().getCurrentUser().updateEmail( email.getText().toString() )
                                    .addOnCompleteListener(updatetask -> {
                                        if (updatetask.isSuccessful()) {
                                            Toast.makeText(dialog.getContext(), "Email has been updated.",
                                                    Toast.LENGTH_LONG).show();
                                            Log.d(TAG, "User email has been updated.");
                                            PreferenceManager.getDefaultSharedPreferences(dialog.getContext())
                                                    .edit().putString("account_email", email.getText().toString()).apply();
                                        } else {
                                            Toast.makeText(dialog.getContext(), "Error occurred. Email not updated.",
                                                    Toast.LENGTH_LONG).show();
                                            Log.d(TAG, "Error occurred. Email not updated." + updatetask.getException());
                                        }
                                    });
                            dialog.dismiss();
                        } else {
                            Log.d(TAG, "Password is incorrect.");
                            password.setError( "Password is incorrect" );
                        }
                    });
                } else {
                    Log.d(TAG, "Email not valid");
                    email.setError( "Email not valid" );
                }
            });
        }
    }

    // Regex checker to see if the email inputted is valid
    private boolean isValidEmail( String email ) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pat = Pattern.compile( emailRegex );
        if ( email == null ) return false;
        return pat.matcher( email ).matches();
    }
}