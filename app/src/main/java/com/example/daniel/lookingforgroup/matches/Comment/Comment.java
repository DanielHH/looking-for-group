package com.example.daniel.lookingforgroup.matches.Comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Comment {
    private String commentId;
    private String text;
    private int posterId;
    private String posterName;
    private String date;

    private Comment(JSONObject commentData) {
        try {
            this.commentId = (String) commentData.getString("id");
            this.text = (String) commentData.getString("message");
            this.posterId = (Integer) commentData.getInt("author");
            this.posterName = (String) commentData.getString("name");
            this.date = (String) commentData.getString("date");
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

    public Integer getPosterId() {
        return this.posterId;
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
