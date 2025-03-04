package com.example.moodsync;

public class MoodHistoryItem {
    private String moodHeading;
    private String moodEmoji;
    private String moodDescription;

    public MoodHistoryItem(String moodHeading, String moodEmoji, String moodDescription) {
        this.moodHeading = moodHeading;
        this.moodEmoji = moodEmoji;
        this.moodDescription = moodDescription;
    }

    public String getMoodHeading() {
        return moodHeading;
    }

    public String getMoodEmoji() {
        return moodEmoji;
    }

    public String getMoodDescription() {
        return moodDescription;
    }
}
