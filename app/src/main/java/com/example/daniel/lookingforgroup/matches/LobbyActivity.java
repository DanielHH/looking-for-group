package com.example.daniel.lookingforgroup.matches;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.daniel.lookingforgroup.AsyncResponse;
import com.example.daniel.lookingforgroup.matches.Comment.Comment;
import com.example.daniel.lookingforgroup.matches.Comment.CommentsAdapter;
import com.example.daniel.lookingforgroup.GetData;
import com.example.daniel.lookingforgroup.PostData;
import com.example.daniel.lookingforgroup.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity implements AsyncResponse {
    private Match match;
    Button joinButton;
    SharedPreferences sp;
    RecyclerView rvComments;
    CommentsAdapter adapter;
    EditText textLeaveComment;
    Button buttonLeaveComment;

    int curPlayers;
    int maxPlayers;
    String gameName;
    String location;
    int id;
    String date;

    TextView gameNameView;
    TextView locationView;
    View fractionView;
    TextView numView;
    TextView denView;

    JSONArray players; // Contains info for all players in a match
    ArrayList<Comment> comments; // Contains all comments for the match

    private String baseUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_lobby);

        baseUrl = getResources().getString(R.string.url);

        sp = getSharedPreferences("myPrefs", MODE_PRIVATE);

        rvComments = (RecyclerView) findViewById(R.id.lobbyComments);

        Bundle data = getIntent().getExtras();
        assert data != null;
        match = (Match) data.getParcelable("match");
        assert match != null;

        curPlayers = match.getCurrentPlayers();
        maxPlayers = match.getMaxPlayers();
        gameName = match.getName();
        location = match.getLocation();
        id = match.getMatchId();
        date = match.getMatchDate();

        System.out.println("Match id: " + id);
        System.out.println("Cur players: " + curPlayers);
        System.out.println("Max players: " + maxPlayers);
        System.out.println("Game name: " + gameName);
        System.out.println("Location: " + location);
        System.out.println("Date: " + date);

        joinButton = findViewById(R.id.joinButtonViewGame);
        gameNameView = findViewById(R.id.gameNameViewGame);
        locationView = findViewById(R.id.locationViewGame);
        fractionView = findViewById(R.id.fractionViewGame);
        numView = fractionView.findViewById(R.id.fracNum);
        denView = fractionView.findViewById(R.id.fracDen);

        getMatchData();
        textLeaveComment = findViewById(R.id.lobbyAddCommentText);
        buttonLeaveComment = findViewById(R.id.lobbyPostButton);

        // rvComments set size after all other views have been initialized
        rvComments.setHasFixedSize(true);
        rvComments.setLayoutManager(new LinearLayoutManager(this));

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinOrLeaveGame();
            }
        });
        buttonLeaveComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveComment();
            }
        });

        gameNameView.setText(gameName);
        locationView.setText(location);
        numView.setText(String.valueOf(curPlayers));
        denView.setText(String.valueOf(maxPlayers));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getMatchData() {
        GetData getData = new GetData();
        getData.delegate = this;
        StringBuilder url = new StringBuilder(baseUrl + "matches/");
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

            JSONArray commentData = data.getJSONArray("comments");
            comments = Comment.createCommentList(commentData);
            adapter = new CommentsAdapter(comments);
            rvComments.setAdapter(adapter);

            curPlayers = data.getInt("cur_players");
            int myId = Integer.parseInt(sp.getString("userId", "-1"));

            // Both buttons reenable after the response has been returned
            joinButton.setEnabled(true);
            buttonLeaveComment.setEnabled(true);

            rvComments.scrollToPosition(comments.size() - 1);

            if (isInPlayers(myId)) {
                joinButton.setText("Leave");
            } else if (curPlayers < maxPlayers) {
                joinButton.setText("Join");
            } else {
                joinButton.setEnabled(false);
                joinButton.setText("Full");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isInPlayers(Integer myId) {
        try {
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                if (player.getInt("id") == myId) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void joinOrLeaveGame() {
        // Disable buttons to prevent repeated requests to the server
        buttonLeaveComment.setEnabled(false);
        joinButton.setEnabled(false);

        PostData postData = new PostData();
        postData.setSP(sp);
        postData.delegate = this;
        StringBuilder url = new StringBuilder(baseUrl + "matches/");
        url.append(id);
        url.append("/join");
        try {
            postData.execute(url.toString(), "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void leaveComment() {
        if (textLeaveComment.getText().toString().equals("")) return;

        // Disable buttons to prevent repeated requests to the server
        buttonLeaveComment.setEnabled(false);
        joinButton.setEnabled(false);

        String postBody =  '\"' + textLeaveComment.getText().toString() + '\"';
        textLeaveComment.setText("");

        PostData postData = new PostData();
        postData.delegate = this;
        StringBuilder url = new StringBuilder(baseUrl + "matches/");
        url.append(id);

        postData.setSP(sp);

        try {
            postData.execute(url.toString(), postBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
