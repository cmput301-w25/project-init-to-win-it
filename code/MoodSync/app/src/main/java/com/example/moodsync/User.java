package com.example.moodsync;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class User {
    public String name;
    public String userName;
    public String pass;

    public String id;
    public String pfpUrl;
    public String location;
    public String bio;
    public ArrayList<String> followerList;
    public ArrayList<String> followingList;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }



    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }



    public String getPfpUrl() {
        return pfpUrl;
    }

    public void setPfpUrl(String pfpUrl) {
        this.pfpUrl = pfpUrl;
    }



    public void setFollowerList(ArrayList<String> followerList) {
        this.followerList = followerList;
    }



    public void setFollowingList(ArrayList<String> followingList) {
        this.followingList = followingList;
    }

    public void setCommentList(ArrayList<Integer> commentList) {
        this.commentList = commentList;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    // All the comments that
    public ArrayList<Integer> commentList;

    public String getName() {return name;}
    public String getUsername(){
        return this.userName;
    }
    public String getPass(){
        return this.pass;
    }
    public ArrayList<String> getflwrList(){
        return this.followerList;
    }
    public ArrayList<String> getFlwingList(){
        return this.followingList;
    }
    public ArrayList<Integer> getCList(){
        return this.commentList;
    }

    public void setName(String name) {this.name = name;}
    public void setUsername(String username){
        this.userName = username;
    }
    public void setPass(String passw){
        this.pass = passw;
    }
    public void addFlwr(String flwrID){
        this.followerList.add(flwrID);
    }
    public void addFlwing(String flwingID){
        this.followingList.add(flwingID);
    }
    public void addCList(Integer cId){
        this.commentList.add(cId);
    }

}
