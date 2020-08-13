package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crown.library.onspotlibrary.controller.OSGoogleSignIn;
import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.BusinessHolder;
import com.crown.library.onspotlibrary.model.business.BusinessV6;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.library.onspotlibrary.views.LoadingBounceDialog;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.AppController;
import com.crown.onspotbusiness.databinding.ActivitySignInBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Collections;
import java.util.Date;

public class SignInPage extends AppCompatActivity implements OSGoogleSignIn.OnGoogleSignInResponse {

    private final int RC_SIGN_IN = 1;
    private final int RC_PHONE_VERIFY = 2;

    private UserOSB user;
    private ActivitySignInBinding binding;
    private OSGoogleSignIn signIn;
    private LoadingBounceDialog loadingBounce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingBounce = new LoadingBounceDialog(this);
        binding.signInBtn.setOnClickListener(this::onCLickedSignIn);
        user = OSPreferences.getInstance(this.getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AppController.getInstance().isAuthenticated()) {
            onAuthenticated();
        } else {
            AppController instance = AppController.getInstance();
            signIn = new OSGoogleSignIn(this, instance.getGoogleSignInClient(), instance.getFirebaseAuth(), RC_SIGN_IN, this);
            if (signIn.isDeviceReady()) signIn.checkAppAvailability();
        }
    }

    private void onCLickedSignIn(View view) {
        AppController.getInstance().getGoogleSignInClient().signOut();
        AppController.getInstance().setGoogleSignInClient(null);
        signIn.pickAccount();
    }

    private void verifyBusiness(UserOSB user) {
        String bRefId = user.getBusinessRefId();
        if (TextUtils.isEmpty(bRefId)) {

            DocumentReference docRef = FirebaseFirestore.getInstance().collection(getString(R.string.ref_business)).document();

            BusinessV6 businessV6 = new BusinessV6();
            businessV6.setBusinessRefId(docRef.getId());
            businessV6.setMobileNumber(user.getPhoneNumber());
            businessV6.setCreator(user.getUserId());
            businessV6.setCreatedOn(new Timestamp(new Date()));
            businessV6.setHolder(Collections.singletonList(new BusinessHolder("owner", user.getUserId())));

            docRef.set(businessV6, SetOptions.merge()).addOnSuccessListener(v -> {
                user.setBusinessRefId(docRef.getId());
                user.setHasOnSpotBusinessAccount(true);
                FirebaseFirestore.getInstance().collection(getString(R.string.ref_user)).document(user.getUserId()).set(user, SetOptions.merge()).addOnSuccessListener(v1 -> {
                    navToMainActivity();
                }).addOnFailureListener(e -> {
                    OSMessage.showAIBar(this, "Sign in error!!", "Retry", this::onCLickedSignIn);
                });
            }).addOnFailureListener(e -> {
                OSMessage.showAIBar(this, "Error creating business!!", "Retry", this::onCLickedSignIn);
            });
        }
    }

    private void navToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("debug", "" + data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK && data != null) {
            Log.d("debug", "A: " + signIn.getAvailableHere());
            // If OSSpot is available at user's location, allow direct sign-in
            // If it is not available, check if the user already has account or not
            if (signIn.getAvailableHere() == null) signIn.checkAppAvailability();
            else if (signIn.getAvailableHere()) signIn.signIn(data);
            else {
                signIn.setAccount(data);
                signIn.isExistingUser(value -> {

                    // If the user already has existing account, allow sign-in
                    if (value) signIn.signIn(data);
                    else
                        OSMessage.showAIBar(this, "Account not found", "Change Account", v -> binding.signInBtn.performClick());
                }, (e, msg) -> {
                    OSMessage.showSToast(this, "Can't get account information");
                });
            }
        } else if (requestCode == RC_PHONE_VERIFY && resultCode == RESULT_OK && data != null) {
            String number = data.getStringExtra(PhoneVerificationActivity.KEY_PHONE_NO);

            user = OSPreferences.getInstance(this.getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class);
            user.setPhoneNumber(number);
            user.setHasPhoneNumberVerified(true);
            FirebaseFirestore.getInstance().collection(getString(R.string.ref_user)).document(user.getUserId())
                    .set(user, SetOptions.merge()).addOnSuccessListener(v -> navToMainActivity())
                    .addOnFailureListener(e -> OSMessage.showSToast(this, "Update failed!!"));

        }
    }

    private void onAuthenticated() {
        if (TextUtils.isEmpty(user.getPhoneNumber())) {
            // User phone no. is null or empty
            startActivityForResult(new Intent(this, PhoneVerificationActivity.class), RC_PHONE_VERIFY);
        } else {
            navToMainActivity();
        }
    }

    @Override
    public void onSuccessGoogleSignIn(DocumentSnapshot doc) {
        user = doc.toObject(UserOSB.class);
        user.setUserId(doc.getId());
        if (user == null) {
            OSMessage.showSToast(this, "Failed to get user data");
            return;
        }
        OSPreferences.getInstance(this.getApplicationContext()).setObject(user, OSPreferenceKey.USER);
        verifyBusiness(user);
        onAuthenticated();
    }

    @Override
    public void onFailureGoogleSignIn(String response, Exception e) {
        OSMessage.showAIBar(this, "Sign in failed!!", "Retry", v -> binding.signInBtn.performClick());
    }
}