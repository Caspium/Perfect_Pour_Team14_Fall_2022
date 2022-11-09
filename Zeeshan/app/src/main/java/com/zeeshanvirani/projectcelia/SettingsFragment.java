package com.zeeshanvirani.projectcelia;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends Fragment  {

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        assert getActivity() != null;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment_container, new PreferenceFragment())
                .commit();

        return view;
    }
}