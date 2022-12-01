package com.zeeshanvirani.projectcelia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";

    private TextInputEditText email_textbox;
    private TextInputEditText password_textbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_textbox = findViewById(R.id.email_textbox);
        password_textbox = findViewById(R.id.password_textbox);

        ImageButton back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(view -> {
            // Return to LaunchActivity
            onBackPressed();
        });

        Button forgotpassword_btn = findViewById(R.id.forgotpassword_button);
        forgotpassword_btn.setOnClickListener(view -> {
            // Email field is null
            if ( email_textbox.getText() == null ) {
                Toast.makeText(getApplicationContext(), "Email field cannot be null.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Email text box is empty
            if ( email_textbox.getText().toString().isEmpty()) {
                // Display error message and have user retry
                Toast.makeText(getApplicationContext(), "Please enter your email address and then click forgot password again.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Invalid email format
            if ( !isValidEmail( email_textbox.getText().toString() ) ) {
                Toast.makeText(getApplicationContext(), "Please enter a valid email address.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Send Password Reset Email
            FirebaseAuth.getInstance().sendPasswordResetEmail(email_textbox.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "A password reset link has been sent to your email. Please click the link to reset your password then come back here to retry login.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "An error has occurred. The email you entered is either invalid or there are network connection issues.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        Button login_btn = findViewById(R.id.login_button);
        login_btn.setOnClickListener(view -> {
            // Verify if fields are properly filled in
            if ( email_textbox.getText() == null
                    || password_textbox.getText() == null ) { // Text boxes are empty

                // Display error message and have user retry
                Toast.makeText(getApplicationContext(), "Email and Password fields cannot be null.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if ( email_textbox.getText().toString().isEmpty()
                    || password_textbox.getText().toString().isEmpty() ) { // Text boxes are empty

                // Display error message and have user retry
                Toast.makeText(getApplicationContext(), "Email and Password fields cannot be empty.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if ( !isValidEmail( email_textbox.getText().toString() ) ) { // Invalid email format
                Toast.makeText(getApplicationContext(), "Please enter a valid email address.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email_textbox.getText().toString(),
                    password_textbox.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) { // Sign in success
                        // Setup shared preferences
                        DataHandler.updateSharedPreferences( getApplicationContext() );
                        // Switch to MainActivity
                        startActivity( new Intent(getApplicationContext(), MainActivity.class) );
                        finishAffinity();
                    } else { // Sign in failed
                        Log.d(TAG, task.getException().toString());
                        Toast.makeText(getApplicationContext(), "Invalid email or password. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                });

        });

    }

    // Determines if a provided string (email) is a valid email
    // Input: String
    // Returns: Boolean
    public static boolean isValidEmail( String email ) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

}