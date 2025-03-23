package com.example.moodsync;

public interface OnImageUploadedListener {
    void onImageUploaded(String imageUrl);
    void onUploadFailed(Exception e);
}
