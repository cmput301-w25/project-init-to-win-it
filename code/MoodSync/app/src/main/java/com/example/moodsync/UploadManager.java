package com.example.moodsync;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.moodsync.PendingUpload;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UploadManager {
    // Global storage for pending uploads
    private static final List<PendingUpload> pendingUploads = new ArrayList<>();
    private static final Handler retryHandler = new Handler(Looper.getMainLooper());
    private static boolean isRetrying = false;

    // Method to add a pending upload
    public static void addPendingUpload(Uri imageUri, String uploadPath) {
        synchronized (pendingUploads) {
            pendingUploads.add(new PendingUpload(imageUri, uploadPath));
        }
    }

    // Starts the retry cycle if not already started
    public static void startRetryCycle() {
        if (!isRetrying) {
            isRetrying = true;
            scheduleRetry();
        }
    }

    // Schedules a retry every second
    private static void scheduleRetry() {
        retryHandler.postDelayed(() -> {
            retryPendingUploads();
            // Reschedule the next retry
            scheduleRetry();
        }, 1000); // 1000 milliseconds = 1 second
    }

    // Try to upload all pending uploads
    private static void retryPendingUploads() {
        synchronized (pendingUploads) {
            Iterator<PendingUpload> iterator = pendingUploads.iterator();
            while (iterator.hasNext()) {
                PendingUpload pendingUpload = iterator.next();
                attemptUpload(pendingUpload, new UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        Log.d("UploadManager", "Retried upload successful. Image URL: " + imageUrl);
                        // Remove this upload from the pending list
                        synchronized (pendingUploads) {
                            iterator.remove();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Upload failed; it will be retried in the next cycle.
                        Log.e("UploadManager", "Retried upload failed: " + e.getMessage());
                    }
                });
            }
        }
    }

    // Attempt to upload a single pending upload
    private static void attemptUpload(PendingUpload pendingUpload, UploadCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(pendingUpload.getUploadPath());
        UploadTask uploadTask = storageRef.putFile(pendingUpload.getImageUri());

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                callback.onSuccess(downloadUrl.toString());
            }).addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }

    // A simple callback interface for upload results.
    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }
}
