package com.example.sharehelmet.model;

import java.util.ArrayList;

public class Storage {
    private String LocationID;
    private double latitude;
    private double longitude;
    private int stock;
    ArrayList<String> storedHelmetID;

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }

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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public ArrayList<String> getStoredHelmetID() {
        return storedHelmetID;
    }

    public void setStoredHelmetID(ArrayList<String> storedHelmetID) {
        this.storedHelmetID = storedHelmetID;
    }
}
