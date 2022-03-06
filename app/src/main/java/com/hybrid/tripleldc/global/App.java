package com.hybrid.tripleldc.global;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

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

        // Init Realm
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("triple_ldc.realm")
                .schemaVersion(2) // 数据库版本
                .modules(new TripleLDCModule())
                .migration(new TripleLDCMigration())
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
