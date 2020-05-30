package com.crown.onspotbusiness.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;

public class Location {
    private String addressLine;
    private GeoPoint geoPoint;
    private String postalCode;
    private String howToReach;

    public Location() {
    }

    public Location(String postalCode, String addressLine, GeoPoint geoPoint) {
        this.postalCode = postalCode;
        this.addressLine = addressLine;
        this.geoPoint = geoPoint;
    }

    public Location(String addressLine, GeoPoint geoPoint, String postalCode, String howToReach) {
        this.addressLine = addressLine;
        this.geoPoint = geoPoint;
        this.postalCode = postalCode;
        this.howToReach = howToReach;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getHowToReach() {
        return howToReach;
    }

    public void setHowToReach(String howToReach) {
        this.howToReach = howToReach;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
