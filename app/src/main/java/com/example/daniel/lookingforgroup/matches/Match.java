package com.example.daniel.lookingforgroup.matches;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Match implements Parcelable {
    private String gameName;
    private String location;
    private int maxPlayers;
    private int curPlayers;
    private Date createdDate;
    private int matchId;

    /* Parcelable implementation for passing between activities */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(gameName);
        parcel.writeString(location);
        parcel.writeInt(maxPlayers);
        parcel.writeInt(curPlayers);
        parcel.writeValue(createdDate);
        parcel.writeInt(matchId); // Strange behavior when writing to parcel
    }

    public static final Parcelable.Creator<Match> CREATOR
            = new Parcelable.Creator<Match>() {
        public Match createFromParcel(Parcel in) {
            return new Match(in);
        }

        public Match[] newArray(int size) {
            return new Match[size];
        }
    };

    private Match(Parcel parcel) {
        gameName = parcel.readString();
        location = parcel.readString();
        maxPlayers = parcel.readInt();
        curPlayers = parcel.readInt();
        createdDate = (Date) parcel.readValue(ClassLoader.getSystemClassLoader());
        matchId = parcel.readInt();
    }

    private Match(JSONObject matchData) {
        this.gameName = "dummy";

        try {
            this.location = (String) matchData.get("location");
            this.maxPlayers = (Integer) matchData.get("max_players");
            this.curPlayers = (Integer) matchData.get("cur_players");
            this.createdDate = (Date) matchData.get("created_date");
            this.matchId = (Integer) matchData.get("match_id");
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public String getName() {
        return gameName;
    }

    public Integer getCurrentPlayers() {
        return curPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public String getLocation() {
        return location;
    }

    public int getMatchId() {return matchId;}

    public static ArrayList<Match> createMatchList(JSONArray matches) {
        ArrayList<Match> matchList = new ArrayList<Match>();

        for(int i = 0; i < matches.length(); ++i) {
            try {
                matchList.add(new Match(matches.getJSONObject(i)));
            } catch (JSONException e) {
                System.err.println("Error on get match object at index " + i);
                System.err.println(e.toString());
            }
        }

        return matchList;
    }
}
