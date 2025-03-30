package com.example.moodsync;

import java.util.ArrayList;


/**
 * Represents a user in the MoodSync application.
 * This class contains user details and manages lists of followers, followings, and comments.
 */
public class User {
    private String id;
    private String name;
    private String userName;
    private String pass;
    private String pfpUrl;
    private String location;
    private String bio;
    private ArrayList<String> followerList = new ArrayList<>();
    private ArrayList<String> followingList = new ArrayList<>();
    private ArrayList<Integer> commentList = new ArrayList<>();

    /**
     * Default constructor required for calls to DataSnapshot.getValue(User.class).
     */
    public User() {

    }

    /**
     * Gets the user's unique ID.
     *
     * @return
     *      The user's unique ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the user's unique ID.
     *
     * @param id
     *      The user's unique ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the user's name.
     *
     * @return
     *      The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param name
     *      The user's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's username.
     *
     * @return
     *      The user's username.
     */
    public String getUsername() {
        return userName;
    }

    /**
     * Sets the user's username.
     *
     * @param userName
     *      The user's username.
     */
    public void setUsername(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the user's password.
     *
     * @return
     *      The user's password.
     */
    public String getPass() {
        return pass;
    }

    /**
     * Sets the user's password.
     *
     * @param pass
     *      The user's password.
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * Gets the URL of the user's profile picture.
     *
     * @return
     *      The URL of the user's profile picture.
     */
    public String getPfpUrl() {
        return this.pfpUrl;
    }

    /**
     * Sets the URL of the user's profile picture.
     *
     * @param pfpUrl
     *      The URL of the user's profile picture.
     */
    public void setPfpUrl(String pfpUrl) {
        this.pfpUrl = pfpUrl;
    }

    /**
     * Gets the user's location.
     *
     * @return
     *      The user's location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the user's location.
     *
     * @param location
     *      The user's location.
     */
    public void setLocation(String location) {
        this.location = location;
    }


    /**
     * Gets the user's bio.
     *
     * @return
     *      The user's bio.
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the user's bio.
     *
     * @param bio
     *      The user's bio.
     */
    public void setBio(String bio) {
        this.bio = bio;
    }


    /**
     * Gets the list of followers' IDs for this user.

     * @return
     * A list of follower IDs.
     */
    public ArrayList<String> getFollowerList() {
        return followerList;
    }

    /**
     * Sets the list of followers' IDs for this user.

     * @param followerList
     * A list of follower IDs.
     */
    public void setFollowerList(ArrayList<String> followerList) {
        this.followerList = followerList;
    }

    /**
     * Gets the list of IDs of users this user is following.
     *
     * @return
     *      A list of IDs of users this user is following.
     */
    public ArrayList<String> getFollowingList() {
        return followingList;
    }

    /**
     * Sets the list of IDs of users this user is following.
     *
     * @param followingList
     *      A list of IDs of users this user is following.
     */
    public void setFollowingList(ArrayList<String> followingList) {
        this.followingList = followingList;
    }

    /**
     * Gets the list of comment IDs associated with this user.
     *
     * @return
     *      A list of comment IDs.
     */
    public ArrayList<Integer> getCommentList() {
        return commentList;
    }

    /**
     * Sets the list of comment IDs associated with this user.
     *
     * @param commentList
     *      A list of comment IDs.
     */
    public void setCommentList(ArrayList<Integer> commentList) {
        this.commentList = commentList;
    }

    /**
     * Adds a follower to the user's follower list.
     *
     * @param followerId
     *      The ID of the follower to add.
     */
    public void addFollower(String followerId) {
        this.followerList.add(followerId);
    }

    /**
     * Adds a user to the user's following list.
     *
     * @param followingId
     *      The ID of the user to follow.
     */
    public void addFollowing(String followingId) {
        this.followingList.add(followingId);
    }

    /**
     * Adds a comment ID to the user's comment list.
     *
     * @param commentId
     *      The ID of the comment to add.
     */
    public void addComment(Integer commentId) {
        this.commentList.add(commentId);
    }
}
