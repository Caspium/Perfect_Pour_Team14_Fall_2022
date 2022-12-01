package com.zeeshanvirani.projectcelia;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothSocketHandler {

    private static final String TAG = "ProjectCelia:BluetoothSocketHandler";

    private final BrewingProcess bpInstance;
    private final BluetoothSocket btSocket;

    private final String target_temp;
    private final String target_saturation;
    private final String cupsize;

    private OutputStream outputStream;
    private InputStream inputStream;

    // Constructor
    public BluetoothSocketHandler( BrewingProcess instance, BluetoothSocket btSocket,
                                   String target_temp, String target_saturation, String cupsize ) {
        this.bpInstance = instance;
        this.btSocket = btSocket;
        this.target_saturation = target_saturation;
        this.target_temp = target_temp;
        this.cupsize = cupsize;
        try {
            Log.d(TAG, "Connecting to bluetooth device...");
            outputStream = btSocket.getOutputStream();
            inputStream = btSocket.getInputStream();
            btSocket.connect();
            new InputStreamThread().start();
            DataHandler.DEVICE_CONNECTED = true;
            Log.d(TAG, "Connected.");
            bpInstance.statusUpdate("CONNECTED");

            // Was app already brewing?
            if ( !DataHandler.IS_BREWING )
                // App was not already brewing. Send start message.
                sendMessage("START_BREW:" + target_temp + ":" + target_saturation + ":" + cupsize);
            else
                Log.d(TAG, "There is already a brew in progress. Resuming...");

            DataHandler.updateIsBrewing(bpInstance.getApplicationContext(), true);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Unable to connect to device.");
            bpInstance.statusUpdate("UNABLE_TO_CONNECT");
            closeSocket();
        }
    }

    // Closes the input and output streams and the socket itself to close the bluetooth connection
    public void closeSocket() {
        try {
            Thread.sleep(1000);
            Log.d(TAG, "Closing Bluetooth Socket");
            inputStream.close();
            outputStream.close();
            inputStream = null;
            outputStream = null;
            btSocket.close();
            DataHandler.DEVICE_CONNECTED = false;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Sends the "msgToSend" to the Pi
    public void sendMessage(String msgToSend) {
        Log.d(TAG, msgToSend);
        try {
            outputStream.write( msgToSend.getBytes() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // InputStream for the bluetooth connection that constantly reads
    public class InputStreamThread extends Thread {
        public void run() {
            byte[] inBuffer = new byte[1024];
            int numBytes;

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = inputStream.read(inBuffer);
                    String readMessage = new String(inBuffer, 0, numBytes);
                    Log.d(TAG, readMessage);
                    bpInstance.runOnUiThread(() -> bpInstance.statusUpdate(readMessage));
                } catch (IOException e) {
                    Log.d(TAG, "Input Stream was Disconnected. " + e);
                    DataHandler.DEVICE_CONNECTED = false;
                    break;
                }
            }
        }
    }
}
