package com.example.moodsync;

public class MoodHistoryItem {
    private String moodHeading;
    private String moodEmoji;

    public MoodHistoryItem(String moodHeading, String moodEmoji) {
        this.moodHeading = moodHeading;
        this.moodEmoji = moodEmoji;
    }

    public String getMoodHeading() {
        return moodHeading;
    }

    public String getMoodEmoji() {
        return moodEmoji;
    }
}
