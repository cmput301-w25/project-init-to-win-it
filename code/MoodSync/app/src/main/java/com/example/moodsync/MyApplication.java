package com.example.moodsync;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private String loggedInUsername;
    LocalStorage globalStorage;
    private static MyApplication instance;
    private Activity currentActivity;

    public static MyApplication getInstance() {
        return instance;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public void setLoggedInUsername(String username) {
        globalStorage.setCurrentUserId(username);
        this.loggedInUsername = username;
        Log.d("username", "setLoggedInUsername: " + username);
    }

    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
        globalStorage = LocalStorage.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Default: true (enabled)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        // Set up the global exception handler
        setupExceptionHandler();
    }

    private void setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof NullPointerException) {
                Log.e(TAG, "Caught NullPointerException: ", throwable);
                handleNullPointerException(thread, throwable);
            } else {
                // For other exceptions, use the default handler
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable);
            }
        });
    }

    private void handleNullPointerException(Thread thread, Throwable throwable) {
        try {
            // If we have a current activity, use the GlobalExceptionHandler
            if (currentActivity != null && !currentActivity.isFinishing()) {
                GlobalExceptionHandler handler = new GlobalExceptionHandler(currentActivity);
                handler.uncaughtException(thread, throwable);
            } else {
                // If no activity is available, restart the app
                Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("RESTARTED_AFTER_CRASH", true);
                    startActivity(intent);
                }

                // Kill the current process
                Process.killProcess(Process.myPid());
                System.exit(1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling NullPointerException", e);
            // If our handler fails, use the default handler
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable);
        }
    }
}
