package com.example.moodsync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Activity activity;
    private final Thread.UncaughtExceptionHandler defaultHandler;

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
