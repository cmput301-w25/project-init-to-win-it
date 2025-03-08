package com.example.moodsync;

import java.util.Date;

/**
 * Represents an item in the mood history.
 */
public class MoodHistoryItem {

    private String id; // Firestore document ID
    private String mood;
    private String emoji;
    private String description;
    private Date date;


    /**
     * Constructor for a MoodHistoryItem.
     * @param mood The mood.
     * @param emoji The emoji associated with the mood.
     * @param description A description of the mood.
     * @param date The date of the mood event.
     */
    public MoodHistoryItem(String mood, String emoji, String description, Date date) {
        this.mood = mood;
        this.emoji = emoji;
        this.description = description;
        this.date=date;
    }


    /**
     * Gets the Firestore document ID.
     * @return The document ID.
     */
    public String getId() { return id; }

    /**
     * Sets the Firestore document ID.
     * @param id The document ID to set.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Gets the mood.
     * @return The mood.
     */
    public String getMood() { return mood; }

    /**
     * Sets the mood.
     * @param mood The mood to set.
     */
    public void setMood(String mood) { this.mood = mood; }

    /**
     * Gets the emoji.
     * @return The emoji.
     */
    public String getEmoji() { return emoji; }

    /**
     * Sets the emoji.
     * @param emoji The emoji to set.
     */
    public void setEmoji(String emoji) { this.emoji = emoji; }

    /**
     * Gets the description.
     * @return The description.
     */
    public String getDescription() { return description; }

    /**
     * Sets the description.
     * @param description The description to set.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Gets the date.
     * @return The date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date.
     * @param date The date to set.
     */
    public void setDate(Date date) {
        this.date = date;
    }

}
