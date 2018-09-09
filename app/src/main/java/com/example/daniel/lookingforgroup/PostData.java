package com.example.daniel.lookingforgroup;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostData extends AsyncTask<String, Void, String> {
    SharedPreferences sp;
    public void setSP(SharedPreferences sp) {
        this.sp = sp;
    }
    public AsyncResponse delegate = null;
    OkSingleton client = OkSingleton.getInstance();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        String token = "";
        if (sp.contains("token")) {
            token = sp.getString("token", "");
        }

        RequestBody body = RequestBody.create(JSON, params[1]);
        //  System.out.println(body);
        Request request = new Request.Builder()
                .url(params[0])
                .post(body)
                .addHeader("Authorization", token)
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
        System.out.println("Response: " + result);
        //TODO: Handle this response
        delegate.processFinish(result);
    }
}
