package com.example.daniel.lookingforgroup;

import okhttp3.OkHttpClient;

public class OkSingleton extends OkHttpClient{
    private static class LazyHolder {
        private static final OkSingleton instance = new OkSingleton();
    }

    public static OkSingleton getInstance() {
        return LazyHolder.instance;
    }
}
