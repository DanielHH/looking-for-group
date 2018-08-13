package com.example.daniel.lookingforgroup.matches;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.daniel.lookingforgroup.R;

import java.util.ArrayList;

public class OpenGamesActivity extends AppCompatActivity {

    ArrayList<Match> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecyclerView rvMatches = (RecyclerView) findViewById(R.id.rvMatches);

        matches = Match.createMatchList(20);

        MatchesAdapter adapter = new MatchesAdapter(matches);

        rvMatches.setAdapter(adapter);

        rvMatches.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
