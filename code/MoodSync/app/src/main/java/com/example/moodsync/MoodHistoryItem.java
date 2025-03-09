package com.example.moodsync;

import java.util.Date;

public class MoodHistoryItem {
    private String id;
    private String mood;
    private String emoji;
    private String description;
    private Date date;
    public MoodHistoryItem(String mood, String emoji, String description, Date date) {
        this.mood = mood;
        this.emoji = emoji;
        this.description = description;
        this.date=date;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
}