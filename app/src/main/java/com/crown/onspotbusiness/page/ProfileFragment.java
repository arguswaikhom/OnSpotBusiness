package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.business.BusinessV6;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.page.ContactUsActivity;
import com.crown.library.onspotlibrary.page.EditProfileActivity;
import com.crown.library.onspotlibrary.utils.OSCommonIntents;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.AppController;
import com.crown.onspotbusiness.databinding.DialogOpenOrCloseBusinessBinding;
import com.crown.onspotbusiness.databinding.FragmentProfileBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private UserOSB user;
    private BusinessV6 business;
    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpUi();
    }

    private void init() {
        binding.myBusinessLl.setOnClickListener(v -> startActivity(new Intent(getActivity(), ModifyBusinessActivity.class)));
        binding.openCloseBusinessFl.setOnClickListener(this::onClickedOpenOrCloseSwitchLayout);
        binding.deliveryPartnersOpi.setOnClickListener(v -> startActivity(new Intent(getContext(), DeliveryPartnersActivity.class)));
        binding.editProfileOpi.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            intent.putExtra(EditProfileActivity.USER_ID, user.getUserId());
            startActivity(intent);
        });
        binding.contactUsOpi.setOnClickListener(v -> startActivity(new Intent(getContext(), ContactUsActivity.class)));
        binding.shareOpi.setOnClickListener(v -> OSCommonIntents.onIntentShareAppLink(getContext()));
        binding.rateThisAppOpi.setOnClickListener(v -> OSCommonIntents.onIntentAppOnPlayStore(getContext()));
        binding.logoutOpi.setOnClickListener(v -> AppController.getInstance().signOut(getActivity()));
        binding.logoutOpi.setOnClickListener(v -> AppController.getInstance().signOut(getActivity()));
    }

    private void setUpUi() {
        OSPreferences instance = OSPreferences.getInstance(getContext().getApplicationContext());
        user = instance.getObject(OSPreferenceKey.USER, UserOSB.class);
        business = instance.getObject(OSPreferenceKey.BUSINESS, BusinessV6.class);

        Glide.with(this)
                .load(user.getProfileImageUrl())
                .apply(new RequestOptions().centerCrop().circleCrop())
                .into(binding.profileImageIv);

        binding.userNameTv.setText(user.getDisplayName());
        binding.emailTv.setText(user.getEmail());
        binding.openCloseBusinessSwitch.setChecked(business.getIsOpen() == null ? true : business.getIsOpen());
    }

    void onClickedOpenOrCloseSwitchLayout(View view) {
        DialogOpenOrCloseBusinessBinding ocBinding = DialogOpenOrCloseBusinessBinding.inflate(getLayoutInflater(), null, false);

        boolean isOpen = business.getIsOpen() == null ? true : business.getIsOpen();
        ocBinding.rbtnDoocbOpen.setChecked(isOpen);
        ocBinding.rbtnDoocbClose.setChecked(!isOpen);

        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.LoadingDialogTheme).setView(ocBinding.getRoot()).create();
        dialog.show();

        ocBinding.rbtnDoocbOpen.setOnClickListener(v -> {
            ocBinding.rbtnDoocbOpen.setChecked(true);
            ocBinding.rbtnDoocbClose.setChecked(false);
        });
        ocBinding.rbtnDoocbClose.setOnClickListener(v -> {
            ocBinding.rbtnDoocbOpen.setChecked(false);
            ocBinding.rbtnDoocbClose.setChecked(true);
        });

        ocBinding.btnDoocbCancel.setOnClickListener(v -> dialog.dismiss());
        ocBinding.btnDoocbOk.setOnClickListener(v -> {
            boolean open = ocBinding.rbtnDoocbOpen.isChecked();
            if (isOpen != open) {
                FirebaseFirestore.getInstance().collection(OSString.refBusiness)
                        .document(business.getBusinessRefId()).update(OSString.fieldIsOpen, open)
                        .addOnSuccessListener(v1 -> setUpUi())
                        .addOnFailureListener(error -> Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
            }
            dialog.dismiss();
        });
    }
}
