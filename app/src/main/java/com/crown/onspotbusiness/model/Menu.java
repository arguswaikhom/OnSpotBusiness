package com.crown.onspotbusiness.model;

import java.util.HashMap;
import java.util.Map;

public class Menu {
    public static class Status {
        public static final int STATUS_ITEM_NOT_AVAILABLE = 0;
        public static final int STATUS_ITEM_AVAILABLE = 1;
        public static final int STATUS_ITEM_OUT_OF_STOCK = 2;

        private static Map<Integer, String> status;

        static {
            status = new HashMap<>();
            status.put(STATUS_ITEM_AVAILABLE, "Available");
            status.put(STATUS_ITEM_NOT_AVAILABLE, "Not available");
            status.put(STATUS_ITEM_OUT_OF_STOCK, "Out of stock");
        }

        public static String get(int id) {
            return status.get(id);
        }

        public static Map<Integer, String> all() {
            return status;
        }
    }

    public static class DiscountType {
        public static final int DT_NO_DISCOUNT = 0;
        public static final int DT_PRICE = 1;
        public static final int DT_PERCENT = 2;

        private static Map<Integer, String> discountType;
        private static Map<Integer, String> discountTypeKeyExchange;

        static {
            discountType = new HashMap<>();
            discountTypeKeyExchange = new HashMap<>();

            discountType.put(DT_NO_DISCOUNT, "No discount");
            discountType.put(DT_PRICE, "Price");
            discountType.put(DT_PERCENT, "percent");

            discountTypeKeyExchange.put(DT_NO_DISCOUNT, MenuItem.Discount.NO_DISCOUNT.toString().toUpperCase());
            discountTypeKeyExchange.put(DT_PRICE, MenuItem.Discount.PRICE.toString().toUpperCase());
            discountTypeKeyExchange.put(DT_PERCENT, MenuItem.Discount.PERCENT.toString().toUpperCase());
        }

        public static String get(int id) {
            return discountType.get(id);
        }

        public static String getKey(int id) {
            return discountTypeKeyExchange.get(id);
        }

        public static Map<Integer, String> all() {
            return discountType;
        }
    }
}
