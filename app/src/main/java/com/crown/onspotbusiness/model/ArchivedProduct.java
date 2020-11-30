package com.crown.onspotbusiness.model;

import com.crown.library.onspotlibrary.model.businessItem.BusinessItemOSB;
import com.crown.library.onspotlibrary.utils.ListItemType;

public class ArchivedProduct extends BusinessItemOSB {
    @Override
    public int getItemType() {
        return ListItemType.ARCHIVED_PRODUCT;
    }
}

