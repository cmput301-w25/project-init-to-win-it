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

/**
 * A manager class responsible for handling image uploads to Firebase Storage.
 * This class maintains a list of pending uploads and retries failed uploads
 * periodically until they succeed.
 *
 * <p>
 * The retry mechanism ensures that uploads are attempted every second if there
 * are any pending items in the queue. It uses a callback interface to handle
 * success or failure events for individual uploads.
 * </p>
 *
 */
public class UploadManager {
    private static final List<PendingUpload> pendingUploads = new ArrayList<>();
    private static final Handler retryHandler = new Handler(Looper.getMainLooper());
    private static boolean isRetrying = false;

    /**
     * Adds a new upload task to the list of pending uploads. This method is synchronized
     * to ensure thread-safe access to the global list.
     *
     * @param imageUri   The URI of the image to be uploaded.
     * @param uploadPath The path in Firebase Storage where the image should be uploaded.
     */
    public static void addPendingUpload(Uri imageUri, String uploadPath) {
        synchronized (pendingUploads) {
            pendingUploads.add(new PendingUpload(imageUri, uploadPath));
        }
    }

    /**
     * Starts the retry cycle for pending uploads if it is not already active.
     *
     * <p>
     * This method sets the {@code isRetrying} flag to {@code true} and schedules
     * periodic retries using a handler. The retry process continues until all pending
     * uploads are successfully completed.
     * </p>
     */
    public static void startRetryCycle() {
        if (!isRetrying) {
            isRetrying = true;
            scheduleRetry();
        }
    }

    /**
     * Schedules a retry attempt for all pending uploads. This method uses a handler to post
     * a delayed task that invokes {@link #retryPendingUploads()} every second.
     */
    private static void scheduleRetry() {
        retryHandler.postDelayed(() -> {
            retryPendingUploads();
            // Reschedule the next retry
            scheduleRetry();
        }, 1000); // 1000 milliseconds = 1 second
    }

    /**
     * Attempts to upload all items in the pending uploads list. If an upload succeeds,
     * it is removed from the list; otherwise, it remains in the queue for future retries.
     *
     * <p>
     * This method iterates through the list of pending uploads and invokes {@link #attemptUpload(PendingUpload, UploadCallback)}
     * for each item. The callback handles success or failure events for individual uploads.
     * </p>
     */
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

    /**
     * Attempts to upload a single item to Firebase Storage. If the upload succeeds,
     * the download URL of the image is retrieved and passed to the success callback.
     *
     * <p>
     * In case of failure, the error is passed to the failure callback, and the item remains
     * in the pending list for future retries.
     * </p>
     *
     * @param pendingUpload The {@link PendingUpload} object containing details of the upload task.
     * @param callback      The {@link UploadCallback} interface used to handle success or failure events.
     */
    private static void attemptUpload(PendingUpload pendingUpload, UploadCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(pendingUpload.getUploadPath());
        UploadTask uploadTask = storageRef.putFile(pendingUpload.getImageUri());

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                callback.onSuccess(downloadUrl.toString());
            }).addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }

    /**
     * A simple callback interface used to handle success or failure events during an upload operation.
     */
    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }
}
