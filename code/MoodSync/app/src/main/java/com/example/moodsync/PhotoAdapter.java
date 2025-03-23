package com.example.moodsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhotoAdapter extends ArrayAdapter<String> {
    private Context context;
    private LayoutInflater inflater;
    private List<String> imageUrls;
    private static final String PREF_NAME = "PhotoAdapterPrefs";
    private static final String KEY_SAVED_IMAGES = "saved_image_urls";

    // Constructor
    public PhotoAdapter(Context context, List<String> imageUrls) {
        super(context, R.layout.photo_item, imageUrls);
        this.context = context;
        this.imageUrls = imageUrls;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.photo_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.photo_image);

        // Load image using Glide
        Glide.with(context)
                .load(imageUrls.get(position))
                .placeholder(R.drawable.arijitsingh)
                .centerCrop()
                .into(imageView);

        return convertView;
    }

    // Save current image URLs to SharedPreferences
    public void saveImageUrls() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Set<String> imageUrlSet = new HashSet<>(imageUrls);
        editor.putStringSet(KEY_SAVED_IMAGES, imageUrlSet);
        editor.apply();
    }

    // Update the adapter with new image URLs
    public void updateImageUrls(List<String> newImageUrls) {
        this.imageUrls.clear();
        this.imageUrls.addAll(newImageUrls);
        notifyDataSetChanged();
        saveImageUrls();
    }

    // Add a new image URL to the list
    public void addImageUrl(String imageUrl) {
        if (!imageUrls.contains(imageUrl)) {
            imageUrls.add(imageUrl);
            notifyDataSetChanged();
            saveImageUrls();
        }
    }

    // Helper class to load photos into ListView
    public static class PhotoLoader {
        private static final String[] PLACEHOLDER_IMAGES = {
                "https://i.imgur.com/76Jfv9b.jpg",
                "https://i.imgur.com/fUX7EIB.jpg",
                "https://i.imgur.com/syELajx.jpg",
                "https://i.imgur.com/COzBnru.jpg"
        };

        public static void loadPhotos(Context context, ListView photoListView) {
            // Try to load saved images first
            List<String> imageList = getSavedImageUrls(context);

            // If no saved images, use placeholders
            if (imageList.isEmpty()) {
                imageList = Arrays.asList(PLACEHOLDER_IMAGES);
            }

            PhotoAdapter adapter = new PhotoAdapter(context, new ArrayList<>(imageList));
            photoListView.setAdapter(adapter);
        }

        // Get saved image URLs from SharedPreferences
        private static List<String> getSavedImageUrls(Context context) {
            SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Set<String> imageUrlSet = preferences.getStringSet(KEY_SAVED_IMAGES, new HashSet<>());
            return new ArrayList<>(imageUrlSet);
        }

        // Save a new set of images
        public static void savePhotos(Context context, List<String> imageUrls) {
            SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            Set<String> imageUrlSet = new HashSet<>(imageUrls);
            editor.putStringSet(KEY_SAVED_IMAGES, imageUrlSet);
            editor.apply();
        }
    }
}
