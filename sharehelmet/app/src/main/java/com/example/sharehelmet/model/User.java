package com.example.sharehelmet.model;

public class User {
    private String nickname;
    private String Email;

    public User(){}
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
