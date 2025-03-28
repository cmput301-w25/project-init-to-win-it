package com.example.moodsync;

import java.util.ArrayList;

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

    // Constructors
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    // Getters and Setters

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return userName;
    }
    public void setUsername(String userName) {
        this.userName = userName;
    }

    public String getPass() {
        return pass;
    }
    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getPfpUrl() {
        return this.pfpUrl;
    }
    public void setPfpUrl(String pfpUrl) {
        this.pfpUrl = pfpUrl;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public ArrayList<String> getFollowerList() {
        return followerList;
    }
    public void setFollowerList(ArrayList<String> followerList) {
        this.followerList = followerList;
    }

    public ArrayList<String> getFollowingList() {
        return followingList;
    }
    public void setFollowingList(ArrayList<String> followingList) {
        this.followingList = followingList;
    }

    public ArrayList<Integer> getCommentList() {
        return commentList;
    }
    public void setCommentList(ArrayList<Integer> commentList) {
        this.commentList = commentList;
    }

    // Utility methods to add entries to lists

    public void addFollower(String followerId) {
        this.followerList.add(followerId);
    }

    public void addFollowing(String followingId) {
        this.followingList.add(followingId);
    }

    public void addComment(Integer commentId) {
        this.commentList.add(commentId);
    }
}
