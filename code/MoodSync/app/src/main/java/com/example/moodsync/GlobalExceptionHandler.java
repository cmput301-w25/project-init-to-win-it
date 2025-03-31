package com.example.moodsync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
/**
 * Custom global exception handler that gracefully handles uncaught exceptions
 * and provides user-friendly error recovery for specific exception types.
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Activity activity;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    /**
     * Constructs a GlobalExceptionHandler tied to a specific activity.
     *
     * @param activity The host activity where exceptions will be handled
     */
    public GlobalExceptionHandler(Activity activity) {
        this.activity = activity;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Check if the exception is a NullPointerException
        if (throwable instanceof NullPointerException) {
            showErrorDialog(thread, throwable);
        } else {
            // For other exceptions, use the default handler
            defaultHandler.uncaughtException(thread, throwable);
        }
    }


    /**
     * Displays a user-friendly error dialog and restarts the application.
     * <p>
     * <b>Note:</b> Runs on the UI thread using a Handler to ensure proper dialog display
     *
     * @param thread The thread where the exception occurred
     * @param throwable The exception that triggered the error
     * @throws SecurityException If the PendingIntent cannot be created
     */
    private void showErrorDialog(Thread thread, Throwable throwable) {
        try {
            // Create an intent to restart the app
            Intent intent = new Intent(activity, activity.getClass());
            intent.putExtra("RESTARTED_AFTER_CRASH", true);

            // Create a pending intent to restart the app after dialog dismissal
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    activity.getApplicationContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );

            // Show dialog on UI thread
            new Handler(Looper.getMainLooper()).post(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Unexpected Error");
                builder.setMessage("Something unexpected happened. The app needs to restart.");
                builder.setCancelable(false);
                builder.setPositiveButton("Restart", (dialog, which) -> {
                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }

                    // Kill the current process
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            });
        } catch (Exception e) {
            // If showing the dialog fails, use the default handler
            defaultHandler.uncaughtException(thread, throwable);
        }
    }
}
