package com.example.moodsync;

import java.util.ArrayList;

public class LocalStorage {
    private static LocalStorage instance;

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(String currentUserId) {
        LocalStorage.currentUserId = currentUserId;
    }

    private static String currentUserId;

    private ArrayList<User> UserList = new ArrayList<User>();
    private ArrayList<MoodEvent> MoodList = new ArrayList<>();
    private ArrayList<String> Comments = new ArrayList<String>();

    // Private constructor to prevent instantiation
    private LocalStorage() {
        UserList = new ArrayList<>();
        MoodList = new ArrayList<>();
        Comments = new ArrayList<>();
    }

    // Public method to get the singleton instance
    public static synchronized LocalStorage getInstance() {
        if (instance == null) {
            instance = new LocalStorage();
        }
        return instance;
    }

    public ArrayList<String> getComments() {
        return Comments;
    }

    public void addComment(String comment) {
        this.Comments.add(comment);
    }

    public ArrayList<MoodEvent> getMoodList() {
        return MoodList;
    }

    public void addMood(MoodEvent mood) {
        this.MoodList.add(mood);
    }

    public ArrayList<User> getUserList() {
        return UserList;
    }

    public void addUser(User user) {
        this.UserList.add(user);
    }
}
