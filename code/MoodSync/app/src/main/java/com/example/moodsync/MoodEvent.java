package com.example.moodsync;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.moodsync.MoodHistoryFragment;

import java.util.Date;

/**
 * Represents a mood event, capturing details about a user's mood at a specific time.
 * This class implements Parcelable, allowing instances to be easily passed between Android components.
 */

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



    /**
     * Default constructor required for Firebase.
     */
    public MoodEvent() {
        // Default constructor required for Firebase
    }

    /**
     * Constructs a MoodEvent with specified mood, trigger, description, social situation and date.
     * @param mood The mood felt during the event (e.g., "Happy", "Sad").
     * @param trigger The trigger or cause of the mood.
     * @param description A more detailed description of the mood event.
     * @param socialSituation The social context in which the mood occurred.
     * @param date The timestamp of when the mood event occurred.
     */
    public MoodEvent(String mood, String trigger, String description, String socialSituation, Long date) {
        this.mood = mood;
        this.trigger = trigger;
        this.description = description;
        this.socialSituation = socialSituation;
        this.date=date;
    }
    /**
     * Constructs a MoodEvent from a Parcel.  Used when unmarshalling the object.
     * @param in The Parcel containing the serialized data.
     */
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

    /**
     * Creator field used for parcelable interface.
     */
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


    /**
     * Constructs a MoodEvent with specified mood, trigger, description, social situation, some object o, and date.
     * @param selectedMood The mood felt during the event (e.g., "Happy", "Sad").
     * @param trigger The trigger or cause of the mood.
     * @param moodDescription A more detailed description of the mood event.
     * @param socialSituation The social context in which the mood occurred.
     * @param o some random object.
     * @param currentTimestamp The timestamp of when the mood event occurred.
     */
    public MoodEvent(String selectedMood, String trigger, String moodDescription, String socialSituation, Object o, long currentTimestamp) {
        this.mood = mood;
        this.trigger = trigger;
        this.description = description;
        this.socialSituation = socialSituation;
        this.date=date;
    }


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

    /**
     * Gets the mood associated with this event.
     * @return The mood (e.g., "Happy", "Sad").
     */
    public String getMood() {
        return mood;
    }

    /**
     * Sets the mood associated with this event.
     * @param mood The mood to set.
     */
    public void setMood(String mood) {
        this.mood = mood;
    }

    /**
     * Gets the trigger for this mood event.
     * @return The trigger of the mood.
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * Sets the trigger for this mood event.
     * @param trigger The trigger to set.
     */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    /**
     * Gets the description for this mood event.
     * @return The description of the mood.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this mood event.
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the social situation for this mood event.
     * @return The social situation.
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * Sets the social situation for this mood event.
     * @param socialSituation The social situation to set.
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Gets the ID of this mood event.  This is typically a Firebase document ID.
     * @return The ID of the mood event.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of this mood event.
     * @param id The ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the timestamp of when this mood event occurred.
     * @return The timestamp in milliseconds.
     */
    public long getDate() {
        return date;
    }

    /**
     * Sets the timestamp of when this mood event occurred.
     * @param date The timestamp in milliseconds.
     */
    public void setDate(long date) {
        this.date = date;
    }

    /**
     * Gets the URL of an image associated with this mood event.
     * @return The image URL.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the URL of an image associated with this mood event.
     * @param imageUrl The image URL to set.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the location associated with this mood event.
     * @return The location of the mood.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location for this mood event.
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the photo path associated with this mood event.
     * @return The photoPath of the mood.
     */
    public String getPhotoPath() {
        return photoPath;
    }

    /**
     * Sets the photoPath for this mood event.
     * @param photoPath The photoPath to set.
     */
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}