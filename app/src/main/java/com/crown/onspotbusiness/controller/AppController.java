package com.crown.onspotbusiness.controller;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.page.SignInPage;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
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
        OSPreferences.getInstance(this).setObject(getString(R.string.package_onspot_business), OSPreferenceKey.APP_PACKAGE);
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
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public boolean isAuthenticated() {
        boolean hasAuthenticated = false;
        FirebaseAuth auth = getFirebaseAuth();
        UserOSB user = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class);
        if (auth != null && auth.getUid() != null && !auth.getUid().isEmpty() && auth.getCurrentUser() != null && user != null) {
            hasAuthenticated = true;
        }
        return hasAuthenticated;
    }

    public void signOut(Activity activity) {
        getFirebaseAuth().signOut();
        getGoogleSignInClient().signOut();
        setFirebaseAuth(null);
        setGoogleSignInClient(null);
        OSPreferences.getInstance(getApplicationContext()).clearAll();
        getRequestQueue().getCache().clear();
        // ClearCacheData.clear(this);

        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(activity, SignInPage.class);
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
