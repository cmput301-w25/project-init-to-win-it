package com.example.moodsync;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class MoodEvent implements Serializable {
    private UUID id;
    private String mood;
    private Date date;
    private String description;
    private String location;
    private String socialSituation;
    private String photoPath;
    private String trigger;

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    private String intensity;


    // Default constructor
    public MoodEvent(String intensity, String description) {
        this.id = UUID.randomUUID();
        this.date = new Date();
        this.intensity = intensity;
        this.description = description;
        this.trigger = trigger;

    }

    // Parameterized constructor
    public MoodEvent(String mood, String description, String location, String socialSituation, String photoPath) {
        //this(intensity, description, trigger);
        this.mood = mood;
        this.description = description;
        this.location = location;
        this.socialSituation = socialSituation;
        this.photoPath = photoPath;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getMood() {
        return mood;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    // Setters
    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    // Additional methods
    public boolean hasPhoto() {
        return photoPath != null && !photoPath.isEmpty();
    }

    public boolean hasLocation() {
        return location != null && !location.isEmpty();
    }

    @Override
    public String toString() {
        return "MoodEvent{" +
                "id=" + id +
                ", mood='" + mood + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                ", hasPhoto=" + hasPhoto() +
                '}';
    }
}
