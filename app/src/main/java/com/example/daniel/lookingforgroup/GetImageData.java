package com.example.daniel.lookingforgroup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;

public class GetImageData extends AsyncTask<String, Void, Bitmap> {

    public AsyncImageResponse delegate = null;
    OkSingleton client = OkSingleton.getInstance();

    @Override
    protected Bitmap doInBackground(String... params) {
        InputStream is = null;
        Bitmap image = null;
        Request request = new Request.Builder()
                .url(params[0])
                .build();
        try (Response response = client.newCall(request).execute()) {
            is = response.body().byteStream();
            image = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    protected void onPostExecute(Bitmap result) {
        delegate.processFinish(result);
    }
}
