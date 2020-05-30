package com.crown.onspotbusiness.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.crown.onspotbusiness.utils.ListItemKey;
import com.crown.onspotbusiness.utils.abstracts.ListItem;
import com.google.gson.Gson;

import java.util.List;

public class Order extends ListItem implements Parcelable {
    public transient static final int TYPE = ListItemKey.ORDER;
    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
    private String orderId;
    private String businessDisplayName;
    private String customerDisplayName;
    private String customerId;
    private String businessRefId;
    private StatusRecord.Status status;
    private List<StatusRecord> statusRecord;
    private Long totalPrice;
    private Long finalPrice;
    private List<OrderItem> items;
    private Contact contact;
    private Location destination;

    public Order() {
    }

    protected Order(Parcel in) {
        this.businessDisplayName = in.readString();
        this.customerDisplayName = in.readString();
        this.customerId = in.readString();
        this.businessRefId = in.readString();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : StatusRecord.Status.values()[tmpStatus];
        this.statusRecord = in.createTypedArrayList(StatusRecord.CREATOR);
        this.totalPrice = (Long) in.readValue(Long.class.getClassLoader());
        this.finalPrice = (Long) in.readValue(Long.class.getClassLoader());
        this.items = in.createTypedArrayList(OrderItem.CREATOR);
    }

    public static Creator<Order> getCREATOR() {
        return CREATOR;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBusinessDisplayName() {
        return businessDisplayName;
    }

    public void setBusinessDisplayName(String businessDisplayName) {
        this.businessDisplayName = businessDisplayName;
    }

    public String getCustomerDisplayName() {
        return customerDisplayName;
    }

    public void setCustomerDisplayName(String customerDisplayName) {
        this.customerDisplayName = customerDisplayName;
    }

    public Contact getContact() {
        return contact;
    }

    public Location getDestination() {
        return destination;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getBusinessRefId() {
        return businessRefId;
    }

    public void setBusinessRefId(String businessRefId) {
        this.businessRefId = businessRefId;
    }

    public StatusRecord.Status getStatus() {
        return status;
    }

    public void setStatus(StatusRecord.Status status) {
        this.status = status;
    }

    public List<StatusRecord> getStatusRecord() {
        return statusRecord;
    }

    public void setStatusRecord(List<StatusRecord> statusRecord) {
        this.statusRecord = statusRecord;
    }

    public Long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Long finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public long getFinalAmount() {
        return this.finalPrice;
    }

    public long getQuantity() {
        return this.items.size();
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public int getItemType() {
        return TYPE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.businessDisplayName);
        dest.writeString(this.customerDisplayName);
        dest.writeString(this.customerId);
        dest.writeString(this.businessRefId);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeTypedList(this.statusRecord);
        dest.writeValue(this.totalPrice);
        dest.writeValue(this.finalPrice);
        dest.writeTypedList(this.items);
    }
}
