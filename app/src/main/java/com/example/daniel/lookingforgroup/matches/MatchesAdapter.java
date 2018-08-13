package com.example.daniel.lookingforgroup.matches;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.lookingforgroup.R;

public class MatchesAdapter extends Adapter<MatchesAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Set member variables for all views to be set as row is rendered

        public TextView myTextView;
        public Button myButton;

        // This will find all subviews in the row and set them for each row
        public ViewHolder(View itemView) {
            super(itemView);

            myTextView = (TextView) itemView.findViewById(R.id.game_name);
            myButton = (Button) itemView.findViewById(R.id.game_button);
        }
    }

    private List<Match>
}
