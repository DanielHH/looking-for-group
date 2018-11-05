package com.example.daniel.lookingforgroup;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.File;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostData extends AsyncTask<Object, Void, String> {
    SharedPreferences sp;
    public void setSP(SharedPreferences sp) {
        this.sp = sp;
    }
    public AsyncResponse delegate = null;
    OkSingleton client = OkSingleton.getInstance();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected String doInBackground(Object... params) {
        Headers JSON_HEADER = new Headers.Builder().add("Content-Type", "application/json").build();
        String result = "";
        String token = "";
        if (sp.contains("token")) {
            token = sp.getString("token", "");
        }
        RequestBody body;
        body = RequestBody.create(JSON, (String) params[1]); //THIS CODE IS WORKING WHEN THERE ARE NO IMAGES.
        Request request = new Request.Builder()
                .url((String)params[0])
                .post(body)
                .addHeader("Authorization", token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            result = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void onPostExecute(String result) {
        System.out.println("Response: " + result);
        //TODO: Handle this response
        delegate.processFinish(result);
    }
}
