package com.crown.onspotbusiness.model;

import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.onspotbusiness.utils.ListItemKey;

public class Header extends ListItem {
    public static final int TYPE = ListItemKey.HEADER;

    private String header;

    public Header(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    @Override
    public int getItemType() {
        return TYPE;
    }
}
