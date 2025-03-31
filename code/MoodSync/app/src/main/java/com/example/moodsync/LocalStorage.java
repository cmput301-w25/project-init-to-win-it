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

    private String currentUserId;

    public long getCurrentMoodForEdit() {
        return currentMoodForEdit;
    }

    public void setCurrentMoodForEdit(long currentMoodForEdit) {
        LocalStorage.currentMoodForEdit = currentMoodForEdit;
    }

    private static long currentMoodForEdit;

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
    public void updateMood(long time, MoodEvent mood){
        // Update data of the specific mood
        for (int i=0;i<MoodList.size();i++){
            if (MoodList.get(i).getDate() == time){
                MoodList.set(i,mood);
            }
        }
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
        this.currentUserId = currentUserId;
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
    public void updatePrivMood(MoodEvent mood){
        for (int i=0; i<PrivList.size();i++){
            if (mood.getDate() == PrivList.get(i).getDate()){
                PrivList.set(i,mood);
                break;
            }
        }
        PrivList.add(mood);
    }
    public void deletePrivDups() {
        for (int i = 0; i < PrivList.size(); i++) {
            for (int j = i + 1; j < PrivList.size(); ) {
                if (PrivList.get(i).getDate() == (PrivList.get(j).getDate())) {
                    PrivList.remove(j);
                    // Don't increment j here because removal shifts elements
                } else {
                    j++;
                }
            }
        }
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
    public void removeMood(long millis){
        for (int i=0; i<MoodList.size();i++) {
            if (MoodList.get(i).getDate() == (millis)){
                MoodList.remove(i);
                break;
            }
        }
    }

    public MoodEvent getMoodEvent(long millis){
        ArrayList <MoodEvent> temp = new ArrayList<MoodEvent>();
        temp.addAll(MoodList);
        temp.addAll(PrivList);
        for (int i=0; i<temp.size();i++) {
            if (temp.get(i).getDate() == (millis)) {
                return temp.get(i);
            }
        }
        return null;
    }
    public boolean checkMoodInPriv(MoodEvent mood){
        boolean flag= false;
        for (int i=0;i<PrivList.size();i++){
            if(PrivList.get(i).getDate() == mood.getDate()){
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    public void refreshPrivList(){
        for (int i=0;i<PrivList.size();i++){
            if(PrivList.get(i).isPublic()) {
                MoodList.add(PrivList.get(i));
                PrivList.remove(i);
            }
        }
        for (int i=0;i<PrivList.size();i++){
            for (int j=0;j<PrivList.size();j++){
                if (PrivList.get(i).getDate() == PrivList.get(j).getDate() ){
                    PrivList.remove(j);
                }
            }
        }
        refreshPubList();
    }
    public void refreshPubList(){
        for (int i=0;i<MoodList.size();i++){
            if(!MoodList.get(i).isPublic()) {
                PrivList.add(MoodList.get(i));
                MoodList.remove(i);
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