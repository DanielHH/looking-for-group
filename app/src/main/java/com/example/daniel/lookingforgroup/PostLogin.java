package com.example.daniel.lookingforgroup;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostLogin extends AsyncTask<String, Void, String[]> {
    SharedPreferences sp;
    public void setSP(SharedPreferences sp) {
        this.sp = sp;
    }
    public LoginResponse delegate = null;
    OkSingleton client = OkSingleton.getInstance();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected String[] doInBackground(String... params) {
        String[] result = new String[2];
        RequestBody body = RequestBody.create(JSON, params[1]);
        Request request = new Request.Builder()
                .url(params[0])
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            result[0] = Integer.toString(response.code());
            result[1] = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void onPostExecute(String[] result) {
        System.out.println("Token value: " + result[1]);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", result[1]);
        editor.apply();
        delegate.processFinish(result);

            /* HOW TO ACCESS TOKEN (outside this class):
                SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
                String token = sp.getString("token","");
             */
    }
}
