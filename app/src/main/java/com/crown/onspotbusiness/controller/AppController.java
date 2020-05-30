package com.crown.onspotbusiness.controller;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.model.Business;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.page.SignInActivity;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

public class AppController extends Application {
    public static final String TAG = AppController.class.getName();
    private static AppController mInstance;
    private RequestQueue mRequestQueue;
    private FirebaseAuth mFirebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public FirebaseAuth getFirebaseAuth() {
        if (mFirebaseAuth == null) {
            return FirebaseAuth.getInstance();
        }
        return mFirebaseAuth;
    }

    public void setFirebaseAuth(FirebaseAuth firebaseAuth) {
        this.mFirebaseAuth = firebaseAuth;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        if (mGoogleSignInClient == null) {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            return GoogleSignIn.getClient(this, googleSignInOptions);
        }
        return mGoogleSignInClient;
    }

    public void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        this.mGoogleSignInClient = googleSignInClient;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }

    public boolean isAuthenticated() {
        boolean hasAuthenticated = false;
        FirebaseAuth auth = getFirebaseAuth();
        User user = Preferences.getInstance(getApplicationContext()).getObject(PreferenceKey.USER, User.class);
        if (auth != null && auth.getUid() != null && !auth.getUid().isEmpty() && auth.getCurrentUser() != null && user != null) {
            hasAuthenticated = true;
        }
        return hasAuthenticated;
    }

    public void signOut(Activity activity) {
        Preferences preferences = Preferences.getInstance(getApplicationContext());
        String token = preferences.getObject(PreferenceKey.DEVICE_TOKEN, String.class);
        Business business = preferences.getObject(PreferenceKey.BUSINESS, Business.class);

        if (token != null && business != null) {
            FirebaseFirestore.getInstance().collection(getString(R.string.ref_business)).document(business.getBusinessRefId())
                    .update(getString(R.string.field_device_token), FieldValue.arrayRemove(token))
                    .addOnSuccessListener(v -> {
                        clearContent(activity);
                    });
        } else {
            clearContent(activity);
        }
    }

    private void clearContent(Activity activity) {
        getFirebaseAuth().signOut();
        getGoogleSignInClient().signOut();
        setFirebaseAuth(null);
        setGoogleSignInClient(null);
        Preferences.getInstance(getApplicationContext()).clearAll();
        // ClearCacheData.clear(this);

        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(activity, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public boolean performIfAuthenticated(Activity activity) {
        if (isAuthenticated()) return true;
        Toast.makeText(getApplicationContext(), "Please login and try again.", Toast.LENGTH_SHORT).show();
        signOut(activity);
        return false;
    }
}
