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

public class MoodImageAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> moodList;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setMoodList(List<Map<String, Object>> moodList) {
        this.moodList = moodList;
    }

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