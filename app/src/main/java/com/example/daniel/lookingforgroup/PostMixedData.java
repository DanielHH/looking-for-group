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

public class PostMixedData extends AsyncTask<Object, Void, String> {
    SharedPreferences sp;
    public void setSP(SharedPreferences sp) {
        this.sp = sp;
    }
    public AsyncResponse delegate = null;
    OkSingleton client = OkSingleton.getInstance();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");

    @Override
    protected String doInBackground(Object... params) {
        Headers JSON_HEADER = new Headers.Builder().add("Content-Type", "application/json").build();
        String result = "";
        String token = "";
        if (sp.contains("token")) {
            token = sp.getString("token", "");
        }

        MultipartBody.Builder builderNew = new MultipartBody.Builder().setType(MultipartBody.FORM);
        int IMAGE_PARAMS = 0;
        if (params.length % 2 == 0) {
            IMAGE_PARAMS = 3;
        }
        for (int i = 1; i <= params.length-2-IMAGE_PARAMS; i = i+2) {
            builderNew.addFormDataPart((String)params[i], (String) params[i+1]);
        }
        if (IMAGE_PARAMS != 0) {
            builderNew.addFormDataPart(
                    (String) params[params.length-3],
                    (String) params[params.length-2],
                    RequestBody.create(MEDIA_TYPE_JPEG, (File) params[params.length-1])
            );
        }
        MultipartBody body = builderNew.build();
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
