package com.example.moodsync;

public class MoodHistoryItem {

    private String id; // Firestore document ID
    private String mood;
    private String emoji;
    private String description;

    // Constructor
    public MoodHistoryItem(String mood, String emoji, String description) {
        this.mood = mood;
        this.emoji = emoji;
        this.description = description;
    }


    // Getters and setters for all fields including id
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
