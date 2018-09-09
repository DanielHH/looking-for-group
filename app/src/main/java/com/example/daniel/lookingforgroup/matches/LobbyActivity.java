package com.example.daniel.lookingforgroup.matches;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.lookingforgroup.AsyncResponse;
import com.example.daniel.lookingforgroup.GetData;
import com.example.daniel.lookingforgroup.PostData;
import com.example.daniel.lookingforgroup.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LobbyActivity extends AppCompatActivity implements AsyncResponse {
    private Match match;
    Button joinButton;
    SharedPreferences sp;

    int curPlayers;
    int maxPlayers;
    String title;
    String location;
    int id;

    JSONArray players; // Contains info for all players in a match
    JSONArray comments; // Contains all comments for the match

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_lobby);
        sp = getSharedPreferences("myPrefs", MODE_PRIVATE);

        Bundle data = getIntent().getExtras();
        assert data != null;
        match = (Match) data.getParcelable("match");
        assert match != null;

        curPlayers = match.getCurrentPlayers();
        maxPlayers = match.getMaxPlayers();
        title = match.getName();
        location = match.getLocation();

        id = match.getMatchId() + 1;
        // There is a strange error when creating match objects where the id returned by the
        // response is decremented by one. This is a temporary solution.

        joinButton = findViewById(R.id.joinButtonViewGame);

        getMatchData();

        TextView titleView = findViewById(R.id.titleViewGame);
        TextView locationView = findViewById(R.id.locationViewGame);
        View fractionView = findViewById(R.id.fractionViewGame);
        TextView numView = fractionView.findViewById(R.id.fracNum);
        TextView denView = fractionView.findViewById(R.id.fracDen);


        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinOrLeaveGame();
            }
        });

        titleView.setText(title);
        locationView.setText(location);
        numView.setText(String.valueOf(curPlayers));
        denView.setText(String.valueOf(maxPlayers));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getMatchData() {
        GetData getData = new GetData();
        getData.delegate = this;
        StringBuilder url = new StringBuilder("http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/matches/");
        url.append(id);
        try {
            getData.execute(url.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processFinish(String response) {
        try {
            JSONObject data = new JSONObject(response);
            players = data.getJSONArray("played_by");
            comments = data.getJSONArray("comments");
            curPlayers = data.getInt("cur_players");
            int myId = Integer.parseInt(sp.getString("userId", "-1"));

            joinButton.setEnabled(true);

            if (isInPlayers(myId)) {
                joinButton.setText("Leave");
            } else if (curPlayers < maxPlayers) {
                joinButton.setText("Join");
            } else {
                joinButton.setEnabled(false);
                joinButton.setText("Full");
            }

            // TODO: create CommentsAdapter and Comments object

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isInPlayers(Integer myId) {
        try {
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                int playerId = player.getInt("id");
                if (playerId == myId) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void joinOrLeaveGame() {
        PostData postData = new PostData();
        postData.delegate = this;
        StringBuilder url = new StringBuilder("http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/matches/");
        url.append(id);
        url.append("/join");
        try {
            postData.execute(url.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
