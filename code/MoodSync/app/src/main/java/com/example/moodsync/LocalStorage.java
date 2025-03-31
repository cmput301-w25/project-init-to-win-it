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

    /**
     * Returns the profile picture URL.
     *
     * @return A string representing the profile picture URL.
     */
    public String getPfpUrl() {
        return pfpUrl;
    }

    /**
     * Sets the profile picture URL.
     *
     * @param pfpUrl The profile picture URL to set.
     */
    public void setPfpUrl(String pfpUrl) {
        LocalStorage.pfpUrl = pfpUrl;
    }

    private static String pfpUrl;

    /**
     * Returns the current search result.
     *
     * @return A string representing the current search result.
     */
    public String getSearchResult() {
        return searchResult;
    }

    /**
     * Sets the current search result.
     *
     * @param searchResult The search result to set.
     */
    public void setSearchResult(String searchResult) {
        this.searchResult = searchResult;
    }

    private String searchResult;

    private String currentUserId;


    /**
     * Returns the current mood ID for editing.
     *
     * @return A long representing the current mood ID for editing.
     */
    public long getCurrentMoodForEdit() {
        return currentMoodForEdit;
    }

    /**
     * Sets the current mood ID for editing.
     *
     * @param currentMoodForEdit The mood ID to set for editing.
     */
    public void setCurrentMoodForEdit(long currentMoodForEdit) {
        LocalStorage.currentMoodForEdit = currentMoodForEdit;
    }

    private static long currentMoodForEdit;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<User> UserList = new ArrayList<User>();
    private ArrayList<MoodEvent> MoodList = new ArrayList<>();
    private ArrayList<Comment> Comments = new ArrayList<Comment>();

    /**
     * Retrieves the list of mood history items stored locally.
     *
     * @return An ArrayList of MoodHistoryItem objects representing the mood history items.
     */
    public ArrayList<MoodHistoryItem> getMHItem() {
        return MHItem;
    }


    /**
     * Sets the mood history items list with a new ArrayList of MoodHistoryItem objects.
     *
     * @param MHItem The ArrayList of MoodHistoryItem objects to set as the mood history items list.
     */
    public void setMHItem(ArrayList<MoodHistoryItem> MHItem) {
        this.MHItem = MHItem;
    }

    private ArrayList<MoodHistoryItem> MHItem = new ArrayList<MoodHistoryItem>();

    /**
     * Retrieves the private mood list stored locally.
     *
     * @return An ArrayList of MoodEvent objects representing the private mood list.
     */
    public ArrayList<MoodEvent> getPrivList() {
        return PrivList;
    }

    /**
     * Sets the private mood list with a new list of MoodEvent objects.
     *
     * @param privList The ArrayList of MoodEvent objects to set as the private mood list.
     */
    public void setPrivList(ArrayList<MoodEvent> privList) {
        PrivList = privList;
    }

    private ArrayList<MoodEvent> PrivList = new ArrayList<MoodEvent>();

    private LocalStorage() {
        UserList = new ArrayList<>();
        MoodList = new ArrayList<>();
        Comments = new ArrayList<>();
    }

    /**
     * Clears all moods from the mood list.
     */

    public void clearMoods(){
        MoodList.clear();
    }

    /**
     * Updates a specific mood in the mood list based on its timestamp.
     *
     * @param time The timestamp of the mood to update.
     * @param mood The updated mood event object.
     */
    public void updateMood(long time, MoodEvent mood){
        // Update data of the specific mood
        for (int i=0;i<MoodList.size();i++){
            if (MoodList.get(i).getDate() == time){
                MoodList.set(i,mood);
            }
        }
    }

    /**
     * Updates a user's information in the user list based on their username.
     *
     * @param username The username of the user to update.
     */
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


    /**
     * Returns the current user ID.
     *
     * @return A string representing the current user ID.
     */
    public  String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Sets the current user ID.
     *
     * @param currentUserId The user ID to set as the current user ID.
     */
    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    /**
     * Returns the singleton instance of LocalStorage.
     *
     * @return The singleton instance of LocalStorage.
     */
    public static synchronized LocalStorage getInstance() {
        if (instance == null) {
            instance = new LocalStorage();
        }
        return instance;
    }

    /**
     * Retrieves the currently logged-in user object based on their user ID.
     *
     * @return The User object representing the currently logged-in user, or null if not found.
     */
    public User getCurrentUser(){
        return findUser(currentUserId);
    }

    /**
     * Returns a list of comments stored locally.
     *
     * @return An ArrayList of Comment objects.
     */
    public ArrayList<Comment> getComments() {
        return Comments;
    }

    /**
     * Adds a comment to the local storage list of comments.
     *
     * @param comment The Comment object to add to the list.
     */
    public void addComment(Comment comment) {
        this.Comments.add(comment);
    }


    /**
     * Retrieves all stored mood events in local storage.
     *
     * @return An ArrayList of MoodEvent objects representing stored moods.
     */
    public ArrayList<MoodEvent> getMoodList() {
        return MoodList;
    }

    /**
     * Adds a mood event to the mood list.
     *
     * @param mood The MoodEvent object to be added to the mood list.
     */
    public void addMood(MoodEvent mood) {
        this.MoodList.add(mood);
    }


    /**
     * Retrieves the list of users stored locally.
     *
     * @return An ArrayList of User objects representing all stored users.
     */
    public ArrayList<User> getUserList() {
        return UserList;
    }

    /**
     * Adds a user to the local storage user list.
     *
     * @param user The User object to be added to the user list.
     */
    public void addUser(User user) {
        this.UserList.add(user);
    }

    /**
     * Checks if a user exists in the local storage user list.
     *
     * @param user The User object to check for existence.
     * @return True if the user exists, false otherwise.
     */
    public boolean checkIfUserExists(User user){
        for (int i=0;i<UserList.size();i++){
            if (UserList.get(i).getUsername().equals(user.getUsername())){
                return true;
            }
        }
        return false;
    }

    /**
     * Updates a private mood event in the private mood list. If the mood already exists,
     * it is updated; otherwise, it is added to the list.
     *
     * @param mood The MoodEvent object to update or add to the private mood list.
     */
    public void updatePrivMood(MoodEvent mood){
        for (int i=0; i<PrivList.size();i++){
            if (mood.getDate() == PrivList.get(i).getDate()){
                PrivList.set(i,mood);
                break;
            }
        }
        PrivList.add(mood);
    }

    /**
     * Removes duplicate entries from the private mood list based on their timestamp.
     */
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

    /**
     * Finds and retrieves a user from the local storage based on their username.
     *
     * @param uname The username of the user to find.
     * @return The User object if found; otherwise, returns a default User object with an empty username.
     */
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

    /**
     * Inserts a comment into the local storage comment list. If the comment already exists
     * based on its ID, it is not added again.
     *
     * @param comment The Comment object to insert into the comment list.
     */
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

    /**
     * Inserts a mood event into the local storage mood list. If a mood with the same timestamp
     * already exists, it is not added again.
     *
     * @param moodEvent The MoodEvent object to insert into the mood list.
     */
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

    /**
     * Retrieves a user from local storage based on their username.
     *
     * @param uName The username of the user to retrieve.
     * @return The User object if found; otherwise, returns null.
     */

    public User getUserFromUName(String uName) {
        User use = null;
        for (int i = 0; i < UserList.size(); i++) {
            if (UserList.get(i).getUsername().equals(uName)) {
                return UserList.get(i);
            }
        }
        return use;
    }

    /**
     * Removes a mood event from the local storage based on its timestamp.
     *
     * @param millis The timestamp of the mood event to remove.
     */
    public void removeMood(long millis){
        for (int i=0; i<MoodList.size();i++) {
            if (MoodList.get(i).getDate() == (millis)){
                MoodList.remove(i);
                break;
            }
        }
    }
    /**
     * Retrieves a mood event from either the public or private lists based on its timestamp.
     *
     * @param millis The timestamp of the mood event to retrieve.
     * @return The MoodEvent object if found; otherwise, returns null.
     */
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

    /**
     * Checks whether a given mood event exists in the private mood list based on its timestamp.
     *
     * @param mood The MoodEvent object to check for existence in the private list.
     * @return True if the mood exists in the private list, false otherwise.
     */
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

    /**
     * Refreshes the private mood list by moving public moods from it into the public list
     * and removing duplicates within itself. Also calls {@link #refreshPubList()} to synchronize lists.
     */
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

    /**
     * Refreshes the public mood list by moving private moods from it into the private list.
     */
    public void refreshPubList(){
        for (int i=0;i<MoodList.size();i++){
            if(!MoodList.get(i).isPublic()) {
                PrivList.add(MoodList.get(i));
                MoodList.remove(i);
            }
        }
    }
    /**
     * Retrieves all moods associated with a given user and their visibility status
     * (public or private).
     *
     * @param user     The User object whose moods are being retrieved.
     * @param isPublic A boolean indicating whether to retrieve public or private moods.
     * @return An ArrayList of MoodEvent objects matching the criteria.
     */
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