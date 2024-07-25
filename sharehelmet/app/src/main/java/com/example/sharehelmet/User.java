package com.example.sharehelmet;

public class User {
    private String nickname;
    private String ID;
    //보안 ㅈ까
    private String PW;

    public String getNickname() {return nickname;}
    public void setNickname(String nickname) {this.nickname = nickname;}
    public String getID() {return ID;}
    public void setID(String ID) {this.ID = ID;}
    public String getPW() {return PW;}
    public void setPW(String PW) {this.PW = PW;}
}
