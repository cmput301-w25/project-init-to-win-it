package com.example.moodsync;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class MoodEvent implements Serializable {
    // Preset fields with default values
    public String id = "123"; // Preset ID

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String mood;

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String trigger; // New trigger field
    public long date;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String description;
    public String socialSituation;
    public String location = "";
    public String photoPath = "";

    // Default constructor
    public MoodEvent() {
        this.date = System.currentTimeMillis();
    }

    // Full parameterized constructor
    public MoodEvent(String mood, String trigger, String description, String socialSituation) {
        this();
        this.mood = mood;
        this.trigger = trigger;
        this.description = description;
        this.socialSituation = socialSituation;
    }
    public long getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "MoodEvent{" +
                "id='" + id + '\'' +
                ", mood='" + mood + '\'' +
                ", trigger='" + trigger + '\'' +
                ", date=" + new Date(date) +
                ", description='" + description + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                '}';
    }
}