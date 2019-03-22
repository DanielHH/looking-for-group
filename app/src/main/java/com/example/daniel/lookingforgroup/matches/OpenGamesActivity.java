package com.example.daniel.lookingforgroup.matches;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.example.daniel.lookingforgroup.AsyncResponse;
import com.example.daniel.lookingforgroup.GetData;
import com.example.daniel.lookingforgroup.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class OpenGamesActivity extends AppCompatActivity implements AsyncResponse {

    ArrayList<Match> matches;

    RecyclerView rvMatches;
    MatchesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_games);

        rvMatches = (RecyclerView) findViewById(R.id.matches_view);
        getMatchData();
        // This happens after, as we must set the RecyclerView before a response comes in

        rvMatches.setHasFixedSize(true);
        rvMatches.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void processFinish(String response) {
        JSONArray respArray;
        try {
            respArray = new JSONArray(response);
            matches = Match.createMatchList(respArray);
            adapter = new MatchesAdapter(matches, new MatchesAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Match match) {
                    navigateToMatch(match);
                }
            });

            rvMatches.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void navigateToMatch(Match match) {
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.putExtra("match", match);
        startActivity(intent);
    }

    private void getMatchData() {
        GetData getData = new GetData();
        getData.delegate = this;
        String URL = R.string.url + "matches";
        try {
            getData.execute(URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
