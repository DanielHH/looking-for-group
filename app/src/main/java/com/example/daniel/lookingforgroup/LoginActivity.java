package com.example.daniel.lookingforgroup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity implements LoginResponse {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button buttonLogin = (Button) findViewById(R.id.btn_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //newSubmitData();
                testLogin();
            }
        });
    }

    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterUserActivity.class);
        startActivity(intent);
    }

    private void testLogin() {
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", "asdfasdfasdfasdfasdfasdfasdfasdf");
        editor.apply(); // TODO: remove this line.
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void newSubmitData() {
        PostLogin postLogin = new PostLogin();
        postLogin.delegate = this;
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        postLogin.setSP(sp);
        String url = "http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/user/login";
        String jsonData = getFormattedDataString();

        try {
            //execute the async task
            postLogin.execute(url, jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processFinish(String[] response){
        if(response[0].equals("200")) {
            Toast.makeText(this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else if(response[0].equals("400")) {
            Toast.makeText(this, response[1], Toast.LENGTH_SHORT).show();
        }
    }

    private String getFormattedDataString() {
        TextView tEmail = findViewById(R.id.edit_email_login);
        String email = tEmail.getText().toString();
        if (!isEmailValid(email)) {
            Toast.makeText(this, "Not a valid email", Toast.LENGTH_SHORT).show();
            return "";
        }
        TextView tPassword = findViewById(R.id.edit_password_login);
        String password = tPassword.getText().toString();
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    // TODO: Outsource to a utility function class or something.
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}