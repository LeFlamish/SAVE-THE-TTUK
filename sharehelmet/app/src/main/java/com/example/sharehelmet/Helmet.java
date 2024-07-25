package com.example.sharehelmet;

public class Helmet {
    private String id;
    private boolean borrow;

    public boolean isBorrow() {return borrow;}
    public void setBorrow(boolean borrow){ this.borrow = borrow; }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
}
