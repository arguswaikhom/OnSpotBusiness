package com.crown.onspotbusiness.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockData {

    public static List<String> images() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("https://i.pinimg.com/564x/fe/ec/98/feec9882976f0bbf165b0183f39afeda.jpg");
        }
        return list;
    }

    public static List<String> categories() {
        return new ArrayList<>(Arrays.asList(
                "Carbonated Beverages", "Milk", "Fresh Bread & Rolls", "Salty Snacks", "Beer Ale Alcoholic Cider",
                "Natural Cheese", "Wine", "Fz Dinners Entrees", "Cold Cereal", "Cigarettes", "Yogurt",
                "Rfg Juices Drinks", "Ice Cream Sherbet", "Bottled Water", "Soup", "Crackers", "Cookies",
                "Bottled Juices", "Coffee", "Luncheon Meats", "Breakfast Meats", "Rfg Fresh Eggs",
                "Toilet Tissue", "Dog Food", "Total Chocolate Candy", "Fz Pizza", "Rfg Salad Coleslaw",
                "Fz Novelties", "Laundry Detergent", "Vegetables"
        ));
    }

    public static List<String> businessType() {
        return new ArrayList<>(Arrays.asList(
                "Appliances", "Beauty", "Books", "Car & Motorbike", "Clothing & Accessories",
                "Computers & Accessories", "Electronics", "Furniture", "Garden & Outdoors",
                "Grocery & Gourmet Foods", "Health & Personal Care", "Home & Kitchen",
                "Industrial & Scientific", "Jewellery", "Luggage & Bags", "Luxury Beauty",
                "Musical Instruments", "Office Products", "Pet Supplies", "Shoes & Handbags", "Software",
                "Sports, Fitness & Outdoors", "Tools & Home Improvement", "Toys & Games", "Watch store"
        ));
    }
}
