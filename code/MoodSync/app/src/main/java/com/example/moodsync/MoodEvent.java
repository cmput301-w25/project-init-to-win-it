package com.example.moodsync;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.moodsync.MoodHistoryFragment;

public class MoodEvent implements Parcelable {

    private String mood;
    private String trigger;
    private long date;
    private String imageUrl;
    private String description;
    private String socialSituation;
    private String location;
    private String photoPath;
    private String id;

    // Constructors, getters, and setters

    public MoodEvent() {
        // Default constructor required for Firebase
    }

    public MoodEvent(String mood, String trigger, String description, String socialSituation) {
        this.mood = mood;
        this.trigger = trigger;
        this.description = description;
        this.socialSituation = socialSituation;
    }
    protected MoodEvent(Parcel in) {
        mood = in.readString();
        trigger = in.readString();
        date = in.readLong();
        imageUrl = in.readString();
        description = in.readString();
        socialSituation = in.readString();
        location = in.readString();
        photoPath = in.readString();
        id = in.readString();
    }

    public static final Creator<MoodEvent> CREATOR = new Creator<MoodEvent>() {
        @Override
        public MoodEvent createFromParcel(Parcel in) {
            return new MoodEvent(in);
        }

        @Override
        public MoodEvent[] newArray(int size) {
            return new MoodEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mood);
        dest.writeString(trigger);
        dest.writeLong(date);
        dest.writeString(imageUrl);
        dest.writeString(description);
        dest.writeString(socialSituation);
        dest.writeString(location);
        dest.writeString(photoPath);
        dest.writeString(id);
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
}