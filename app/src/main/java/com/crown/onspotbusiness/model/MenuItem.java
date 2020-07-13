package com.crown.onspotbusiness.model;

import androidx.annotation.NonNull;

import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.onspotbusiness.utils.ListItemKey;
import com.crown.onspotbusiness.utils.MenuItemHelper;
import com.google.gson.Gson;

import java.util.List;

public class MenuItem extends ListItem {
    String category;
    Discount discountType;
    Long discountValue;
    String itemId;
    String itemName;
    Long price;
    Long tax;
    Long onStock;
    MenuItemHelper.Status status;
    String description;

    private List<String> imageUrls;
    private Boolean isDeleted;

    MenuItem() {
    }

    @Override
    public int getItemType() {
        return ListItemKey.MENU_ITEM;
    }

    public String getCategory() {
        return category;
    }

    public Discount getDiscountType() {
        return discountType;
    }

    public Long getDiscountValue() {
        return discountValue;
    }

    public String getImageUrl() {
        return imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(0) : null;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public Boolean getDeleted() {
        return isDeleted == null ? false : isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public Long getPrice() {
        return price;
    }

    public Long getTax() {
        return tax;
    }

    public Long getPriceWithTax() {
        return price + tax;
    }

    public Long getFinalPrice() {
        return (price + tax) - getDiscount();
    }

    public Long getOnStock() {
        return onStock;
    }

    public void setOnStock(Long onStock) {
        this.onStock = onStock;
    }

    public MenuItemHelper.Status getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDiscount() {
        switch (discountType) {
            case PERCENT: {
                return (discountValue * price) / 100;
            }
            case PRICE: {
                return discountValue;
            }
            case NO_DISCOUNT:
            default: {
                return 0;
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public enum Discount {
        NO_DISCOUNT, PERCENT, PRICE
    }
}
