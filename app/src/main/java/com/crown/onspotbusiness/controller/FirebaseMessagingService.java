package com.crown.onspotbusiness.controller;

import android.util.Log;

import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
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
        Preferences preferences = Preferences.getInstance(getApplicationContext());
        User user = preferences.getObject(PreferenceKey.USER, User.class);

        if (user == null) return;
        String field = getString(R.string.field_device_token);
        FirebaseFirestore.getInstance().collection(getString(R.string.ref_business))
                .document(user.getBusinessRefId())
                .update(field, FieldValue.arrayUnion(token))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        preferences.setObject(token, PreferenceKey.DEVICE_TOKEN);
                    }
                });
    }
}