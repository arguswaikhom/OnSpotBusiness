package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.business.BusinessOSB;
import com.crown.library.onspotlibrary.model.business.BusinessV6;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();
    private BottomNavigationView mBottomNavigationView;
    private ListenerRegistration mUserChangeListener;
    private ListenerRegistration mBusinessChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNavigationView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(mBottomNavigationView, navController);
        syncAccountInfo();
    }

    private void syncAccountInfo() {
        UserOSB user = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class);
        mUserChangeListener = FirebaseFirestore.getInstance().collection(getString(R.string.ref_user)).document(user.getUserId()).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                UserOSB updatedUser = documentSnapshot.toObject(UserOSB.class);
                if (updatedUser != null) {
                    OSPreferences.getInstance(getApplicationContext()).setObject(updatedUser, OSPreferenceKey.USER);
                    sendBroadcast(new Intent(getString(R.string.action_osb_changes)));
                }
            }
        });
        mBusinessChangeListener = FirebaseFirestore.getInstance().collection(getString(R.string.ref_business)).document(user.getBusinessRefId()).addSnapshotListener(((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                BusinessOSB osb = documentSnapshot.toObject(BusinessOSB.class);
                if (osb != null) {
                    OSPreferences.getInstance(getApplicationContext()).setObject(osb, OSPreferenceKey.BUSINESS);
                    sendBroadcast(new Intent(getString(R.string.action_osb_business_changes)));
                }
            }
        }));

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(result -> {
            BusinessV6 business = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.BUSINESS, BusinessV6.class);
            if (business == null || business.getDeviceToken() == null || business.getDeviceToken().isEmpty() || !business.getDeviceToken().contains(result.getToken())) {
                FirebaseFirestore.getInstance().collection(getString(R.string.ref_business)).document(user.getBusinessRefId()).update(getString(R.string.field_device_token), FieldValue.arrayUnion(result.getToken()));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserChangeListener.remove();
        mBusinessChangeListener.remove();
    }
}
