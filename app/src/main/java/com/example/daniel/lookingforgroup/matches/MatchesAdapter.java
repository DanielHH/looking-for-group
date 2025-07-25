package com.example.daniel.lookingforgroup.matches;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.daniel.lookingforgroup.R;

import java.util.List;

public class MatchesAdapter extends Adapter<MatchesAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Set member variables for all views to be set as row is rendered

        public TextView gameName;
        public TextView location;
        public TextView currentPlayers;
        public TextView maxPlayers;

        // This will find all subviews in the row and set them for each row
        public ViewHolder(View itemView) {
            super(itemView);
            View fractionView = (View) itemView.findViewById(R.id.item_fraction);
            gameName = (TextView) itemView.findViewById(R.id.game_name);
            location = (TextView) itemView.findViewById(R.id.location);
            currentPlayers = (TextView) fractionView.findViewById(R.id.fracNum);
            maxPlayers = (TextView) fractionView.findViewById(R.id.fracDen);
        }
    }

    private List<Match> matches;
    private OnItemClickListener listener;

    // Constructor to be fed all data to be shown
    public MatchesAdapter(List<Match> matches, OnItemClickListener listener) {
        this.matches = matches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View matchView = inflater.inflate(R.layout.item_match, parent, false);

        return new ViewHolder(matchView);
    }

    @Override
    public void onBindViewHolder(MatchesAdapter.ViewHolder viewHolder, int position) {
        final Match match = matches.get(position);

        TextView gameName = viewHolder.gameName;
        gameName.setText(match.getName());

        TextView location = viewHolder.location;
        location.setText(match.getLocation());

        TextView currentPlayers = viewHolder.currentPlayers;
        TextView maxPlayers = viewHolder.maxPlayers;

        currentPlayers.setText(Integer.toString(match.getCurrentPlayers()));
        maxPlayers.setText(Integer.toString(match.getMaxPlayers()));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                listener.onItemClick(match);
            }
        });
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Match match);
    }
}
