package com.example.moodsync;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * {@code MoodEvent} represents a user's mood at a specific time, including details
 * such as the mood itself, the trigger for the mood, a description, the social situation,
 * the date and time the mood was recorded, an image URL, privacy settings, associated song,
 * and location.
 *
 * <p>This class implements the {@link Parcelable} interface to allow {@code MoodEvent} objects
 * to be passed between different components of an Android application, such as Activities
 * or Fragments, via an {@link android.content.Intent} or {@link android.os.Bundle}.</p>
 * */

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
    private String documentId;  // (new field) to keep track of document id (needed for comments subcollection)

    private boolean isPublic;
    private String songUrl;
    private String songTitle;

    /**
     * Default constructor required for Firebase Firestore. It is necessary for Firebase
     * to be able to deserialize objects from the database.
     */
    public MoodEvent() {
        // Default constructor required for Firebase
    }

    /**
     * Constructs a new {@code MoodEvent} with the specified details.
     *
     * @param mood The mood of the event (e.g., "happy", "sad").
     * @param trigger The trigger that caused the mood.
     * @param description A detailed description of the mood event.
     * @param socialSituation The social situation in which the mood occurred.
     * @param date The date and time the mood event occurred, represented as milliseconds since the epoch.
     * @param imageUrl The URL of an image associated with the mood event.
     * @param isPublic A boolean indicating whether the mood event is public or private.
     * @param id The unique identifier of the user who created the mood event.
     * @param songUrl The URL of a song associated with the mood event.
     * @param songTitle The title of the song associated with the mood event.
     * @param currentLocation The location where the mood event occurred.
     */

    public MoodEvent(String mood, String trigger, String description, String socialSituation, long date, String imageUrl, boolean isPublic, String id, String songUrl, String songTitle, String currentLocation) {
        this.mood = mood;
        this.trigger = trigger;
        this.description = description;
        this.socialSituation = socialSituation;
        this.date=date;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
        this.id = id;
        this.songUrl = songUrl;
        this.songTitle = songTitle;
        this.location = currentLocation;
    }
    /**
     * Constructs a {@code MoodEvent} object from a {@link Parcel}. This constructor is used
     * when deserializing the object from a Parcel.
     *
     * @param in The Parcel to read the object's data from.
     **/
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
        isPublic = in.readByte() != 0;
        songUrl = in.readString();
        songTitle = in.readString();
    }

    /**
     * Gets the URL of the song associated with the mood event.
     *
     * @return The URL of the song.
     */
    public String getSongUrl() {
        return songUrl;
    }

    /**
     * Sets the URL of the song associated with the mood event.
     *
     * @param songUrl The URL of the song.
     */
    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
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

    /**
     * Constructs a new {@code MoodEvent} with the specified details. This constructor is used when creating
     * a MoodEvent with a String for ImageUrl.
     *
     * @param selectedMood The mood of the event (e.g., "happy", "sad").
     * @param trigger The trigger that caused the mood.
     * @param moodDescription A detailed description of the mood event.
     * @param socialSituation The social situation in which the mood occurred.
     * @param currentTimestamp The date and time the mood event occurred, represented as milliseconds since the epoch.
     * @param imageeUrl The URL of an image associated with the mood event.
     * @param songUrl The URL of a song associated with the mood event.
     * @param songTitle The title of the song associated with the mood event.
     * @param currentLocation The location where the mood event occurred.
     */
    public MoodEvent(String selectedMood, String trigger, String moodDescription, String socialSituation, long currentTimestamp,String imageeUrl , String songUrl, String songTitle , String currentLocation) {
        this.mood = selectedMood;
        this.trigger = trigger;
        this.description = moodDescription;
        this.socialSituation = socialSituation;
        this.date=currentTimestamp;
        this.imageUrl = imageeUrl;
        this.songUrl = songUrl;
        this.songTitle = songTitle;
        this.location = currentLocation;
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
        dest.writeByte((byte) (isPublic ? 1 : 0));
        dest.writeString(songTitle);
        dest.writeString(songUrl);
    }
    /**
     * Gets the title of the song associated with the mood event.
     *
     * @return The title of the song.
     */
    public String getSongTitle() {
        return songTitle;
    }

    /**
     * Sets the title of the song associated with the mood event.
     *
     * @param songTitle The title of the song.
     */
    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    /**
     * Gets the document ID of the mood event in Firebase Firestore.
     *
     * @return The document ID.
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the document ID of the mood event in Firebase Firestore.
     *
     * @param documentId The document ID.
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the mood of the event.
     *
     * @return The mood.
     */
    public String getMood() {
        return mood;
    }

    /**
     * Sets the mood of the event.
     *
     * @param mood The mood.
     */
    public void setMood(String mood) {
        this.mood = mood;
    }

    /**
     * Gets the trigger that caused the mood.
     *
     * @return The trigger.
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * Sets the trigger that caused the mood.
     *
     * @param trigger The trigger.
     */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    /**
     * Gets the description of the mood event.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the mood event.
     *
     * @param description The description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the social situation in which the mood occurred.
     *
     * @return The social situation.
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * Sets the social situation in which the mood occurred.
     *
     * @param socialSituation The social situation.
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Gets the ID of the user who created the mood event.
     *
     * @return The user ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the user who created the mood event.
     *
     * @param id The user ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the date and time the mood event occurred, represented as milliseconds since the epoch.
     *
     * @return The date and time.
     */
    public long getDate() {
        return date;
    }

    /**
     * Sets the date and time the mood event occurred, represented as milliseconds since the epoch.
     *
     * @param date The date and time.
     */
    public void setDate(long date) {
        this.date = date;
    }

    /**
     * Gets the URL of the image associated with the mood event.
     *
     * @return The image URL.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the URL of the image associated with the mood event.
     *
     * @param imageUrl The image URL.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the location where the mood event occurred.
     *
     * @return The location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location where the mood event occurred.
     *
     * @param location The location.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the path to the photo associated with the mood event.
     *
     * @return The photo path.
     */
    public String getPhotoPath() {
        return photoPath;
    }

    /**
     * Sets the path to the photo associated with the mood event.
     *
     * @param photoPath The photo path.
     */
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    /**
     * Indicates whether the mood event is public or private.
     *
     * @return {@code true} if the mood event is public; {@code false} if it is private.
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Sets whether the mood event is public or private.
     *
     * @param isPublic {@code true} if the mood event is public; {@code false} if it is private.
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    private transient Object tag; // transient means it won't be serialized

}
