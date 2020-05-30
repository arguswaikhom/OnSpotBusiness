package com.crown.onspotbusiness.utils;

public class MenuItemHelper {

    public MenuItemHelper() {
    }

    public static String getTitle(Status status) {
        switch (status) {
            case OUT_OF_STOCK:
                return "Out of stock";
            case NOT_AVAILABLE:
                return "Not available";
            case AVAILABLE:
            default:
                return "Available";
        }
    }

    public enum Status {
        AVAILABLE(0), NOT_AVAILABLE(1), OUT_OF_STOCK(2);
        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
