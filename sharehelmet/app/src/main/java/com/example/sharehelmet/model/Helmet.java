package com.example.sharehelmet.model;

public class Helmet {
    private String helmetId;
    private int batteryState;
    private boolean isBorrow;
    private String storageId;
    private String userId;

    public String getHelmetId() {
        return helmetId;
    }

    public void setHelmetId(String helmetId) {
        this.helmetId = helmetId;
    }

    public int getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(int batteryState) {
        this.batteryState = batteryState;
    }

    public boolean isBorrow() {
        return isBorrow;
    }

    public void setBorrow(boolean borrow) {
        isBorrow = borrow;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
