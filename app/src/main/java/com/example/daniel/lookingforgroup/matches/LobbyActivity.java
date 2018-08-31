package com.example.daniel.lookingforgroup.matches;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.lookingforgroup.R;

public class LobbyActivity extends AppCompatActivity {

    private Match match;
    Button joinButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_lobby);

        match = getIntent().getParcelableExtra("match");
        int curPlayers = match.getCurrentPlayers();
        int maxPlayers = match.getMaxPlayers();
        String title = match.getName();
        String location = match.getLocation();

        TextView titleView = findViewById(R.id.titleViewGame);
        View fractionView = findViewById(R.id.fractionViewGame);
        TextView numView = fractionView.findViewById(R.id.fracNum);
        TextView denView = fractionView.findViewById(R.id.fracDen);
        joinButton = findViewById(R.id.joinButtonViewGame);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGame();
            }
        });

        titleView.setText(title);
        numView.setText(curPlayers);
        denView.setText(maxPlayers);

        if (curPlayers < maxPlayers) {
            joinButton.setEnabled(false);
        } else joinButton.setEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void joinGame() {
        // TODO: send a join game request to the server
        // TODO: change the function of the joinButton to allow the player to leave the match

    }
}
