package com.example.daniel.lookingforgroup.matches;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.lookingforgroup.R;

import java.util.List;

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

    private List<Match> matches;

    // Constructor to be fed all data to be shown
    public MatchesAdapter(List<Match> matches) {
        this.matches = matches;
    }

    @Override
    public MatchesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View matchView = inflater.inflate(R.layout.item_match, parent, false);

        ViewHolder viewHolder = new ViewHolder(matchView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MatchesAdapter.ViewHolder viewHolder, int position) {
        Match match = matches.get(position);

        TextView textView = viewHolder.myTextView;
        textView.setText(match.getName());

        Button button = viewHolder.myButton;
        button.setText(position);
        button.setEnabled(true);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }
}
