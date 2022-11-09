package com.zeeshanvirani.projectcelia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // Create notification channel
        DataHandler.createNotificationChannel( this );

        Button createaccount_btn = findViewById(R.id.button_createaccount);
        createaccount_btn.setOnClickListener(view -> {
            // Switch to Create Account Activity so user can create their new account
            startActivity( new Intent(this, CreateAccountActivity.class) );
        });

        // Check if user is already logged in
        if ( FirebaseAuth.getInstance().getCurrentUser() != null ) {
            // User is logged in
            DataHandler.updateSharedPreferences( getApplicationContext() );
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        Button login_btn = findViewById(R.id.button_login);
        login_btn.setOnClickListener(view -> {
            // Switch to Login Activity so that user can login with their account info
            startActivity( new Intent(this, LoginActivity.class) );
        });

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // Check for connected network
        if ( networkInfo == null || !networkInfo.isConnectedOrConnecting() ) {
            Toast.makeText(getApplicationContext(), "No Internet Connection available. Connect to the internet and restart the application.",
                    Toast.LENGTH_SHORT).show();
            createaccount_btn.setEnabled( false );
            login_btn.setEnabled( false );
        } else {
            createaccount_btn.setEnabled( true );
            login_btn.setEnabled( true );
        }

        // Check if bluetooth is available
        if ( BluetoothAdapter.getDefaultAdapter() == null ) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available on this device. Application cannot function without bluetooth capabilities.",
                    Toast.LENGTH_SHORT).show();
            createaccount_btn.setEnabled( false );
            login_btn.setEnabled( false );
        } else {
            createaccount_btn.setEnabled( true );
            login_btn.setEnabled( true );
        }

        // Request Location Permission
        int CONSTANT = 0;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CONSTANT);

    }
}