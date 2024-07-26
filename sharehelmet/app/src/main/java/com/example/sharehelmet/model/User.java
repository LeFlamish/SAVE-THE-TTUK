package com.example.sharehelmet.model;

import java.util.HashMap;

public class User {
    private String nickname;
    private String Email;
    HashMap<String, String> Record = new HashMap<>();
    public User(){}
    public User(String nickname, String Email){
        this.nickname = nickname;
        this.Email = Email;
        //this.PW = PW;
    }

    public HashMap<String, String> getRecord() {
        return Record;
    }

    public void setRecord(HashMap<String, String> record) {
        Record = record;
    }

    public String getNickname() {return nickname;}
    public void setNickname(String nickname) {this.nickname = nickname;}
    public String getEmail() {return Email;}
    public void setEmail(String Email) {this.Email = Email;}
}
