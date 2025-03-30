package com.example.moodsync;

import java.util.Date;

/**
 * Represents a single mood history item with details such as mood, emoji, description, and date.
 * This class is used to store and manage mood-related data for historical tracking purposes.
 *
 * <p>
 * Each mood history item contains:
 * <ul>
 *   <li>An ID (optional)</li>
 *   <li>A mood string (e.g., "happy", "sad")</li>
 *   <li>An emoji representing the mood</li>
 *   <li>A description providing additional context</li>
 *   <li>A date indicating when the mood was recorded</li>
 * </ul>
 * </p>
 *
 * @see Date
 */
public class MoodHistoryItem {
    private String id;
    private String mood;
    private String emoji;
    private String description;
    private Date date;

    /**
     * Constructs a new MoodHistoryItem with the specified mood, emoji, description, and date.
     *
     * @param mood        The mood string (e.g., "happy", "sad").
     * @param emoji       The emoji representing the mood.
     * @param description A description providing additional context about the mood.
     * @param date        The date when the mood was recorded.
     */
    public MoodHistoryItem(String mood, String emoji, String description, Date date) {
        this.mood = mood;
        this.emoji = emoji;
        this.description = description;
        this.date=date;
    }

    /**
     * Gets the ID of the mood history item.
     *
     * @return The ID of the mood history item, or null if not set.
     */
    public String getId() { return id; }
    /**
     * Sets the ID of the mood history item.
     *
     * @param id The ID to set for the mood history item.
     */
    public void setId(String id) { this.id = id; }
    /**
     * Gets the mood string of this item.
     *
     * @return The mood string (e.g., "happy", "sad").
     */
    public String getMood() { return mood; }
    /**
     * Sets the mood string for this item.
     *
     * @param mood The mood string to set (e.g., "happy", "sad").
     */
    public void setMood(String mood) { this.mood = mood; }
    /**
     * Gets the emoji representing the mood of this item.
     *
     * @return The emoji string corresponding to the mood.
     */
    public String getEmoji() { return emoji; }

    /**
     * Sets the emoji representing the mood of this item.
     *
     * @param emoji The emoji string to set for the mood.
     */
    public void setEmoji(String emoji) { this.emoji = emoji; }
    /**
     * Gets the description providing additional context about this item's mood.
     *
     * @return The description of this item's mood.
     */
    public String getDescription() { return description; }

    /**
     * Sets a description providing additional context about this item's mood.
     *
     * @param description The description to set for this item's mood.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Gets the date when this item's mood was recorded.
     *
     * @return The date of this item's record.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date when this item's mood was recorded.
     *
     * @param date The date to set for this item's record.
     */
    public void setDate(Date date) {
        this.date = date;
    }
}