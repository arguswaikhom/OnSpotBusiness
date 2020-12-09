package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.business.BusinessOSB;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.OSListUtils;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.AppController;
import com.crown.onspotbusiness.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private UserOSB user;
    private BusinessOSB business;
    private ListenerRegistration mUserChangeListener;
    private ListenerRegistration mBusinessChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.navView, navController);

        if (!AppController.getInstance().isAuthenticated()) {
            AppController.getInstance().signOut(this);
            return;
        }

        OSPreferences preferences = OSPreferences.getInstance(getApplicationContext());
        user = preferences.getObject(OSPreferenceKey.USER, UserOSB.class);
        business = preferences.getObject(OSPreferenceKey.BUSINESS, BusinessOSB.class);

        syncAccountInfo();
        verifyDeviceToken();
    }

    private void syncAccountInfo() {
        FirebaseFirestore instance = FirebaseFirestore.getInstance();
        mUserChangeListener = instance.collection(OSString.refUser).document(user.getUserId()).addSnapshotListener((doc, e) -> {
            if (doc != null && doc.exists()) {
                UserOSB updatedUser = doc.toObject(UserOSB.class);
                if (updatedUser != null) {
                    this.user = updatedUser;
                    OSPreferences.getInstance(getApplicationContext()).setObject(updatedUser, OSPreferenceKey.USER);
                    sendBroadcast(new Intent(getString(R.string.action_osb_changes)));
                }
            }
        });
        mBusinessChangeListener = instance.collection(OSString.refBusiness).document(user.getBusinessRefId()).addSnapshotListener(((doc, e) -> {
            if (doc != null && doc.exists()) {
                BusinessOSB osb = doc.toObject(BusinessOSB.class);
                if (osb != null) {

                    OSPreferences.getInstance(getApplicationContext()).setObject(osb, OSPreferenceKey.BUSINESS);
                    sendBroadcast(new Intent(getString(R.string.action_osb_business_changes)));
                }
            }
        }));
    }

    private void verifyDeviceToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (business == null || OSListUtils.isEmpty(business.getDeviceToken()) || !business.getDeviceToken().contains(token)) {
                FirebaseFirestore.getInstance().collection(OSString.refBusiness).document(user.getBusinessRefId())
                        .update(OSString.fieldDeviceToken, FieldValue.arrayUnion(token));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUserChangeListener != null) mUserChangeListener.remove();
        if (mBusinessChangeListener != null) mBusinessChangeListener.remove();
    }
}
