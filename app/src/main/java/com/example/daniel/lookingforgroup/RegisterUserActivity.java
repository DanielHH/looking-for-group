package com.example.daniel.lookingforgroup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class RegisterUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button buttonRegister = (Button)findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: implement sending data to server
            }
        });
    }

    private String getFormattedDataString(){
        String email = findViewById(R.id.emailRegister).toString();
        if(!isEmailValid(email)) {
            return "";
        }

        String name = findViewById(R.id.nameRegister).toString();
        String password = findViewById(R.id.passwordRegister).toString();
        String passwordRepeat = findViewById(R.id.passwordRepeatRegister).toString();

        if (!password.equals(passwordRepeat)) {
            return "";
        }

        // TODO: format correctly for sending with used library
        return "{'email':" + email + ",name:" + name + ",password:" + password + "}";

    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
