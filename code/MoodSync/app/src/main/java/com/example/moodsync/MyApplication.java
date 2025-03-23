package com.example.moodsync;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    private String loggedInUsername;

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}