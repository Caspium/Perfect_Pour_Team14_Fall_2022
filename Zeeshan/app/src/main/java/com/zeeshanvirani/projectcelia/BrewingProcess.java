package com.zeeshanvirani.projectcelia;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

public class BrewingProcess extends AppCompatActivity {

    public static final String TAG = "ProjectCelia:BrewingProcess";

    public static final String TAG_FIREBASE_ID = "firebaseid";
    public static final String TAG_TEMPERATURE = "temperature";
    public static final String TAG_TARGET_SATURATION = "saturation";
    public static final String TAG_CUP_SIZE = "cupsize";

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    private String temperature, target_saturation, firebaseid, cupsize;

    TextView brewing_text;
    MaterialButton returnhome_btn;
    MaterialButton continuebrewing_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewing_process);

        // Get extras from Intent
        Intent i = getIntent();
        temperature = i.getStringExtra(TAG_TEMPERATURE);
        target_saturation = i.getStringExtra(TAG_TARGET_SATURATION);
        firebaseid = i.getStringExtra(TAG_FIREBASE_ID);
        cupsize = i.getStringExtra(TAG_CUP_SIZE);

        // Initialize views
        brewing_text = findViewById(R.id.brewing_text);
        returnhome_btn = findViewById(R.id.brewing_gohome);
        continuebrewing_btn = findViewById(R.id.brewing_continuebrewing);

        // End activity on return button clicked
        returnhome_btn.setOnClickListener(view -> {
            finish();
        });
        continuebrewing_btn.setOnClickListener(view -> continueBrewingClicked());

        // Check if device is already connected, if not register broadcast receiver for bluetooth
        if (!DataHandler.DEVICE_CONNECTED) {
            Log.d(TAG, "No Device connected. Registering receiver.");
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(myBroadcastReceiver, filter);
            startPairing();
        } else
            Log.d(TAG, "Device is already connected. Receiver NOT registered.");

        // Waits until device is connected, then beginBrewing is called from establishHandler()
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(myBroadcastReceiver);
        if (DataHandler.DEVICE_CONNECTED) {
            DataHandler.btSocketHandler.closeSocket();
        }
        super.onDestroy();
    }

    // Called when messages received from bluetooth input stream
    public void statusUpdate(String newStatus) {
        switch (newStatus) {
            case "CONNECTED":
                brewing_text.setText("Connected to device.");
                break;
            case "UNABLE_TO_CONNECT":
                brewing_text.setText("Unable to connect to device.");
                returnhome_btn.setVisibility(View.VISIBLE);
                break;
            case "OUT_OF_WATER":
                brewing_text.setText("The reservoir is out of water.");
                DataHandler.sendNotification(this, "Out of Water", "The water tank is out of water. Please refill and hit continue to resume brewing.", 1234, DataHandler.NOTIFICATION_TYPE.BREWSTATUS);
                continuebrewing_btn.setVisibility(View.VISIBLE);
                break;
            case "STARTED_HEATING":
                brewing_text.setText("Heating water.");
                break;
            case "STARTED_POURING":
                brewing_text.setText("Pouring water.");
                break;
            case "BREWING_COMPLETE":
                brewing_text.setText("Your coffee is ready!");
                DataHandler.sendNotification(this, "Your coffee is ready!", "", 1234, DataHandler.NOTIFICATION_TYPE.BREWSTATUS);
                // DISCONNECT BLUETOOTH DEVICE
                DataHandler.btSocketHandler.closeSocket();
                DataHandler.btSocketHandler = null;
                DataHandler.updateIsBrewing(this, false);

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    DocumentReference docRef = FirebaseFirestore.getInstance().collection("brews")
                            .document( firebaseid );
                    docRef.update(DataHandler.DB_STATUS, DataHandler.DB_STATUS_DONE);
                    docRef.get().addOnSuccessListener(documentSnapshot -> {
                        Log.d(TAG, "Updated status of brew on database.");
                    });
                }

                returnhome_btn.setVisibility(View.VISIBLE);
                break;
            default:
                brewing_text.setText(newStatus);
                DataHandler.sendNotification(this, "An error has occurred", newStatus, 1234, DataHandler.NOTIFICATION_TYPE.BREWSTATUS);
                // DISCONNECT BLUETOOTH DEVICE
                DataHandler.btSocketHandler.closeSocket();
                DataHandler.btSocketHandler = null;
                DataHandler.updateIsBrewing(this, false);
                returnhome_btn.setVisibility(View.VISIBLE);
                break;
        }
    }

    // Start bluetooth pairing process
    private void startPairing() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // If device does not have a bluetooth adapter, quit.
        if (btAdapter == null) return;

        try {
            // Check if bluetooth is on
            if (!btAdapter.isEnabled()) {
                // Bluetooth is off, ask user to enable
                Log.d(TAG, "Enabled Bluetooth Adapter");
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(turnOn);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                    if (!isGranted.containsValue(false)) {
                        // Check if bluetooth is already in discoverable mode, if yes: cancel discovery
                        if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
                        Log.d(TAG, String.valueOf(btAdapter.startDiscovery()));
                    } else {
                        Log.d(TAG, "Bluetooth permission not given.");
                    }
                }).launch(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT});
            } else {
                // Check if bluetooth is already in discoverable mode, if yes: cancel discovery
                if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
                Log.d(TAG, String.valueOf(btAdapter.startDiscovery()));
            }

        } catch (SecurityException e ) {
            e.printStackTrace();
        }
    }

    // Creates a bluetooth socket for "device"
    public void establishHandler( BluetoothDevice device ) {
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            btAdapter.cancelDiscovery();
            Log.d(TAG, "Found a device. Attempting to connect.");
            btSocket = device.createInsecureRfcommSocketToServiceRecord( DataHandler.MY_UUID );
            DataHandler.btSocketHandler = new BluetoothSocketHandler( this, btSocket, temperature, target_saturation, cupsize );
        } catch (SecurityException | IOException e) {
            Log.d(TAG, "Error connecting to the device.");
        }
    }

    public void continueBrewingClicked() {
        // Hide the button
        continuebrewing_btn.setVisibility(View.GONE);
        // Send the continue message
        DataHandler.btSocketHandler.sendMessage("CONTINUE_BREWING");
        // Status update will come from Pi
    }

    private final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        boolean deviceFound = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                Log.d(BrewingProcess.TAG, "Started Bluetooth Discovery");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog
                if ( !deviceFound ) {
                    Log.d(BrewingProcess.TAG, "Device not found.");
                    brewing_text.setText( R.string.brewingprocess_cannotfinddevice );
                    returnhome_btn.setVisibility( View.VISIBLE );
                }
                Log.d(BrewingProcess.TAG, "Discovery Completed");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if ( device.getAddress().equals( DataHandler.DEVICE_MACADDR ) ) {
                    deviceFound = true;
                    establishHandler(device);
                }
            }
        }
    };
}