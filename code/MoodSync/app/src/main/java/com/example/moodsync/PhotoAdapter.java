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

/**
 * A custom adapter for displaying a list of photos in a ListView. The adapter uses Glide to load
 * images from URLs and provides functionality to save, update, and manage image URLs using
 * SharedPreferences.
 */
public class PhotoAdapter extends ArrayAdapter<String> {
    private Context context;
    private LayoutInflater inflater;
    private List<String> imageUrls;
    private static final String PREF_NAME = "PhotoAdapterPrefs";
    private static final String KEY_SAVED_IMAGES = "saved_image_urls";

    /**
     * Constructs a new PhotoAdapter.
     *
     * @param context   The context of the application or activity.
     * @param imageUrls A list of image URLs to display in the ListView.
     */
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

    /**
     * Saves the current list of image URLs to SharedPreferences. This allows the URLs
     * to persist across app sessions.
     */
    public void saveImageUrls() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Set<String> imageUrlSet = new HashSet<>(imageUrls);
        editor.putStringSet(KEY_SAVED_IMAGES, imageUrlSet);
        editor.apply();
    }
    /**
     * Updates the adapter with a new list of image URLs and saves them to SharedPreferences.
     *
     * @param newImageUrls A list of new image URLs to replace the current list.
     */
    public void updateImageUrls(List<String> newImageUrls) {
        this.imageUrls.clear();
        this.imageUrls.addAll(newImageUrls);
        notifyDataSetChanged();
        saveImageUrls();
    }

    /**
     * Adds a new image URL to the adapter's list if it is not already present. The updated
     * list is saved to SharedPreferences.
     *
     * @param imageUrl The URL of the new image to add.
     */
    public void addImageUrl(String imageUrl) {
        if (!imageUrls.contains(imageUrl)) {
            imageUrls.add(imageUrl);
            notifyDataSetChanged();
            saveImageUrls();
        }
    }

    /**
     * A helper class for managing and loading photos into a ListView. Provides utility methods
     * for loading saved photos, saving new photos, and using placeholder images when no saved
     * images are available.
     */
    public static class PhotoLoader {
        private static final String[] PLACEHOLDER_IMAGES = {
                "https://i.imgur.com/76Jfv9b.jpg",
                "https://i.imgur.com/fUX7EIB.jpg",
                "https://i.imgur.com/syELajx.jpg",
                "https://i.imgur.com/COzBnru.jpg"
        };

        /**
         * Loads photos into a ListView using either saved images from SharedPreferences or
         * placeholder images if no saved images are available.
         *
         * @param context       The context of the application or activity.
         * @param photoListView The ListView where photos will be displayed.
         */
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

        /**
         * Retrieves saved image URLs from SharedPreferences.
         *
         * @param context The context of the application or activity.
         * @return A list of saved image URLs. Returns an empty list if no saved URLs are found.
         */
        private static List<String> getSavedImageUrls(Context context) {
            SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Set<String> imageUrlSet = preferences.getStringSet(KEY_SAVED_IMAGES, new HashSet<>());
            return new ArrayList<>(imageUrlSet);
        }

        /**
         * Saves a new set of image URLs to SharedPreferences. This overwrites any previously
         * saved URLs.
         *
         * @param context   The context of the application or activity.
         * @param imageUrls A list of new image URLs to save.
         */
        public static void savePhotos(Context context, List<String> imageUrls) {
            SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            Set<String> imageUrlSet = new HashSet<>(imageUrls);
            editor.putStringSet(KEY_SAVED_IMAGES, imageUrlSet);
            editor.apply();
        }
    }
}
