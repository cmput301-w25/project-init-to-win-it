package com.example.moodsync;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class LocalStorage {
    private static LocalStorage instance;

    public String getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(String searchResult) {
        this.searchResult = searchResult;
    }

    private String searchResult;

    private static String currentUserId;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<User> UserList = new ArrayList<User>();
    private ArrayList<MoodEvent> MoodList = new ArrayList<>();
    private ArrayList<Comment> Comments = new ArrayList<Comment>();

    private LocalStorage() {
        UserList = new ArrayList<>();
        MoodList = new ArrayList<>();
        Comments = new ArrayList<>();
    }
    public void clearMoods(){
        MoodList.clear();
    }
    public void updateMood(String user) {
        db.collection("mood_events")
                .whereEqualTo("id", user)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent updatedMood = document.toObject(MoodEvent.class);
                            for (int i = 0; i < MoodList.size(); i++) {
                                if (MoodList.get(i).getId().equals(user)) {
                                    MoodList.set(i, updatedMood);
                                    break;
                                }
                            }
                        }
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    //Updates the UserList with the latest information from the database (only for  "username")
    public void updateUser(String username) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User updatedUser = document.toObject(User.class);
                            for (int i = 0; i < UserList.size(); i++) {
                                if (UserList.get(i).getUsername().equals(username)) {
                                    UserList.set(i, updatedUser);
                                    break;
                                }
                            }
                        }
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }


    public  String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        LocalStorage.currentUserId = currentUserId;
    }

    // Public method to get the singleton instance
    public static synchronized LocalStorage getInstance() {
        if (instance == null) {
            instance = new LocalStorage();
        }
        return instance;
    }
    public User getCurrentUser(){
        return findUser(currentUserId);
    }

    public ArrayList<Comment> getComments() {
        return Comments;
    }

    public void addComment(Comment comment) {
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
    public boolean checkIfUserExists(User user){
        for (int i=0;i<UserList.size();i++){
            if (UserList.get(i).getUsername().equals(user.getUsername())){
                return true;
            }
        }
        return false;
    }

    public User findUser(String uname){
        for (int i=0;i<UserList.size();i++){
            if (UserList.get(i).getUsername().equals(uname)){
                return UserList.get(i);
            }
        }
        User smn = new User();
        smn.setUsername("");
        return smn;
    }

    public void insertMood(MoodEvent moodEvent) {
        int flag =0;
        for (int i=0; i<MoodList.size();i++) {
            if (moodEvent.getId().equals(MoodList.get(i).getId())){
                flag = 1;   // Found
                break;
            }
        }
        if (flag == 0){
            MoodList.add(moodEvent);
        }
    }
    public User getUserFromUName(String uName) {
        User use = null;
        for (int i = 0; i < UserList.size(); i++) {
            if (UserList.get(i).getUsername().equals(uName)) {
                return UserList.get(i);
            }
        }
        return use;
    }

}
