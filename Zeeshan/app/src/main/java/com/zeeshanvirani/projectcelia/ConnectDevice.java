package com.zeeshanvirani.projectcelia;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConnectDevice extends Fragment {

    public ConnectDevice() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Find device by name
        // Store mac address in preferences/database
        // Set device connected to true

        return inflater.inflate(R.layout.fragment_connect_device, container, false);
    }

}