package com.crown.onspotbusiness.model;

import androidx.annotation.NonNull;

import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.onspotbusiness.utils.ListItemKey;
import com.google.gson.Gson;

public class MenuItemImage extends ListItem {
    public static final int SOURCE_DEVICE = 1;
    public static final int SOURCE_SERVER = 2;

    private final Object image;
    private int imageSource;

    public MenuItemImage(Object image) {
        this.image = image;
    }

    public MenuItemImage(Object image, int imageSource) {
        this.image = image;
        this.imageSource = imageSource;
    }

    public Object getImage() {
        return image;
    }

    public int getImageSource() {
        return imageSource;
    }

    @Override
    public int getItemType() {
        return ListItemKey.MENU_ITEM_IMAGE;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
