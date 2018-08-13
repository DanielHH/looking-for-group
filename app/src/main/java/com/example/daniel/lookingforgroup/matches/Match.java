package com.example.daniel.lookingforgroup.matches;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Match {
    private String gameName;
    private String location;
    private int maxPlayers;
    private int curPlayers;
    private Date createdDate;
    private int matchId;


    private Match(String gameName) {
        this.gameName = gameName;
    }

    private Match(String gameName, String location, int maxPlayers, int curPlayers,
                  Date createdDate, int matchId) {
        this.gameName = gameName;
        this.location = location;
        this.maxPlayers = maxPlayers;
        this.curPlayers = curPlayers;
        this.createdDate = createdDate;
        this.matchId = matchId;
    }

    public String getgameName() {
        return gameName;
    }

    private static int lastMatchId = 0;

    public static void populateMatchList(List<Map> matches) {
        ArrayList<Match> matchData = new ArrayList<Match>();

        for (Map match: matches){
            String location = (String) match.get("location");
            
        }
    }

    public static ArrayList<Match> createMatchList(int numMatches) {
        ArrayList<Match> matches = new ArrayList<Match>();

        for (int i = 1; i <= numMatches; i++) {
            matches.add(new Match("Match: " + ++lastMatchId));
        }

        return matches;
    }
}
