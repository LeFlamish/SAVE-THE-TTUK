package com.example.sharehelmet.model;

public class Helmet {
    private String helmetId;
    private int batteryState;
    private boolean isBorrow;
    private String storageId;
    private String userId;
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

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
