package com.crown.onspotbusiness.controller;

import com.crown.library.onspotlibrary.model.DeliveryPartnerOSB;
import com.crown.library.onspotlibrary.utils.emun.BusinessRequestStatus;

import java.util.List;

public class DeliveryPartnerStatus {
    private static DeliveryPartnerStatus instance;

    public static DeliveryPartnerStatus getInstance() {
        if (instance == null) return new DeliveryPartnerStatus();
        return instance;
    }

    public BusinessRequestStatus get(List<DeliveryPartnerOSB> osdList, String userId) {
        if (osdList == null || osdList.isEmpty()) return null;
        for (DeliveryPartnerOSB b : osdList) {
            if (b.getUserId().equals(userId)) {
                return BusinessRequestStatus.valueOf(b.getStatus());
            }
        }
        return null;
    }
}
