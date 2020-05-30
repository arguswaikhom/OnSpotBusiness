package com.crown.onspotbusiness.model;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Business {
    List<String> imageUrls;
    private String businessId;
    private String businessRefId;
    private String businessType;
    private Timestamp createdOn;
    private String creator;
    private String displayName;
    private String email;
    private List<Holder> holder;
    private Location location;
    private String mobileNumber;
    private String website;
    private Time openingTime;
    private Time closingTime;
    private String openingDays;
    private Double deliveryRange;
    private Boolean passiveOpenEnable;
    private Boolean open;

    public Business() {
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

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Holder> getHolder() {
        return holder;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getImageUrl() {
        return imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(0) : null;
    }

    public Time getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(Time openingTime) {
        this.openingTime = openingTime;
    }

    public Time getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(Time closingTime) {
        this.closingTime = closingTime;
    }

    public String getOpeningDays() {
        return openingDays;
    }

    public void setOpeningDays(String openingDays) {
        this.openingDays = openingDays;
    }

    public Double getDeliveryRange() {
        return deliveryRange;
    }

    public void setDeliveryRange(Double deliveryRange) {
        this.deliveryRange = deliveryRange;
    }

    public Boolean getPassiveOpenEnable() {
        return passiveOpenEnable;
    }

    public void setPassiveOpenEnable(Boolean passiveOpenEnable) {
        this.passiveOpenEnable = passiveOpenEnable;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static class Holder {
        private String role;
        private String userId;

        public Holder() {
        }

        public String getRole() {
            return role;
        }

        public String getUserId() {
            return userId;
        }

        @NotNull
        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
}
