package com.zeeshanvirani.projectcelia;

import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class RateDialogFragment extends DialogFragment {

    private static final String TAG = "ProjectCelia:RateDialogFragment";
    View view;

    private Button[] strengthTypeButtons;

    BrewHistoryListAdapter adapter;
    String dataID;

    public RateDialogFragment( BrewHistoryListAdapter adapter, String dataID ) {
        this.adapter = adapter;
        this.dataID = dataID;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getLayoutInflater().inflate( R.layout.dialog_rate_brew, null );
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.Theme_ProjectCelia_MaterialAlertDialog);
        builder.setMessage("Rate this brew:")
                .setPositiveButton("Submit", null )
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss() )
                .setView( view );
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog dialog = (AlertDialog) getDialog();
        // Check if dialog is null
        if ( dialog != null ) {

            // Set min and max values for the number picker
            NumberPicker rating = view.findViewById(R.id.rate_rating_np);
            rating.setMinValue(0);
            rating.setMaxValue(10);

            // Initialize views
            Button strength_weak = view.findViewById(R.id.button_strength_weak);
            Button strength_perfect = view.findViewById(R.id.button_strength_perfect);
            Button strength_strong = view.findViewById(R.id.button_strength_strong);

            strengthTypeButtons = new Button[]{ strength_weak, strength_perfect, strength_strong };

            strength_weak.setOnClickListener( view1 -> adjustStrengthButtons(0) );
            strength_perfect.setOnClickListener( view1 -> adjustStrengthButtons(1) );
            strength_strong.setOnClickListener( view1 -> adjustStrengthButtons(2) );

            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                // Check if a strength button is selected
                if ( getSelectedStrength().equals("") ) {
                    Log.d(TAG, "No Strength Selected.");
                    // Display error to user
                    Snackbar.make( requireActivity().findViewById(R.id.brew_history_list),
                            "You must select how strong this brew was.",
                            Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                // Let page know to refresh
                adapter.updateBrew( dataID, rating.getValue(), getSelectedStrength() );

                dialog.dismiss();
            });
        }
    }

    // Handles selection and deselection of buttons
    public void adjustStrengthButtons( int selButton ) {
        for ( int i = 0; i < strengthTypeButtons.length; i++ ) {
            strengthTypeButtons[i].setSelected( i == selButton & !strengthTypeButtons[i].isSelected() );
        }
    }

    // Returns String of strength selected
    // 0 = weak, 1 = perfect, 2 = strong
    public String getSelectedStrength() {
        for ( int i = 0; i < strengthTypeButtons.length; i++ ) {
            if ( strengthTypeButtons[i].isSelected() ) return String.valueOf(i+1);
        }
        return "";
    }
}