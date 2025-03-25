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
    private ArrayList<String> Comments = new ArrayList<String>();

    private LocalStorage() {
        UserList = new ArrayList<>();
        MoodList = new ArrayList<>();
        Comments = new ArrayList<>();
    }
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
    public boolean checkIfUserExists(User user){
        for (int i=0;i<UserList.size();i++){
            if (UserList.get(i).getUserName().equals(user.getUserName())){
                return true;
            }
        }
        return false;
    }

    public User findUser(String uname){
        for (int i=0;i<UserList.size();i++){
            if (UserList.get(i).getUserName().equals(uname)){
                return UserList.get(i);
            }
        }
        User smn = new User();
        smn.setUserName("");
        return smn;
    }
}
