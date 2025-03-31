package com.example.moodsync;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A custom adapter for displaying mood-related images and emojis in a ListView.
 * This adapter dynamically loads images using Glide or displays mood-based emojis
 * when an image URL is unavailable.
 *
 * <p>
 * The adapter takes a list of mood data, where each item contains an image URL and a mood string.
 * It determines whether to display an image or an emoji based on the availability of the image URL.
 * </p>
 */
public class MoodImageAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> moodList;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setMoodList(List<Map<String, Object>> moodList) {
        this.moodList = moodList;
    }

    /**
     * Constructs a new MoodImageAdapter instance.
     *
     * @param context  The application or activity context.
     * @param moodList A list of mood data, where each item contains an image URL and a mood string.
     */
    public MoodImageAdapter(Context context, List<Map<String, Object>> moodList) {
        this.context = context;
        this.moodList = moodList;
    }

    @Override
    public int getCount() {
        return moodList.size();
    }

    @Override
    public Object getItem(int position) {
        return moodList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false);
        }

        // Get views from layout
        ImageView moodImage = convertView.findViewById(R.id.photo_image); // Use the correct ID
        TextView moodEmojiTextView = convertView.findViewById(R.id.moodEmojiTextView);

        // Get data for current item
        Map<String, Object> moodData = moodList.get(position);
        String imageUrl = (String) moodData.get("imageUrl");
        String mood = (String) moodData.get("mood");

        // Check if image URL is available
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Load image using Glide
            Glide.with(context)
                    .load(imageUrl)
                    .into(moodImage);

            // Hide the emoji view
            moodEmojiTextView.setVisibility(View.GONE);
        } else {
            // Display mood emoji
            String emoji = getEmojiForMood(mood);
            moodEmojiTextView.setText(emoji);
            moodEmojiTextView.setVisibility(View.VISIBLE);

            // Hide the image view
            moodImage.setVisibility(View.GONE);
        }
        return convertView;
    }

    /**
     * Maps a given "mood" string to its corresponding emoji representation.
     *
     * <p>
     * Supported moods include:
     * <ul>
     *   <li>"happy" -> 😊</li>
     *   <li>"sad" -> 😢</li>
     *   <li>"excited" -> 😃</li>
     *   <li>"angry" -> 😠</li>
     *   <li>"confused" -> 😕</li>
     *   <li>"surprised" -> 😲</li>
     *   <li>"ashamed" -> 😳</li>
     *   <li>"scared" -> 😨</li>
     *   <li>"disgusted" -> 🤢</li>
     * </ul>
     *
     * If no matching "mood" is found, an empty string is returned.
     *
     * @param mood The "mood" string to map to an emoji.
     * @return A string containing the corresponding emoji or an empty string if no match is found.
     */
    private String getEmojiForMood(String mood) {
        switch (mood.toLowerCase()) {
            case "happy":
                return "😊";
            case "sad":
                return "😢";
            case "excited":
                return "😃";
            case "angry":
                return "😠";
            case "confused":
                return "😕";
            case "surprised":
                return "😲";
            case "ashamed":
                return "😳";
            case "scared":
                return "😨";
            case "disgusted":
                return "🤢";
            default:
                return "";
        }
    }
}