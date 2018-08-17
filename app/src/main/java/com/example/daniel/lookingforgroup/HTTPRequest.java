package com.example.daniel.lookingforgroup;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HTTPRequest extends AppCompatActivity {
    SharedPreferences sharedpreferences;
    public static final  String token = "";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkSingleton client = OkSingleton.getInstance();

    private static class LazyHolder {
        private static final HTTPRequest instance = new HTTPRequest();
    }

    private RequestBody body = null;
    private String typeOfRequest;
    private String url;
    private String json;

    public static HTTPRequest getInstance() {
        return LazyHolder.instance;
    }

    public void setSP(SharedPreferences sp) {
        this.sharedpreferences = sp;
    }

    public void setType(String type) {
        this.typeOfRequest = type; //Make type to Enum? 'post' and 'get'.
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void postLogin () {
        if (this.json != "") {
            try {
                new HTTPRequest.SubmitLoginData().execute(this.url, this.json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class SubmitLoginData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            RequestBody body = RequestBody.create(JSON, params[1]);
            Request request = new Request.Builder()
                    .url(params[0])
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
            System.out.println("Token value: " + result);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(token, result);
            editor.apply();

            /* HOW TO ACCESS TOKEN (outside this class):
                SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
                String token = sp.getString("token","");
             */
        }
    }

    public void postData() {
        if (this.json != "") {
            try {
                new HTTPRequest.SubmitPostData().execute(this.url, this.json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class SubmitPostData extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            int result = 0;
            String token = "";
            if(sharedpreferences.contains("token")) {
                token = sharedpreferences.getString("token","");
            }
            System.out.println("Imma send this token: " + token);
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
        }
    }
}
