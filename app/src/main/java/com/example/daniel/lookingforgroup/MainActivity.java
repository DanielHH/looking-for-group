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
public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    public void goToOpenGames(View view) {

       // Intent intent = new Intent(this, OpenGamesActivity.class);
       // startActivity(intent);
        Intent intent = new Intent(this, RegisterUserActivity.class);
        startActivity(intent);

       // new DownloadFilesTask().execute("http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/");
    }

    public void logout(View view) {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
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
