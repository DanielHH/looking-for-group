package com.example.daniel.lookingforgroup;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button buttonRegister = (Button)findViewById(R.id.buttonLogin);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
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

        // Example login for tests
/*
        String email = "daniel@gos.com";
        String password = "danielpw";
*/
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        //return "{'email':'" + email + "','password':'" + password + "'}";

    }


    private void submitData () {
        String json = getFormattedDataString();
        if (json != "") {
            try {
                new LoginActivity.SubmitProfileData().execute("http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/user/login", json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //TODO: Duplicated function (also in RegisterUserActivity). Export to a utility-file or something...
    OkHttpClient client = new OkHttpClient();
    private class SubmitProfileData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            RequestBody body = RequestBody.create(JSON, params[1]);
            //  System.out.println(body);
            Request request = new Request.Builder()
                    .url(params[0])
                    .post(body)
                    .build();
            // System.out.println(request);
            try (Response response = client.newCall(request).execute()) {
                result = response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result) {
            System.out.println("Token value: " + result);
            SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
            sp.edit().putString("token", result).commit();

            /* HOW TO ACCESS TOKEN:
                SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
                String token = sp.getString("token","");
             */
        }
    }

    //TODO: Duplicated function (also in RegisterUserActivity). Export to a utility-file or something...
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}