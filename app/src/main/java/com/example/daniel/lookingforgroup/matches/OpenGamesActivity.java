package com.example.daniel.lookingforgroup.matches;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.daniel.lookingforgroup.AsyncResponse;
import com.example.daniel.lookingforgroup.GetData;
import com.example.daniel.lookingforgroup.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class OpenGamesActivity extends AppCompatActivity implements AsyncResponse{

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
        System.out.println(response);

        JSONArray respArray;
        try {
            respArray = new JSONArray(response);
            matches = Match.createMatchList(respArray);
            adapter = new MatchesAdapter(matches);

            rvMatches.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getMatchData() {
        GetData getData = new GetData();
        getData.delegate = this;
        String URL = "http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/matches";
        try {
            getData.execute(URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
