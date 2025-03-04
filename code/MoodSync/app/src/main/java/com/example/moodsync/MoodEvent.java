package com.example.moodsync;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class MoodEvent implements Serializable {
    // Preset fields with default values
    public String id = "123"; // Preset ID
    public String mood;
    public String trigger; // New trigger field
    public long date;
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