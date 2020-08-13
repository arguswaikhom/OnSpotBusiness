package com.crown.onspotbusiness.page;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.business.BusinessV6;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.model.Business;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
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
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        UserOSB user = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class);
        mUserChangeListener = firestore.collection(getString(R.string.ref_user)).document(user.getUserId()).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                User updatedUser = documentSnapshot.toObject(User.class);
                Log.v(TAG, "User: " + updatedUser);

                if (updatedUser != null) {
                    Preferences preferences = Preferences.getInstance(getApplicationContext());
                    preferences.setObject(updatedUser, PreferenceKey.USER);
                }
            }
        });
        mBusinessChangeListener = firestore.collection(getString(R.string.ref_business)).document(user.getBusinessRefId()).addSnapshotListener(((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Business business = documentSnapshot.toObject(Business.class);
                Log.v(TAG, "Business: " + business);
                if (business != null) {
                    Preferences.getInstance(getApplicationContext()).setObject(business, PreferenceKey.BUSINESS);
                }

                BusinessV6 osb = documentSnapshot.toObject(BusinessV6.class);
                Log.d("debug", osb.toString());
                if (osb != null) {
                    OSPreferences.getInstance(getApplicationContext()).setObject(osb, OSPreferenceKey.BUSINESS);
                }
            }
        }));

        String token = Preferences.getInstance(getApplicationContext()).getObject(PreferenceKey.DEVICE_TOKEN, String.class);
        Log.v(TAG, "Token state: " + token);
        if (token == null) {
            Log.v(TAG, "Getting token");
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                Log.v(TAG, "" + task.isSuccessful());

                if (task.isSuccessful()) {
                    Log.v(TAG, "Token: " + task.getResult().getToken());
                    sendDeviceToken(task.getResult().getToken());
                }
            }).addOnFailureListener(error -> Log.v(TAG, "#####\n" + error + "\n#####"));
        }
    }

    private void sendDeviceToken(String token) {
        OSPreferences preferences = OSPreferences.getInstance(getApplicationContext());
        UserOSB user = preferences.getObject(OSPreferenceKey.USER, UserOSB.class);

        String field = getString(R.string.field_device_token);
        FirebaseFirestore.getInstance().collection(getString(R.string.ref_business))
                .document(user.getBusinessRefId())
                .update(field, FieldValue.arrayUnion(token))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        preferences.setObject(token, OSPreferenceKey.DEVICE_TOKEN_OSB);
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
