package com.example.daniel.lookingforgroup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class EditProfileActivity extends AppCompatActivity implements AsyncResponse {

    TextView name;
    ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        name = findViewById(R.id.profileName);
        profilePicture = findViewById(R.id.profilePicture);
        String userId = getIntent().getStringExtra("EXTRA_USER_ID");
        getUserData(userId);
    }

    public void getUserData(String userId) {
        GetData getData = new GetData();
        getData.delegate = this;

        String url = "http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/user/" + userId;
        try {//execute the async task
            getData.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processFinish(String response) {
        //Handle the response.
        System.out.println(response);
        try {
            JSONObject userData = new JSONObject(response);
            String name = userData.getString("name");
            this.name.setText(name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}