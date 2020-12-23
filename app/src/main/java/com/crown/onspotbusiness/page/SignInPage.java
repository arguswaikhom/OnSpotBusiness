package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crown.library.onspotlibrary.controller.OSGoogleSignIn;
import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.BusinessHolder;
import com.crown.library.onspotlibrary.model.business.BusinessOSB;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.library.onspotlibrary.views.LoadingBounceDialog;
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
    private ActivitySignInBinding binding;
    private OSGoogleSignIn signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (AppController.getInstance().isAuthenticated()) {
            navToMainActivity();
            return;
        }

        binding.gsibtnAsiSignIn.setOnClickListener(this::onCLickedSignIn);
    }

    private void onCLickedSignIn(View view) {
        AppController instance = AppController.getInstance();
        signIn = new OSGoogleSignIn(this, instance.getGoogleSignInClient(), instance.getFirebaseAuth(), RC_SIGN_IN, this);
        signIn.pickAccount();
    }

    private void navToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) signIn.signIn(data);
    }

    @Override
    public void onSuccessGoogleSignIn(DocumentSnapshot userDoc) {
        UserOSB user = userDoc.toObject(UserOSB.class);
        if (user == null) {
            OSMessage.showSToast(this, "Failed to get user data");
            return;
        }
        if (TextUtils.isEmpty(user.getUserId())) user.setUserId(userDoc.getId());

        if (!TextUtils.isEmpty(user.getBusinessRefId())) {
            OSPreferences.getInstance(this.getApplicationContext()).setObject(user, OSPreferenceKey.USER);
            navToMainActivity();
        } else createBusiness(user);
    }

    private void createBusiness(UserOSB user) {
        LoadingBounceDialog loadingBounce = new LoadingBounceDialog(this);
        DocumentReference bussDocRef = FirebaseFirestore.getInstance().collection(OSString.refBusiness).document();
        BusinessOSB buss = new BusinessOSB();
        buss.setBusinessRefId(bussDocRef.getId());
        buss.setMobileNumber(user.getPhoneNumber());
        buss.setEmail(user.getEmail());
        buss.setCreator(user.getUserId());
        buss.setCreatedOn(new Timestamp(new Date()));
        buss.setHolder(Collections.singletonList(new BusinessHolder(OSString.fieldOwner, user.getUserId())));

        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection(OSString.refUser).document(user.getUserId());
        user.setHasOnSpotBusinessAccount(true);
        user.setBusinessRefId(buss.getBusinessRefId());

        loadingBounce.show();
        FirebaseFirestore.getInstance().runTransaction(transaction -> {
            transaction.set(bussDocRef, buss, SetOptions.merge());
            transaction.set(userDocRef, user, SetOptions.merge());
            return null;
        }).addOnSuccessListener(o -> {
            loadingBounce.dismiss();
            OSPreferences.getInstance(this.getApplicationContext()).setObject(user, OSPreferenceKey.USER);
            navToMainActivity();
        }).addOnFailureListener(e -> {
            loadingBounce.dismiss();
            OSMessage.showAIBar(this, "Error creating business!!", "Retry", this::onCLickedSignIn);
        });
    }

    @Override
    public void onFailureGoogleSignIn(String response, Exception e) {
        OSMessage.showAIBar(this, "Sign in failed!!", "Retry", v -> binding.gsibtnAsiSignIn.performClick());
    }
}