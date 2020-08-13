package com.crown.onspotbusiness.page;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.AppController;
import com.crown.onspotbusiness.controller.GetUser;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.utils.Connection;
import com.crown.onspotbusiness.utils.HttpVolleyRequest;
import com.crown.onspotbusiness.utils.JsonParse;
import com.crown.onspotbusiness.utils.LocationUtil;
import com.crown.onspotbusiness.utils.MessageUtils;
import com.crown.onspotbusiness.utils.abstracts.OnHttpResponse;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInActivity extends AppCompatActivity implements MessageUtils.OnSnackBarActionListener, OnHttpResponse {
    private final int RC_SIGN_IN = 100;
    private final int RC_CREATE_BUSINESS = 101;
    private final int RC_HTTP_BUSINESS_AVAILABILITY = 102;
    private final int RC_HTTP_HAS_USER_ACCOUNT = 103;
    private final int RC_HTTP_GET_USER = 104;
    private final String TAG = SignInActivity.class.getSimpleName();

    @BindView(android.R.id.content)
    View mParentView;
    @BindView(R.id.loading_pbar)
    ProgressBar mLoadingPb;

    private AlertDialog mLoadingDialog;
    private GoogleSignInAccount mGoogleAccount;
    private Boolean isBusinessAvailableHere = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        ButterKnife.bind(this);

        RelativeLayout mRootLayout = findViewById(R.id.iasiwg_root_layout);

        AnimationDrawable animationDrawable = (AnimationDrawable) mRootLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(2000);

        animationDrawable.start();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
        mLoadingDialog = new AlertDialog.Builder(this, R.style.LoadingDialogTheme).setView(dialogView).setCancelable(false).create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AppController.getInstance().isAuthenticated()) {
            verifyUserBusiness(Preferences.getInstance(getApplicationContext()).getObject(PreferenceKey.USER, User.class));
        } else {
            prepareAccountSetUp();
        }
    }

    private void prepareAccountSetUp() {
        /* Check internet connection first */
        if (!Connection.hasConnection(this)) {
            MessageUtils.showActionIndefiniteSnackBar(mParentView, "No connection", "RETRY", 0, (view, requestCode) -> {
                prepareAccountSetUp();
            });
            return;
        }

        /* Check for the location service provider */
        if (!LocationUtil.isGPSEnabled(this)) {
            String message = "Your location information is require to use OnSpot Business";
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Enable", (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        /* Check if the location permission has granted already */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        checkBusinessAvailability();
                    }

                    if (report.isAnyPermissionPermanentlyDenied()) {
                        LocationUtil.showSettingsDialog(SignInActivity.this);
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    token.continuePermissionRequest();
                }
            }).check();
        } else {
            checkBusinessAvailability();
        }
    }

    @SuppressLint("MissingPermission")
    private void checkBusinessAvailability() {
        /* Already check for availability and available */
        if (isBusinessAvailableHere != null && isBusinessAvailableHere) return;

        mLoadingDialog.show();
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(this);
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String postalCode = addresses.get(0).getPostalCode();
                    Log.v(TAG, "Postal code: " + postalCode);

                    String url = getString(R.string.domain) + "/getBusinessAvailability/";
                    Map<String, String> map = new HashMap<>();
                    map.put("postalCode", postalCode);
                    new HttpVolleyRequest(Request.Method.POST, url, null, RC_HTTP_BUSINESS_AVAILABILITY, null, map, this).execute();
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                    mLoadingDialog.dismiss();
                    LocationUtil.showLocationErrorDialog(this);
                }

            } else {
                mLoadingDialog.dismiss();
                LocationUtil.showLocationErrorDialog(this);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN: {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    mGoogleAccount = task.getResult(ApiException.class);

                    // If the business is available, allow direct sign in otherwise check for the account availability
                    if (isBusinessAvailableHere == null) {
                        prepareAccountSetUp();
                        return;
                    }

                    if (isBusinessAvailableHere) {
                        firebaseAuthWithGoogle(mGoogleAccount);
                    } else {
                        String url = getString(R.string.domain) + "/getAccountAvailability/";
                        Map<String, String> map = new HashMap<>();
                        map.put("email", mGoogleAccount.getEmail());
                        new HttpVolleyRequest(Request.Method.POST, url, null, RC_HTTP_HAS_USER_ACCOUNT, null, map, this).execute();
                    }
                } catch (ApiException e) {
                    MessageUtils.showActionIndefiniteSnackBar(mParentView, "Sign in failed", "RETRY", RC_SIGN_IN, SignInActivity.this);
                    Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                    Log.v(TAG, "Error: " + e.getMessage());
                } catch (Exception e) {
                    MessageUtils.showActionIndefiniteSnackBar(mParentView, "Sign in failed", "RETRY", RC_SIGN_IN, SignInActivity.this);
                    Log.w(TAG, "signInResult:failed=" + Arrays.toString(e.getStackTrace()));
                    Log.v(TAG, "Error: " + e.getMessage());
                }
                break;
            }
            case RC_CREATE_BUSINESS: {
                if (resultCode == RESULT_OK) {
                    String businessID = data.getStringExtra(CreateBusinessActivity.KEY_BUSINESS_ID);
                    String businessRefID = data.getStringExtra(CreateBusinessActivity.KEY_BUSINESS_REF_ID);
                    updateBusiness(true, businessRefID, businessID);

                    verifyUserBusiness(Preferences.getInstance(getApplicationContext()).getObject(PreferenceKey.USER, User.class));
                } else {
                    finish();
                }
                break;
            }
        }
    }

    public void updateBusiness(boolean hasBusiness, String businessRefID, String businessID) {
        Preferences preferences = Preferences.getInstance(getApplicationContext());
        User user = preferences.getObject(PreferenceKey.USER, User.class);
        user.setHasOnSpotBusinessAccount(hasBusiness);
        user.setBusinessRefId(businessRefID);
        user.setBusinessId(businessID);
        preferences.setObject(user, PreferenceKey.USER);
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        AppController.getInstance().getFirebaseAuth().signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithCredential:success");
                FirebaseUser user = AppController.getInstance().getFirebaseAuth().getCurrentUser();
                if (user != null) {
                    Log.v(TAG, "\n\nUser token: " + user.getIdToken(false) + "\n\n");
                    GetUser.get(this, RC_HTTP_GET_USER);
                    mLoadingPb.setVisibility(View.VISIBLE);
                } else {
                    MessageUtils.showActionIndefiniteSnackBar(mParentView, "Sign in failed", "RETRY", RC_SIGN_IN, SignInActivity.this);
                }
            } else {
                MessageUtils.showActionIndefiniteSnackBar(mParentView, "Sign in failed", "RETRY", RC_SIGN_IN, SignInActivity.this);
                Log.w(TAG, "signInWithCredential:failure", task.getException());
            }
        });
    }

    @OnClick(R.id.sign_in_btn)
    void signInWithGoogle() {
        AppController.getInstance().getGoogleSignInClient().signOut();
        AppController.getInstance().setGoogleSignInClient(null);
        Intent signInIntent = AppController.getInstance().getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onSnackBarActionClicked(View view, int requestCode) {
        if (requestCode == RC_SIGN_IN) {
            signInWithGoogle();
        }
    }

    @Override
    public void onHttpResponse(String response, int request) {
        Log.d(TAG, response);
        switch (request) {
            case RC_HTTP_GET_USER: {
                mLoadingPb.setVisibility(View.VISIBLE);
                User user = User.fromJson(response);
                if (user != null) {
                    Preferences preferences = Preferences.getInstance(getApplicationContext());
                    preferences.setObject(user, PreferenceKey.USER);
                    verifyUserBusiness(user);
                }
                break;
            }
            case RC_HTTP_HAS_USER_ACCOUNT: {
                JSONObject jsonObject = JsonParse.stringToObject(response);
                Boolean available = JsonParse.booleanFromObject(jsonObject, "isAvailable");

                if (available == null) {
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    return;
                } else if (available) {
                    firebaseAuthWithGoogle(mGoogleAccount);
                } else {
                    MessageUtils.showActionIndefiniteSnackBar(mParentView, "Account not found", "Change Account", 0, (view, requestCode) -> {
                        signInWithGoogle();
                    });
                }
                break;
            }
            case RC_HTTP_BUSINESS_AVAILABILITY: {
                mLoadingDialog.dismiss();
                JSONObject jsonObject = JsonParse.stringToObject(response);
                Boolean available = JsonParse.booleanFromObject(jsonObject, "isAvailable");

                if (available == null) {
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    return;
                }

                isBusinessAvailableHere = available;

                if (!available) {
                    new AlertDialog.Builder(SignInActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setCancelable(false)
                            .setMessage(getString(R.string.app_name) + " is not available in your current location. You cannot use the app unless you have an existing account.")
                            .setPositiveButton("Sign in", (dialog, which) -> signInWithGoogle())
                            .setNegativeButton("Cancel", null)
                            .show();
                }
                break;
            }
        }
    }

    @Override
    public void onHttpErrorResponse(VolleyError error, int request) {
        Log.v(TAG, "Error: " + error);
        mLoadingPb.setVisibility(View.VISIBLE);
        switch (request) {
            case RC_HTTP_BUSINESS_AVAILABILITY: {
                mLoadingDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void verifyUserBusiness(User user) {
        if (user.isHasOnSpotBusinessAccount() && user.getBusinessId() != null && !TextUtils.isEmpty(user.getBusinessId())
                && user.getBusinessRefId() != null && !TextUtils.isEmpty(user.getBusinessRefId())) {
            navigateMainActivity();
        } else {
            createBusiness();
        }
    }

    private void navigateMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void createBusiness() {
        User user = Preferences.getInstance(getApplicationContext()).getObject(PreferenceKey.USER, User.class);
        Intent intent = new Intent(this, CreateBusinessActivity.class);
        intent.putExtra(CreateBusinessActivity.KEY_USER, new Gson().toJson(user));
        startActivityForResult(intent, RC_CREATE_BUSINESS);
    }
}
