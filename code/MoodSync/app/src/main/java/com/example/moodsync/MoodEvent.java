package com.example.moodsync;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MoodEvent implements Serializable {
    private static final AtomicInteger counter = new AtomicInteger(200001);

    public final String id;
    public String mood;
    public String trigger;
    public long date;
    private String imageUrl;
    public String description;
    public String socialSituation;
    public String location;
    public String photoPath;

    public MoodEvent() {
        this.id = generateUniqueId();
        this.date = System.currentTimeMillis();
        this.location = "";
        this.photoPath = "";
    }

    public MoodEvent(String mood, String trigger, String description, String socialSituation) {
        this();
        this.mood = mood;
        this.trigger = trigger;
        this.description = description;
        this.socialSituation = socialSituation;
    }

    private String generateUniqueId() {
        int uniqueNumber = counter.getAndIncrement();
        //String timestamp = Long.toString(System.currentTimeMillis(), 36);
        return  String.format("%06d", uniqueNumber);
    }

    public long getDate() {
        return date;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "MoodEvent{" +
                "id='" + id + '\'' +
                ", mood='" + mood + '\'' +
                ", trigger='" + trigger + '\'' +
                ", date=" + new Date(date) +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                ", location='" + location + '\'' +
                ", photoPath='" + photoPath + '\'' +
                '}';
    }
}