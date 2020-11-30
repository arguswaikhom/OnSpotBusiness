package com.crown.onspotbusiness.model;

import com.crown.library.onspotlibrary.model.DeliveryPartnerOSB;
import com.crown.library.onspotlibrary.utils.ListItemType;

public class DeliveryPartnerAssign extends DeliveryPartnerOSB {
    @Override
    public int getItemType() {
        return ListItemType.DELIVERY_PARTNER_ASSIGN;
    }
}
