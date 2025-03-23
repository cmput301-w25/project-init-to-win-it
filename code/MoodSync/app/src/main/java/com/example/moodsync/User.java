package com.example.moodsync;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class User {
    public String name;
    public String userName;
    public String pass;
    public ArrayList<String> followerList;
    public ArrayList<String> followingList;

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
