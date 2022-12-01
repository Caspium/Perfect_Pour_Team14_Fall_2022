package com.zeeshanvirani.projectcelia;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BrewHistoryListAdapter extends RecyclerView.Adapter<BrewHistoryListAdapter.MyViewHolder> {

    public final String TAG = "BrewHistoryListAdapter";

    // Define variables
    List<Brews> brews;
    Context ctx; // Context instance given by calling class
    Activity activity;
    FragmentManager fm; // FragmentManager instance given by calling class

    // Constructor class
    // ctx = Context instance
    // fragmentManager = FragmentManager instance
    // ids, dates, roasts, ratings = Data provided by database
    public BrewHistoryListAdapter(Activity activity, Context ctx, FragmentManager fragmentManager, List<Brews> brews) {
        this.activity = activity;
        this.ctx = ctx;
        this.fm = fragmentManager;
        this.brews = brews;
    }

    // Inflates one instance of brew_history_card
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate( R.layout.brew_history_card, parent, false );
        return new MyViewHolder( view );
    }

    // Sets the data within the holder instance
    // Set the Date and Roast Type textView to the proper string
    // Sets the Rating textView if rating is available, otherwise displays a button to allow user to
    // rate the brew
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String year = brews.get(position).date.substring(brews.get(position).date.length() - 4);
        holder.dateText.setText( brews.get(position).date.substring(0, brews.get(position).date.length() - 4) );
        holder.yearText.setText(year);
        holder.roastText.setText( brews.get(position).roastTypes );
        holder.beanTypeText.setText( brews.get(position).beanType );

        if ( brews.get(position).rating.equals("null") ) {
            holder.ratingText.setVisibility( View.INVISIBLE );
            holder.strengthText.setVisibility( View.INVISIBLE );
            holder.ratingButton.setVisibility( View.VISIBLE );
            holder.ratingButton.setOnClickListener(view -> {
                DialogFragment dialog = new RateDialogFragment( this, brews.get(position).firebase_id );
                dialog.show( fm, "rate" );});
        } else {
            holder.ratingText.setVisibility( View.VISIBLE );
            holder.strengthText.setVisibility( View.VISIBLE );
            holder.ratingButton.setVisibility( View.INVISIBLE );
            holder.ratingText.setText(getRatingString(brews.get(position).rating));
            holder.strengthText.setText(getStrengthString(brews.get(position).strength));
        }
    }

    // Returns the number of items in the brewing history table
    @Override
    public int getItemCount() {
        return brews.size();
    }

    // Updates the data of a brew on the database
    public void updateBrew( String dataID, int rating, String strength ) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("brews")
                    .document( dataID );
            docRef.update("rating", rating);
            docRef.update( "strength", strength );
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                Log.d(TAG, "Update Successful rating and strength values on Firebase");
                for (int i = 0; i < getItemCount(); i++) {
                    if (brews.get(i).firebase_id.equals(dataID)) {
                        brews.get(i).rating = String.valueOf(rating);
                        brews.get(i).strength = strength;
                        this.notifyItemChanged(i);
                        break;
                    }
                }
            });

            // Waits for grind size to be updated by ML and then reports feedback to user
            docRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String coarseness;
                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    if (snapshot.getData() != null) {
                        coarseness = (String) snapshot.getData().get(DataHandler.DB_COARSENESS_RECOMMENDATION);
                        if (coarseness != null) {
                            if (coarseness.equals("0.0")) coarseness = "same";
                            else if (coarseness.equals("1.0")) coarseness = "more";
                            else if (coarseness.equals("2.0")) coarseness = "less";
                            else coarseness = "same";

                            DataHandler.sendLargeNotification(activity,
                                    "Feedback Recommendation",
                                    "Based on your feedback, on your next brew try making the coffee grinds " + coarseness + " coarse.",
                                    123,
                                    DataHandler.NOTIFICATION_TYPE.RECOMMENDATION);
                        }
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            });
        }
    }

    // Returns the string used for the UI
    public String getRatingString( String rating ) {
        if (!rating.equals("null")) return rating + "/10";
        return rating;
    }

    // Returns the strength string to be used by the UI
    public String getStrengthString( String strength ) {
        if (strength.equals("1")) return "Too Weak";
        else if (strength.equals("2")) return "Perfect";
        else return "Too Strong";
    }

    // Handles inflated views and allows RecyclerView to be able to modify internal data.
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView dateText, yearText, roastText, ratingText, beanTypeText, strengthText;
        Button ratingButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById( R.id.history_date );
            yearText = itemView.findViewById( R.id.history_date_year );
            roastText = itemView.findViewById( R.id.history_roasttype );
            beanTypeText = itemView.findViewById( R.id.history_beantype );
            ratingText = itemView.findViewById( R.id.history_rating );
            ratingButton = itemView.findViewById( R.id.history_rating_button );
            strengthText = itemView.findViewById( R.id.history_strength );
        }
    }
}


