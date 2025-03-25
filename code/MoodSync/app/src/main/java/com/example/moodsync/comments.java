package com.example.moodsync;

public class comments {

    public String ID;
    public String text;

    public String gettext(){
        return this.text;
    }
    public String getID(){
        return this.ID;
    }
    public void setUsername(String text){
        this.text = text;
    }
    public void setPass(String ID){
        this.ID = ID;
    }
}
