package com.crown.onspotbusiness.controller;

import android.util.Log;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.business.BusinessV6;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.R;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.RemoteMessage;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = FirebaseMessagingService.class.getName();

    @Override
    public void onMessageReceived(@NotNull RemoteMessage remoteMessage) {

    }

    @Override
    public void onNewToken(@NotNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendDeviceToken(token);
    }

    private void sendDeviceToken(String token) {
        BusinessV6 business = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.BUSINESS, BusinessV6.class);
        if (business == null) return;
        String field = getString(R.string.field_device_token);
        FirebaseFirestore.getInstance().collection(getString(R.string.ref_business)).document(business.getBusinessRefId())
                .update(field, FieldValue.arrayUnion(token));
    }
}