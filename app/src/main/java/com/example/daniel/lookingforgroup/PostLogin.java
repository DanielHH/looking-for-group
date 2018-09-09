package com.example.daniel.lookingforgroup;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONException;
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
        try {
            JSONObject data = new JSONObject(result[1]);
            String token = data.getString("token");
            String id = data.getString("id");
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("token", token);
            editor.apply();
            editor.putString("userId", id);
            editor.apply();
            String tokenDic = sp.getString("token", "");
            System.out.println("Token: " + tokenDic);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        delegate.processFinish(result);

            /* HOW TO ACCESS TOKEN (outside this class):
                SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
                String token = sp.getString("token","");
             */
    }
}
