package com.zeeshanvirani.projectcelia;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Static fields that are shared across the application
// Handles sending and retrieving various pieces of data
public class DataHandler {

    public static final String TAG = "ProjectCelia:DataHandler";

    // Bluetooth Connection Information
    public static boolean IS_BREWING = false;
    public static boolean DEVICE_CONNECTED = false;
    public static BluetoothSocketHandler btSocketHandler = null;
    public static String DEVICE_MACADDR = "DC:A6:32:E5:6B:9D";
    public static UUID MY_UUID = UUID.fromString("b3f75a8f-fa4b-4dbc-8e79-51a486a30fa9");

    // Database Variables
    public static String DB_USER_ID = "user_id";
    public static String DB_DATE = "date";
    public static String DB_TIME = "time";
    public static String DB_ROAST_TYPE = "roast_type";
    public static String DB_BEAN_TYPE = "bean_type";
    public static String DB_CUP_SIZE = "cup_size";
    public static String DB_RATING = "rating";
    public static String DB_TEMPERATURE = "temperature";
    public static String DB_TARGET_SATURATION = "target_saturation";
    public static String DB_STRENGTH = "strength";
    public static String DB_STATUS = "status";
    public static String DB_COARSENESS_RECOMMENDATION = "grind_size";

    public static String DB_STATUS_STARTED = "started";
    public static String DB_STATUS_ERROR = "error";
    public static String DB_STATUS_DONE = "done";

    // Notification Manager Information
    static String CHANNEL_ID = "com.zeeshanvirani.projectcelia.notifications";
    enum NOTIFICATION_TYPE { BREWSTATUS, MAINTENANCE, RECOMMENDATION}

    // Update all preferences
    public static void updateSharedPreferences( Context context ) {
        assert FirebaseAuth.getInstance().getCurrentUser() != null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        DocumentReference docRef;

        docRef = FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            String name = (String) documentSnapshot.get("name");
            Object notifyBrewingStatus_Object = documentSnapshot.get("notifyBrewingStatus");
            Object notifyMaintenanceReminders_Object = documentSnapshot.get("notifyMaintenanceReminders");
            boolean notifyBrewingStatus = true;
            boolean notifyMaintenanceReminders = true;
            if ( notifyBrewingStatus_Object == null || notifyMaintenanceReminders_Object == null ) {
                Log.d(TAG, "notifyBrewingStatus or notifyMaintenanceReminders returned null. Using default value of true.");
            } else {
                notifyBrewingStatus = (boolean) notifyBrewingStatus_Object;
                notifyMaintenanceReminders = (boolean) notifyMaintenanceReminders_Object;
            }
            editor.putString("account_name", name);
            editor.putBoolean( "notifications_brewing_status", notifyBrewingStatus);
            editor.putBoolean( "notifications_maintenance_reminders", notifyMaintenanceReminders);
            editor.apply();
        });
        editor.putString("account_email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        editor.apply();
    }

    public static void createNotificationChannel( LaunchActivity activity ) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "Maintenance and Brewing Notifications";
        String description = "Notifies maintenance intervals and status of brewing.";

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Log.d(TAG, "Created Notification Channel with id " + CHANNEL_ID);
    }

    // Sends notification to user
    public static void sendNotification( Activity activity, String textTitle, String textContent, int notificationId, NOTIFICATION_TYPE type ) {
        if ( type == NOTIFICATION_TYPE.BREWSTATUS ) {
            if (!PreferenceManager
                    .getDefaultSharedPreferences( activity.getApplicationContext() )
                    .getBoolean("notifications_brewing_status", true)
            ) {
                return;
            }
        }

        if ( type == NOTIFICATION_TYPE.MAINTENANCE ) {
            if (!PreferenceManager
                    .getDefaultSharedPreferences( activity.getApplicationContext() )
                    .getBoolean("notifications_maintenance_reminders", true)
            ) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lock)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
        notificationManager.notify(notificationId, builder.build());

        Log.d(TAG, "Sent Notification with id " + notificationId + " on channel " + CHANNEL_ID);
    }

    // Sends large notification to user
    public static void sendLargeNotification( Activity activity, String textTitle, String textContent, int notificationId, NOTIFICATION_TYPE type ) {
        if ( type == NOTIFICATION_TYPE.BREWSTATUS ) {
            if (!PreferenceManager
                    .getDefaultSharedPreferences( activity.getApplicationContext() )
                    .getBoolean("notifications_brewing_status", true)
            ) {
                return;
            }
        }

        if ( type == NOTIFICATION_TYPE.MAINTENANCE ) {
            if (!PreferenceManager
                    .getDefaultSharedPreferences( activity.getApplicationContext() )
                    .getBoolean("notifications_maintenance_reminders", true)
            ) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lock)
                .setContentTitle(textTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(textContent))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
        notificationManager.notify(notificationId, builder.build());

        Log.d(TAG, "Sent Notification with id " + notificationId + " on channel " + CHANNEL_ID);
    }

    // Updates the isBrewing variable in the shared preferences
    public static void updateIsBrewing( Context context, boolean isBrewing ) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isBrewing", isBrewing);
        editor.apply();
        IS_BREWING = isBrewing;
    }

}
