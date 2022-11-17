package com.zeeshanvirani.projectcelia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class BrewingProcess extends AppCompatActivity {

    public static final String TAG = "ProjectCelia:BrewingProcess";
    public static final String TAG_TEMPERATURE = "temperature";
    public static final String TAG_TARGET_SATURATION = "saturation";

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    private String temperature, target_saturation;

    public MyBroadcastReceiver myReceiver;

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

        // Initialize views
        brewing_text = findViewById(R.id.brewing_text);
        returnhome_btn = findViewById(R.id.brewing_gohome);
        continuebrewing_btn = findViewById(R.id.brewing_continuebrewing);

        // End activity on return button clicked
        returnhome_btn.setOnClickListener(view -> finish());
        continuebrewing_btn.setOnClickListener(view -> continueBrewingClicked() );

        // Setup broadcast Receivers
        myReceiver = new MyBroadcastReceiver(this);

        // Check if device is already connected, if not register broadcast receiver for bluetooth
        if (!DataHandler.DEVICE_CONNECTED) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            myReceiver.register(this, filter); // register
            startPairing();
        }

        // Waits until device is connected, then beginBrewing is called from establishHandler()

    }

    // Called when messages received from bluetooth input stream
    public void statusUpdate( String newStatus ) {
        switch (newStatus) {
            case "OUT_OF_WATER":
                brewing_text.setText("The reservoir is out of water.");
                DataHandler.sendNotification( this, "Out of Water", "The water tank is out of water. Please refill and hit continue to resume brewing.", 1234);
                continuebrewing_btn.setVisibility(View.VISIBLE);
                return;
            case "UNKNOWN_ERROR":
                return;
            case "STARTED_HEATING":
                brewing_text.setText("Heating water.");
                return;
            case "STARTED_POURING":
                brewing_text.setText("Pouring water.");
                return;
            case "BREWING_COMPLETE":
                brewing_text.setText("Your coffee is ready!");
                DataHandler.sendNotification( this, "Your coffee is ready!", "", 1234);
                // DISCONNECT BLUETOOTH DEVICE
                DataHandler.btSocketHandler.closeSocket();
                DataHandler.btSocketHandler = null;
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
            // Check if bluetooth is already in discoverable mode, if yes: cancel discovery
            if ( btAdapter.isDiscovering() ) btAdapter.cancelDiscovery();
            Log.d(TAG, String.valueOf(btAdapter.startDiscovery()));
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
            DataHandler.btSocketHandler = new BluetoothSocketHandler( this, btSocket, temperature, target_saturation );
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
}

class MyBroadcastReceiver extends BroadcastReceiver {

    public boolean isRegistered;
    private final BrewingProcess activity;

    private boolean deviceFound = false;

    public MyBroadcastReceiver(BrewingProcess activity) {
        this.activity = activity;
    }

    // Registers the broadcast receiver with the system
    public void register(Context context, IntentFilter filter) {
        try {
            Log.d(BrewingProcess.TAG, "Registered Receiver Adapter");
            context.registerReceiver(this, filter);
        } finally {
            isRegistered = true;
        }
    }

    // Unregisters the receiver from the system
    public void unregister(Context context) {
        Log.d(BrewingProcess.TAG, "Unregistered Receiver Adapter");
        context.unregisterReceiver(this);
        isRegistered = false;
    }

    // Called for any event the receiver is filtered for
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            //discovery starts, we can show progress dialog or perform other tasks
            Log.d(BrewingProcess.TAG, "Started Bluetooth Discovery");
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            //discovery finishes, dismiss progress dialog
            if ( !deviceFound ) {
                Log.d(BrewingProcess.TAG, "Device not found.");
                activity.brewing_text.setText( R.string.brewingprocess_cannotfinddevice );
                activity.returnhome_btn.setVisibility( View.VISIBLE );
            }
            Log.d(BrewingProcess.TAG, "Discovery Completed");
            unregister( context );
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //bluetooth device found
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if ( device.getAddress().equals( DataHandler.DEVICE_MACADDR ) ) {
                deviceFound = true;
                activity.establishHandler(device);
            }

        }
    }
}