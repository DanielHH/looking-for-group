package com.example.daniel.lookingforgroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Comment {
    private int commentId;
    private String text;
    private int posterId;
    private String posterName;
    private Date date;

    private Comment(JSONObject commentData) {
        try {
            this.commentId = (Integer) commentData.getInt("id");
            this.text = (String) commentData.getString("message");
            this.posterId = (Integer) commentData.getInt("author");
            this.posterName = (String) commentData.getString("name");
            this.date = (Date) commentData.get("date");
        } catch(JSONException e) {
            System.err.println(e);
        }
    }

    public String getText() {
        return text;
    }

    public String getPosterName() {
        return posterName;
    }

    public static ArrayList<Comment> createCommentList(JSONArray comments) {
        ArrayList<Comment> commentList = new ArrayList<>();

        for(int i = 0; i < comments.length(); ++i) {
            try {
                commentList.add(new Comment(comments.getJSONObject(i)));
            } catch (JSONException e) {
                System.err.println("Error on get comment object at index " + i);
                System.err.println(e.toString());
            }
        }

        return commentList;
    }
}
