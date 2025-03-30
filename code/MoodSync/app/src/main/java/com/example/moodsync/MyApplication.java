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


/**
 * Main application class that manages global application state and initialization.
 * Handles Firebase setup, user session management, and global exception handling.
 * Implements a custom uncaught exception handler for NullPointerExceptions to provide
 * graceful app recovery and restart functionality.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Firebase services initialization and configuration</li>
 *   <li>Maintaining user session state through {@link LocalStorage}</li>
 *   <li>Tracking current active Activity for error handling</li>
 *   <li>Custom crash recovery mechanism</li>
 * </ul>
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private String loggedInUsername;
    LocalStorage globalStorage;
    private static MyApplication instance;
    private Activity currentActivity;

    /**
     * Gets the singleton instance of the application class.
     *
     * @return The singleton instance of MyApplication
     */
    public static MyApplication getInstance() {
        return instance;
    }

    /**
     * Gets the currently logged-in username.
     *
     * @return String representing the logged-in username, or null if not authenticated
     */
    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    /**
     * Sets the logged-in username and updates persistent storage.
     *
     * @param username The username to set as logged-in user
     * @see LocalStorage#setCurrentUserId(String)
     */
    public void setLoggedInUsername(String username) {
        globalStorage.setCurrentUserId(username);
        this.loggedInUsername = username;
        Log.d("username", "setLoggedInUsername: " + username);
    }

    /**
     * Registers the currently active Activity for context-aware operations.
     *
     * @param activity The currently visible Activity
     */
    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }

    /**
     * Retrieves the last registered active Activity.
     *
     * @return The most recent Activity that called {@link #setCurrentActivity(Activity)}
     */
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

    /**
     * Configures a custom uncaught exception handler that specifically handles
     * NullPointerExceptions. Other exceptions are delegated to the default handler.
     *
     * @see #handleNullPointerException(Thread, Throwable)
     */
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

    /**
     * Handles NullPointerExceptions by either:
     * <ul>
     *   <li>Using {@link GlobalExceptionHandler} if an Activity is available</li>
     *   <li>Restarting the application if no Activity context exists</li>
     * </ul>
     *
     * @param thread The thread that threw the exception
     * @param throwable The NullPointerException instance
     */
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
