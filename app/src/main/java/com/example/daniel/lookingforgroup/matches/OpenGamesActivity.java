package com.example.daniel.lookingforgroup.matches;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.daniel.lookingforgroup.R;

import java.util.ArrayList;

public class OpenGamesActivity extends AppCompatActivity {

    ArrayList<Match> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_games);

        RecyclerView rvMatches = (RecyclerView) findViewById(R.id.matches_view);

        rvMatches.setHasFixedSize(true);

        rvMatches.setLayoutManager(new LinearLayoutManager(this));

        matches = Match.createMatchList(20);
        MatchesAdapter adapter = new MatchesAdapter(matches);
        // TODO: get matches data information via request and feed into the matchesAdapter

        rvMatches.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
