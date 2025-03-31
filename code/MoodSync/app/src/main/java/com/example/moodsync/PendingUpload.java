package com.example.moodsync;

import android.net.Uri;

/**
 * Represents a pending upload task with an image URI and a designated upload path.
 * This class is used to store information about an image that needs to be uploaded
 * to a remote server or storage location.
 */
public class PendingUpload {
    private Uri imageUri;
    private String uploadPath;

    /**
     * Constructs a new PendingUpload instance.
     *
     * @param imageUri   The URI of the image to be uploaded.
     * @param uploadPath The path where the image will be uploaded.
     */
    public PendingUpload(Uri imageUri, String uploadPath) {
        this.imageUri = imageUri;
        this.uploadPath = uploadPath;
    }

    /**
     * Gets the URI of the image to be uploaded.
     *
     * @return The URI of the image.
     */
    public Uri getImageUri() {
        return imageUri;
    }

    /**
     * Gets the upload path where the image will be stored.
     *
     * @return The upload path as a string.
     */
    public String getUploadPath() {
        return uploadPath;
    }
}
