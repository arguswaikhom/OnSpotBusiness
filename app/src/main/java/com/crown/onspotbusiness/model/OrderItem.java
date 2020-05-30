package com.crown.onspotbusiness.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.crown.onspotbusiness.utils.ListItemKey;
import com.crown.onspotbusiness.utils.abstracts.ListItem;
import com.google.gson.Gson;

import java.util.List;

public class OrderItem extends ListItem implements Parcelable {
    public static final int TYPE = ListItemKey.ORDER_ITEM;
    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel source) {
            return new OrderItem(source);
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };
    private int quantity = 0;
    private String category;
    private String itemId;
    private String discountType;
    private Long discountValue;
    private Long price;
    private Long tax;
    private Long priceWithTax;
    private List<String> imageUrls;
    private String itemName;

    public OrderItem() {
    }

    protected OrderItem(Parcel in) {
        this.quantity = in.readInt();
        this.category = in.readString();
        this.itemId = in.readString();
        this.discountType = in.readString();
        this.discountValue = (Long) in.readValue(Long.class.getClassLoader());
        this.price = (Long) in.readValue(Long.class.getClassLoader());
        this.tax = (Long) in.readValue(Long.class.getClassLoader());
        this.priceWithTax = (Long) in.readValue(Long.class.getClassLoader());
        this.imageUrls = in.createStringArrayList();
        this.itemName = in.readString();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    public Long getPriceWithTax() {
        return price + tax;
    }

    public String getItemId() {
        return itemId;
    }

    public String getCategory() {
        return category;
    }

    public String getDiscountType() {
        return discountType;
    }

    public Long getDiscountValue() {
        return discountValue;
    }

    public Long getPrice() {
        return price;
    }

    public Long getTax() {
        return tax;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getItemName() {
        return itemName;
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
        dest.writeInt(this.quantity);
        dest.writeString(this.category);
        dest.writeString(this.itemId);
        dest.writeString(this.discountType);
        dest.writeValue(this.discountValue);
        dest.writeValue(this.price);
        dest.writeValue(this.tax);
        dest.writeValue(this.priceWithTax);
        dest.writeStringList(this.imageUrls);
        dest.writeString(this.itemName);
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
