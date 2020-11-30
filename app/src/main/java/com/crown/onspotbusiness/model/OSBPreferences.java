package com.crown.onspotbusiness.model;

@Deprecated
public class OSBPreferences {
    public static int DETAILS = 1;
    public static int GRID = 2;

    private int bussItemView;

    public int getBussItemView() {
        return bussItemView;
    }

    public void setBussItemView(int bussItemView) {
        this.bussItemView = bussItemView;
    }
}
