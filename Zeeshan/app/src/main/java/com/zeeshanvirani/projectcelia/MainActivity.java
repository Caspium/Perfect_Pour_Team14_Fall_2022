package com.zeeshanvirani.projectcelia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Check if dark mode is enabled
        if ( PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean( "notifications_darklight_mode", false) ) {
            // Dark mode enabled
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            // Dark mode disabled
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Bottom navigation setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if ( item.getItemId() == R.id.history ) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace( R.id.fragment_container, new HistoryFragment() ).commit();
            } else if ( item.getItemId() == R.id.brew ) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace( R.id.fragment_container, new BrewFragment() ).commit();
            } else if ( item.getItemId() == R.id.settings ) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace( R.id.fragment_container, new SettingsFragment() ).commit();
            }
            return true;
        });

        // If activity is restarted, go to settings
        if ( savedInstanceState != null ) {
            bottomNavigationView.setSelectedItemId( R.id.settings );
        } else {
            bottomNavigationView.setSelectedItemId( R.id.brew );
        }

    }

}
