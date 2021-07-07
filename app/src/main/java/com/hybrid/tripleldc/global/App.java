package com.hybrid.tripleldc.global;

import android.app.Application;

public class App extends Application {
    private static final String TAG = "App";

    private static App sInstance;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
