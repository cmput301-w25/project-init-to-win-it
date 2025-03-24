package com.example.moodsync;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    private String loggedInUsername;
    LocalStorage globalStorage = LocalStorage.getInstance();

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public void setLoggedInUsername(String username) {
        globalStorage.setCurrentUserId(username);
        this.loggedInUsername = username;
        Log.d("username", "setLoggedInUsername: " + username);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}