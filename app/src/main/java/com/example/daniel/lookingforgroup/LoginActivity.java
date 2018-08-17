package com.example.daniel.lookingforgroup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitData();
            }
        });
    }

    private String getFormattedDataString() {
        TextView tEmail = findViewById(R.id.emailLogin);
        String email = tEmail.getText().toString();
        if (!isEmailValid(email)) {
            Toast.makeText(this, "Not a valid email", Toast.LENGTH_SHORT).show();
            return "";
        }

        TextView tPassword = findViewById(R.id.passwordLogin);
        String password = tPassword.getText().toString();

        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private void submitData() {
        HTTPRequest request = HTTPRequest.getInstance();
        String jsonData = getFormattedDataString();
        request.setJson(jsonData);
        request.setUrl("http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/user/login");
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        request.setSP(sp);
        request.postLogin();
    }

    // TODO: Outsource to a utility function class or something.
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}