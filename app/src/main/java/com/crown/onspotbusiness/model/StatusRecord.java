package com.crown.onspotbusiness.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class StatusRecord implements Parcelable {
    public static final Creator<StatusRecord> CREATOR = new Creator<StatusRecord>() {
        @Override
        public StatusRecord createFromParcel(Parcel source) {
            return new StatusRecord(source);
        }

        @Override
        public StatusRecord[] newArray(int size) {
            return new StatusRecord[size];
        }
    };
    private Status status;
    private Timestamp timestamp;

    public StatusRecord() {
    }

    public StatusRecord(Status status, Timestamp timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }


    protected StatusRecord(Parcel in) {
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : Status.values()[tmpStatus];
        this.timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static String getButtonText(Status status) {
        switch (status) {
            case CANCELED:
                return "Canceled";
            case ORDERED:
                return "Accept order";
            case ACCEPTED:
                return "Prepare order";
            case PREPARING:
                return "Order ready";
            case ON_THE_WAY:
            case DELIVERED:
            default:
                return "Delivered";
        }
    }

    public Status getStatus() {
        return status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeParcelable(this.timestamp, flags);
    }

    public enum Status {
        ORDERED, ACCEPTED, PREPARING, ON_THE_WAY, DELIVERED, CANCELED
    }
}