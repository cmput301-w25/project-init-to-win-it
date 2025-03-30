package com.example.moodsync;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import com.example.moodsync.MoodHistoryFragment;

import java.util.Date;

/**
 * Represents a utility class for performing arithmetic operations.
 *
 * <p>This class provides methods for basic arithmetic calculations such as addition,
 * subtraction, multiplication, and division. It is intended for demonstration purposes.</p>
 *
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
    private String documentId;  // (new field) to keep track of document id (needed for comments subcollection)

    private boolean isPublic;
    private String songUrl;
    private String songTitle;


    // Constructors, getters, and setters

    public MoodEvent() {
        // Default constructor required for Firebase
    }

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
    public String getSongUrl() {
        return songUrl;
    }

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
    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

    public void setLocation(String loggedInUsername, MoodEvent moodEvent) {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            Log.e("LocationError", "No logged-in username provided.");
            return;
        }
        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch user document from Firestore
        db.collection("users") // Replace "users" with your actual collection name
                .document(loggedInUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the location field from the document
                        String location = documentSnapshot.getString("location");

                    } else {
                        Log.e("LocationError", "User document does not exist for username: " + loggedInUsername);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LocationError", "Failed to fetch user document: ", e);
                });
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    // Add getter and setter for isPublic
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    private transient Object tag; // transient means it won't be serialized



}
