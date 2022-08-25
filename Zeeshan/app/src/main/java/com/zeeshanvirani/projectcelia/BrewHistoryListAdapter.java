package com.zeeshanvirani.projectcelia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

public class BrewHistoryListAdapter extends RecyclerView.Adapter<BrewHistoryListAdapter.MyViewHolder> {

    // Define variables
    String[] ids, dates, roasts, ratings; // Variables containing data gathered from database
    Context ctx; // Context instance given by calling class
    FragmentManager fm; // FragmentManager instance given by calling class

    // Constructor class
    // ctx = Context instance
    // fragmentManager = FragmentManager instance
    // ids, dates, roasts, ratings = Data provided by database
    public BrewHistoryListAdapter(Context ctx, FragmentManager fragmentManager, String[] ids, String[] dates, String[] roasts, String[] ratings) {
        this.ctx = ctx;
        this.fm = fragmentManager;
        this.ids = ids;
        this.dates = dates;
        this.roasts = roasts;
        this.ratings = ratings;
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
        holder.dateText.setText( dates[position] );
        holder.roastText.setText( roasts[position] );

        if ( ratings[position].equals("null") ) {
            holder.ratingText.setVisibility( View.INVISIBLE );
            holder.ratingButton.setVisibility( View.VISIBLE );
            holder.ratingButton.setOnClickListener(view -> {
                DialogFragment dialog = new RateDialogFragment( this, ids[position] );
                dialog.show( fm, "rate" );});
        } else {
            holder.ratingText.setText(ratings[position]);
        }
    }

    // Returns the number of items in the brewing history table
    @Override
    public int getItemCount() {
        return dates.length;
    }

    // Handles inflated views and allows RecyclerView to be able to modify internal data.
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView dateText, roastText, ratingText;
        Button ratingButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById( R.id.history_date );
            roastText = itemView.findViewById( R.id.history_roasttype );
            ratingText = itemView.findViewById( R.id.history_rating );
            ratingButton = itemView.findViewById( R.id.history_rating_button );
        }
    }
}


