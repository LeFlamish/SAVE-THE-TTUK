package com.example.sharehelmet.model;

import com.naver.maps.geometry.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String nickname;
    private String Email;

    Map<String, String> Record = new HashMap<>();
    public User(){}

    public Map<String, String> getRecord() {
        return Record;
    }

    public void setRecord(Map<String, String> record) {
        Record = record;
    }

    public User(String nickname, String Email){
        this.nickname = nickname;
        this.Email = Email;

        //this.PW = PW;
    }



    public String getNickname() {return nickname;}
    public void setNickname(String nickname) {this.nickname = nickname;}
    public String getEmail() {return Email;}
    public void setEmail(String Email) {this.Email = Email;}

}
