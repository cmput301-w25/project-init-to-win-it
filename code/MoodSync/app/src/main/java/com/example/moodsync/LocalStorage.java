package com.example.moodsync;

import android.util.Log;
import android.view.ContextMenu;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class LocalStorage {
    private static LocalStorage instance;

    public String getPfpUrl() {
        return pfpUrl;
    }

    public void setPfpUrl(String pfpUrl) {
        LocalStorage.pfpUrl = pfpUrl;
    }

    private static String pfpUrl;

    public String getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(String searchResult) {
        this.searchResult = searchResult;
    }

    private String searchResult;

    private static String currentUserId;

    public static String getCurrentMoodForEdit() {
        return currentMoodForEdit;
    }

    public static void setCurrentMoodForEdit(String currentMoodForEdit) {
        LocalStorage.currentMoodForEdit = currentMoodForEdit;
    }

    private static String currentMoodForEdit;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<User> UserList = new ArrayList<User>();
    private ArrayList<MoodEvent> MoodList = new ArrayList<>();
    private ArrayList<Comment> Comments = new ArrayList<Comment>();

    public ArrayList<MoodHistoryItem> getMHItem() {
        return MHItem;
    }

    public void setMHItem(ArrayList<MoodHistoryItem> MHItem) {
        this.MHItem = MHItem;
    }

    private ArrayList<MoodHistoryItem> MHItem = new ArrayList<MoodHistoryItem>();


    public ArrayList<MoodEvent> getPrivList() {
        return PrivList;
    }

    public void setPrivList(ArrayList<MoodEvent> privList) {
        PrivList = privList;
    }

    private ArrayList<MoodEvent> PrivList = new ArrayList<MoodEvent>();

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

    public void insertComment(Comment comment){
        if (!Comments.isEmpty()) {
            int flag = 0;
            for (int i = 0; i < Comments.size(); i++) {
                if (Comments.get(i).getCommentId().equals(comment.getCommentId())) {
                    flag = 1;   // Found
                    break;
                }
            }
            if (flag == 0) {
                Comments.add(comment);
            }
        }
        }
    public void insertMood(MoodEvent moodEvent) {
        int flag =0;
        if (!MoodList.isEmpty()){
        for (int i=0; i<MoodList.size();i++) {
            if (moodEvent.getDate() == MoodList.get(i).getDate()) {
                flag = 1;   // Found
                break;
            }
        }
        }
        if (flag == 0){
            Log.d("Insert mood", "insertMood: NEW MOOD"+moodEvent.getId());
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
    public void removeMood(String documentID){
        for (int i=0; i<MoodList.size();i++) {
            if (MoodList.get(i).getDocumentId().equals(documentID)){
                MoodList.remove(i);
                break;
            }
        }
    }

    public ArrayList<MoodEvent>  getMoodsForCurrentUser(User user,boolean isPublic){
        ArrayList <MoodEvent> temp = new ArrayList<MoodEvent>();
        for (int i = 0; i < MoodList.size(); i++) {
            if (MoodList.get(i).getId().equals(user.getId()) && MoodList.get(i).isPublic() == isPublic) {
                temp.add(MoodList.get(i));
            }
        }
        return temp;
    }

}
