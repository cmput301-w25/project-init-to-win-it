package com.example.moodsync;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
/**
 * Utility class for performing operations related to Bitmaps.
 * - Provides methods for loading, blurring, and compressing images.
 * - Uses libraries like Glide and RenderScript for efficient image processing.
 */
public class BitmapUtils {
    /**
     * Asynchronously fetches a Bitmap from a given image URL using Glide.
     * - Returns a CompletableFuture that resolves with the loaded Bitmap or fails with an exception.
     *
     * @param context  The context for Glide to load the image.
     * @param imageUrl The URL of the image to load as a Bitmap.
     * @return A CompletableFuture containing the loaded Bitmap or an exception if loading fails.
     */
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
    /**
     * Applies a Gaussian blur effect to a given Bitmap using RenderScript.
     * - Downscales the image for faster processing and applies the blur effect.
     *
     * @param context The context required for initializing RenderScript.
     * @param image   The original Bitmap to be blurred.
     * @return A new Bitmap with the blur effect applied.
     */
    public static Bitmap blurBitmap(Context context, Bitmap image) {
        // Adjust the scale and blur radius as needed.
        final float BITMAP_SCALE = 0.5f; // Downscale for faster processing and more blur.
        final float BLUR_RADIUS = 10.0f; // Maximum is around 25f.

        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        intrinsicBlur.setRadius(BLUR_RADIUS);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        rs.destroy();
        return outputBitmap;
    }

/**
 * Compresses an image from a given URI and saves it as a temporary file in the cache directory.
 * - Optionally applies a blur effect to reduce details before compression.
 * - Adjusts compression quality dynamically to ensure the file size is below a specified limit.
 *
 * @param context  The context required for accessing content resolver and cache directory.
 * @param photoUri The URI of the image to compress.
 * @return A File object representing the compressed image, or null if compressio
 * */

    public static File compressImageFromUri(Context context, Uri photoUri) {
        try {
            // Convert Uri to Bitmap
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);

            // Apply blur to reduce details (you can adjust parameters for desired blur strength)
            Bitmap blurredBitmap = blurBitmap(context, originalBitmap);

            // Create a temporary file
            File compressedFile = new File(context.getCacheDir(), "compressed_image.jpg");
            FileOutputStream outputStream;

            int quality = 100;
            int maxSizeKB = 64;
            int flag = 1;
            ByteArrayOutputStream byteArrayOutputStream;

            do {
                // Reset the stream
                byteArrayOutputStream = new ByteArrayOutputStream();

                // Compress the blurred bitmap instead of the original
                blurredBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);

                // Reduce quality if file size exceeds limit
                quality -= 5;
                // Stop if quality goes too low
                if (quality <= 5) {
                    quality = 1;
                    flag = 0;
                }
            } while ((byteArrayOutputStream.toByteArray().length / 1024 > maxSizeKB) && flag == 1);

            // Write final compressed data to file
            outputStream = new FileOutputStream(compressedFile);
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();
            outputStream.close();

            return compressedFile; // Return the final compressed image file

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}