package com.zeeshanvirani.projectcelia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class PreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "ProjectCelia:PreferenceFragment";

    @Override
    public void onResume() {
        super.onResume();
        if ( getPreferenceScreen().getSharedPreferences() == null ) return;
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if ( getPreferenceScreen().getSharedPreferences() == null ) return;
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        if ( getContext() == null ) {
            Log.d(TAG, "No Context");
            return;
        }
        if ( getActivity() == null ) {
            Log.d(TAG, "No Activity");
            return;
        }

        EditTextPreference namePreference = findPreference("account_name");
        if (namePreference != null) {
            namePreference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>)
                    EditTextPreference::getText
            );
        }

        EditTextPreference emailPreference = findPreference("account_email");
        if (emailPreference != null) {
            emailPreference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>)
                    EditTextPreference::getText
            );
            emailPreference.setOnBindEditTextListener(editText ->
                    editText.setInputType( InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
            );
        }

        Preference changepassword_preference = findPreference("account_password");
        if (changepassword_preference != null) {
            changepassword_preference.setOnPreferenceClickListener(preference -> {
                // Open dialog fragment
                DialogFragment dialog = new ChangePasswordDialogFragment();
                dialog.show(getActivity().getSupportFragmentManager(),"changepw");
                return true;
            });
        }


        // Device information brings up alertdialog with device information
        Preference deviceinformation_preference = findPreference("device_information");
        if (deviceinformation_preference != null) {
            deviceinformation_preference.setOnPreferenceClickListener(preference -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.Theme_ProjectCelia_MaterialAlertDialog);
                builder.setMessage("Device Information");
                builder.setView( R.layout.dialog_device_information );
                builder.setPositiveButton("Close", null);
                final AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            });
        }

//        Preference deviceconnect_preference = findPreference("device_connect");
//        if (deviceconnect_preference != null) {
//            deviceconnect_preference.setTitle( DataHandler.DEVICE_CONNECTED ? "Disconnect Device" : "Connect Device" );
//            deviceconnect_preference.setOnPreferenceClickListener(preference -> {
//                DataHandler.DEVICE_CONNECTED = !DataHandler.DEVICE_CONNECTED;
//                deviceconnect_preference.setTitle( DataHandler.DEVICE_CONNECTED ? "Disconnect Device" : "Connect Device" );
//                if (deviceinformation_preference != null) {
//                    deviceinformation_preference.setVisible( DataHandler.DEVICE_CONNECTED );
//                }
//                return true;
//            });
//        }

        Preference visitwebsite_preference = findPreference("visitwebsite");
        if (visitwebsite_preference != null) {
            visitwebsite_preference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.zeeshanvirani.com/coffee/faq.html")); // CREATE ONE PAGE SITE FOR FAQ AND CONTACT INFO
                startActivity(intent);
                return true;
            });
        }

        Preference signout_preference = findPreference( "signout" );
        if (signout_preference != null) {
            signout_preference.setOnPreferenceClickListener(preference -> {
                FirebaseAuth.getInstance().signOut();
                startActivity( new Intent( getContext(), LaunchActivity.class ) );
                getActivity().finishAffinity();
                return true;
            });
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(TAG, "in OnSharedPreferenceChanged");
        assert FirebaseAuth.getInstance().getCurrentUser() != null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( requireActivity() );
        Map<String, Object> data = new HashMap<>();
        switch (s) {
            case "notifications_brewing_status":
                if ( sp.getBoolean( "notifications_brewing_status", true) ) {
                    Snackbar.make(requireActivity().findViewById(R.id.settings_heading), "You will be notified about the status of your brews.", Snackbar.LENGTH_SHORT)
                            .show();
                    new Thread(() -> {
                        try {
                            Thread.sleep( 5000 );
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        DataHandler.sendNotification( requireActivity(), "Test Notification", "This is a test", 1234);
                    }).start();
                } else {
                    Snackbar.make(requireActivity().findViewById(R.id.settings_heading), "You will no longer be notified about the status of your brews.", Snackbar.LENGTH_SHORT)
                            .show();
                }

                data.put("notifyBrewingStatus", sp.getBoolean( "notifications_brewing_status", true) );
                FirebaseFirestore.getInstance().collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .set(data, SetOptions.merge());
                break;

            case "notifications_maintenance_reminders":
                if ( sp.getBoolean( "notifications_maintenance_reminders", true) ) {
                    Snackbar.make(requireActivity().findViewById(R.id.settings_heading), "You will be notified about maintenance items.", Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    Snackbar.make(requireActivity().findViewById(R.id.settings_heading), "You will no longer be notified about maintenance items.", Snackbar.LENGTH_SHORT)
                            .show();
                }

                data.put("notifyMaintenanceReminders", sp.getBoolean( "notifications_maintenance_reminders", true) );
                FirebaseFirestore.getInstance().collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .set(data, SetOptions.merge());
                break;

            case "notifications_darklight_mode":
                requireActivity().recreate();
                break;

            case "account_name":
                data.put("name", sp.getString( "account_name", "" ));

                FirebaseFirestore.getInstance().collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .set(data, SetOptions.merge());
                break;

            case "account_email":
                String newEmail = sp.getString( "account_email", "" );
                FirebaseAuth.getInstance().getCurrentUser().updateEmail( newEmail );
                break;

        }
    }

}