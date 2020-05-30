package com.crown.onspotbusiness.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class User {
    private String displayName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String userId;
    private String businessId;
    private String businessRefId;
    private boolean hasOnSpotAccount;
    private boolean hasOnSpotBusinessAccount;
    private boolean hasOnSpotDeliveryAccount;
    private boolean hasPhoneNumberVerified;

    public static User fromJson(String json) {
        return new Gson().fromJson(json, User.class);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessRefId() {
        return businessRefId;
    }

    public void setBusinessRefId(String businessRefId) {
        this.businessRefId = businessRefId;
    }

    public boolean isHasOnSpotAccount() {
        return hasOnSpotAccount;
    }

    public boolean isHasOnSpotBusinessAccount() {
        return hasOnSpotBusinessAccount;
    }

    public void setHasOnSpotBusinessAccount(boolean hasOnSpotBusinessAccount) {
        this.hasOnSpotBusinessAccount = hasOnSpotBusinessAccount;
    }

    public boolean isHasOnSpotDeliveryAccount() {
        return hasOnSpotDeliveryAccount;
    }

    public boolean isHasPhoneNumberVerified() {
        return hasPhoneNumberVerified;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
