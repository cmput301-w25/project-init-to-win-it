package com.example.moodsync;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class User {
    public String userName;
    public String pass;
    public ArrayList<Integer> followerList;
    public ArrayList<Integer> followingList;

    // All the comments that
    public ArrayList<Integer> commentList;

    public String getUsername(){
        return this.userName;
    }
    public String getPass(){
        return this.pass;
    }
    public ArrayList<Integer> getflwrList(){
        return this.followerList;
    }
    public ArrayList<Integer> getFlwingList(){
        return this.followingList;
    }
    public ArrayList<Integer> getCList(){
        return this.commentList;
    }
    public void setUsername(String usrname){
        this.userName = usrname;
    }
    public void setPass(String passw){
        this.pass = passw;
    }
    public void addFlwr(Integer flwrID){
        this.followerList.add(flwrID);
    }
    public void addFlwing(Integer flwingID){
        this.followingList.add(flwingID);
    }
    public void addCList(Integer cId){
        this.commentList.add(cId);
    }


}
