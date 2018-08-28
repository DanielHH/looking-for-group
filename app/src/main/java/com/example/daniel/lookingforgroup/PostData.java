package com.example.daniel.lookingforgroup;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostData extends AsyncTask<String, Void, Integer> {
    SharedPreferences sp;
    public void setSP(SharedPreferences sp) {
        this.sp = sp;
    }
    public AsyncResponse delegate = null;
    OkSingleton client = OkSingleton.getInstance();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected Integer doInBackground(String... params) {
        int result = 0;
        String token = "";
        token = sp.getString("token", "");
        if (sp.contains("token") && (token.length())>30) {
            System.out.println("Imma send this token: " + token.substring(11, 124));
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
            result = response.code();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void onPostExecute(Integer result) {
        System.out.println("Response: " + result);
        //TODO: Handle this response
        delegate.processFinish(result);
    }
}
