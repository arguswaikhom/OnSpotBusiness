package com.crown.onspotbusiness.controller;


import com.crown.onspotbusiness.model.OrderItem;

public class ItemHelper {
    private OrderItem item;

    public ItemHelper(OrderItem item) {
        this.item = item;
    }

    public long getFinalAmount() {
        return (item.getPrice() - getDiscount()) + item.getTax();
    }

    private long getDiscount() {
        switch (item.getDiscountType()) {
            case "percent": {
                return (item.getDiscountValue() * item.getPrice()) / 100;
            }
            case "price": {
                return item.getDiscountValue();
            }
            case "no_discount":
            default: {
                return 0;
            }
        }
    }
}
