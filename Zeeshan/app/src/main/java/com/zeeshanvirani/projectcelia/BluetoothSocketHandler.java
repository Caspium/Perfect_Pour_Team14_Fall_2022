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

    private OutputStream outputStream;
    private InputStream inputStream;

    // Constructor
    public BluetoothSocketHandler( BrewingProcess instance, BluetoothSocket btSocket,
                                   String target_temp, String target_saturation ) {
        this.bpInstance = instance;
        this.btSocket = btSocket;
        this.target_saturation = target_saturation;
        this.target_temp = target_temp;
        try {
            outputStream = btSocket.getOutputStream();
            inputStream = btSocket.getInputStream();
            createConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Establishes a connection using the socket provided
    // Starts the brewing process by sending the start message to the Pi
    private void createConnection() {
        try {
            Log.d(TAG, "Connecting to Bluetooth Device.");
            btSocket.connect();
            new InputStreamThread().start();
            DataHandler.DEVICE_CONNECTED = true;
            Log.d(TAG, "Connected.");
            sendMessage("START_BREW:" + target_temp + ":" + target_saturation);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            closeSocket();
        }
    }

    // Closes the input and output streams and the socket itself to close the bluetooth connection
    public void closeSocket() {
        try {
            Thread.sleep(1000);
            Log.d(TAG, "Closing Bluetooth Socket");
            //sendMessage("endConnection");
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
    public void sendMessage(String msgToSend ) {
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
            int numBytes; // bytes returned from read()

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
                    break;
                }
            }
        }
    }

}
