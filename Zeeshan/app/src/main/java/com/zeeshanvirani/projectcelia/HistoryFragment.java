package com.zeeshanvirani.projectcelia;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class HistoryFragment extends Fragment {

    String TAG = "HistoryFragment";

    Context context;
    RecyclerView historyLayout;
    TextView noItemTextView;

    List<Brews> brewHistoryList = new ArrayList<>();

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

        noItemTextView = view.findViewById(R.id.empty_view);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d(TAG, "User does not exist");
        }

        // Get all brew items from database that match the user id
        FirebaseFirestore.getInstance().collection("brews")
                .whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            brewHistoryList.add(new Brews(
                                    document.getId(),
                                    document.getData().get("user_id").toString(),
                                    document.getData().get("date").toString(),
                                    document.getData().get("time").toString(),
                                    document.getData().get("roast_type").toString(),
                                    document.getData().get("rating").toString(),
                                    document.getData().get("bean_type").toString(),
                                    document.getData().get("strength").toString()
                            ));
                        }

                        brewHistoryList.sort(Comparator.comparing(Brews::getDateTime).reversed());

                        // Set adapter and layout manager for recycler view
                        // This will automate the creation and filling of data on the page
                        historyLayout.setAdapter( new BrewHistoryListAdapter(getActivity(), context, getParentFragmentManager(), brewHistoryList) );
                        historyLayout.setLayoutManager( new LinearLayoutManager( context ) );

                        if (brewHistoryList.isEmpty()) {
                            noItemTextView.setVisibility(View.VISIBLE);
                            historyLayout.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });

        return view;
    }

}