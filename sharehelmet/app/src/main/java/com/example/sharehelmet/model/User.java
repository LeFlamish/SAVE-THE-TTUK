package com.example.sharehelmet.model;

import com.naver.maps.geometry.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String nickname;
    private String Email;
    private String Phone;
    private int money;
    Map<String, String> Record = new HashMap<>();
    Map<String, Object> Cupons = new HashMap<>();
    private int now_qr;
    //0:대여qr
    //1:대여중
    //2:반납 qr
    //3:반납 완료
    ArrayList<String> rental_info;
    ArrayList<String> return_info;


    public User(){}
    public User(String nickname, String Email) {
        this.nickname = nickname;
        this.Email = Email;
        this.now_qr=0;
    }

    public Map<String, String> getRecord() {
        return Record;
    }
    public Map<String, Object> getCupons() { return Cupons; }
    public void setRecord(Map<String, String> record) {
        Record = record;
    }
    public void setCupons(Map<String, Object> cupons){ Cupons = cupons; }
    public String getNickname() {return nickname;}
    public void setNickname(String nickname) {this.nickname = nickname;}
    public String getEmail() {return Email;}
    public void setEmail(String Email) {this.Email = Email;}
    public int getNow_qr() {
        return now_qr;
    }

    public void setNow_qr(int now_qr) {
        this.now_qr = now_qr;
    }

    public ArrayList<String> getRental_info() {
        return rental_info;
    }

    public void setRental_info(ArrayList<String> rental_info) {
        this.rental_info = rental_info;
    }

    public ArrayList<String> getReturn_info() {
        return return_info;
    }

    public void setReturn_info(ArrayList<String> return_info) {
        this.return_info = return_info;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }
}
