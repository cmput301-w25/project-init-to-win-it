package com.example.moodsync;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity is an abstract class that extends AppCompatActivity.
 * - Provides common functionality for managing the current activity reference within the application.
 * - Handles app restart scenarios and lifecycle events such as onCreate, onResume, onPause, and onDestroy.
 * - Ensures proper cleanup of activity references to avoid memory leaks.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register this activity with the application
        ((MyApplication) getApplication()).setCurrentActivity(this);

        // Check if the activity was started after a crash
        if (getIntent().getBooleanExtra("RESTARTED_AFTER_CRASH", false)) {
            Toast.makeText(this, "App restarted after an error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((MyApplication) getApplication()).setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        clearCurrentActivityReference();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearCurrentActivityReference();
        super.onDestroy();
    }
    /**
     * Clears the reference to the current activity in the application instance.
     * - Ensures that only this activity's reference is cleared if it matches the current activity in the application.
     */
    private void clearCurrentActivityReference() {
        Activity currentActivity = ((MyApplication) getApplication()).getCurrentActivity();
        if (currentActivity != null && currentActivity.equals(this)) {
            ((MyApplication) getApplication()).setCurrentActivity(null);
        }
    }
}
