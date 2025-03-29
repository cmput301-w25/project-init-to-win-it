package com.example.moodsync;

import android.net.Uri;

public class PendingUpload {
    private Uri imageUri;
    private String uploadPath;

    public PendingUpload(Uri imageUri, String uploadPath) {
        this.imageUri = imageUri;
        this.uploadPath = uploadPath;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getUploadPath() {
        return uploadPath;
    }
}
