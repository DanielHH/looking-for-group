package com.example.daniel.lookingforgroup;

import android.os.AsyncTask;
import okhttp3.Request;
import okhttp3.Response;

/**
 * HOW TO USE THIS CLASS:
 * public class MyClass extends ... implements AsyncResponse {
 *     ...
 *
 *      public void myFunction {
 *        GetData getData = new GetData();
 *        getData.delegate = this;
 *        String url = "http://URL-YOU-WANT-TO-GET";
 *        try {
 *        //execute the async task
 *        getData.execute(url);
 *        } catch (Exception e) {
 *        e.printStackTrace();
 *        }
 *      }
 *
 *      @Override
 *      public void ProcessFinish(String response) {
 *          //Handle the response.
 *      }
 *
 *}
 */

public class GetData extends AsyncTask<String, Void, String> {

    public AsyncResponse delegate = null;
    OkSingleton client = OkSingleton.getInstance();

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        Request request = new Request.Builder()
                .url(params[0])
                .build();
        try (Response response = client.newCall(request).execute()) {
            result = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}
