package com.example.daniel.lookingforgroup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daniel.lookingforgroup.matches.OpenGamesActivity;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String token = sp.getString("token", "");
        if(sp.contains("token") && (token.length())>30) {
            setContentView(R.layout.activity_main);
        }
        else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /** Called when the user taps the "Create Game"-button */
    public void createGame(View view) {
        Intent intent = new Intent(this, CreateGameActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String gameName = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, gameName);
        startActivity(intent);
    }

    public void goToMyProfile(View view) {
        Intent intent = new Intent(this, EditProfileActivity.class);
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String id = sp.getString("userId","");
        intent.putExtra("EXTRA_USER_ID", id);
        startActivity(intent);
    }

    public void goToOpenGames(View view) {
        Intent intent = new Intent(this, OpenGamesActivity.class);
        startActivity(intent);
       // new DownloadFilesTask().execute("http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/");
    }

    public void logout(View view) {
        PostData postData = new PostData();
        postData.delegate = this;
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        postData.setSP(sp);
        String url = "http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/user/logout";

        try {
            //execute the async task
            postData.execute(url, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processFinish(String response){
        //TODO: Handle different responses
        if(response.equals("200")) {
            Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
            SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
            sp.edit().remove("token").apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else if(response.equals("401")) {
            Toast.makeText(this, "Wrong token. Can't logout! lol", Toast.LENGTH_SHORT).show();
            SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
            sp.edit().remove("token").apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
