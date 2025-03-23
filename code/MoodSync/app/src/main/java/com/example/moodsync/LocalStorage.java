package com.example.moodsync;

import com.example.moodsync;

import java.util.ArrayList;

public class LocalStorage {
    public ArrayList <User> UserList;
    public ArrayList <MoodEvent> MoodList;

    public ArrayList<String> getComments() {
        return Comments;
    }

    public void addComments(String comments) {
        this.Comments.add(comments);
    }

    public ArrayList<MoodEvent> getMoodList() {
        return MoodList;
    }

    public void addMoodList(MoodEvent mood) {
            this.MoodList.add(mood);
    }

    public ArrayList<String> getUserList() {
        return UserList;
    }

    public void addUserList(String user) {
        this.UserList.add(user);
    }

    public ArrayList <String> Comments;
    
    
    
    
}
