package com.zeeshanvirani.projectcelia;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HistoryFragment extends Fragment {

    Context context;
    RecyclerView historyLayout;

    // Data arrays for data gathered from database
    String[] brewHistory_ids, brewHistory_dates, brewHistory_roasttypes, brewHistory_ratings;

    // Required empty public constructor
    public HistoryFragment() {}

    // Called when Fragment is put onto screen
    // Takes in instance of LayoutInflater, the ViewGroup that the fragment will live in,
    // and a Bundle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Set context to application context for use elsewhere
        if ( getActivity() != null ) context = getActivity().getApplicationContext();

        // Initialize views within ViewGroup
        historyLayout = view.findViewById(R.id.brew_history_list);

        // Get data from database and sort into its proper arrays
        brewHistory_ids = TempDatabaseClass.brewHistory_ids;
        brewHistory_dates = TempDatabaseClass.brewHistory_dates;
        brewHistory_roasttypes = TempDatabaseClass.brewHistory_roasttypes;
        brewHistory_ratings = TempDatabaseClass.brewHistory_ratings;

        // Set adapter and layout manager for recycler view
        // This will automate the creation and filling of data on the page
        historyLayout.setAdapter( new BrewHistoryListAdapter(context, getParentFragmentManager(), brewHistory_ids, brewHistory_dates, brewHistory_roasttypes, brewHistory_ratings) );
        historyLayout.setLayoutManager( new LinearLayoutManager( context ) );

        return view;
    }

}