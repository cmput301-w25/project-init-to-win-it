package com.example.moodsync;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.concurrent.CompletableFuture;

public class BitmapUtils {

    public static CompletableFuture<Bitmap> getBitmapFromUrl(Context context, String imageUrl) {
        CompletableFuture<Bitmap> future = new CompletableFuture<>();

        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        future.complete(resource);
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        // Optional: Handle when the image load is cleared
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        future.completeExceptionally(new Exception("Failed to load image from URL: " + imageUrl));
                    }
                });

        return future;
    }
}