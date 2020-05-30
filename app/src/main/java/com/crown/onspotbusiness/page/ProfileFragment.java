package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.AppController;
import com.crown.onspotbusiness.model.Business;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileFragment extends Fragment {
    private static final String TAG = ProfileFragment.class.getName();

    @BindView(R.id.iv_fp_profile_image)
    ImageView mProfileImageIV;
    @BindView(R.id.tv_fp_display_name)
    TextView mDisplayNameTV;
    @BindView(R.id.tv_fp_email)
    TextView mEmailTV;
    @BindView(R.id.switch_ap_open_or_close)
    Switch mOpenOrCloseSwitch;

    private Business business;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpUi();
    }

    private void setUpUi() {
        Preferences preferences = Preferences.getInstance(getActivity().getApplicationContext());
        User user = preferences.getObject(PreferenceKey.USER, User.class);
        business = preferences.getObject(PreferenceKey.BUSINESS, Business.class);

        Glide.with(this)
                .load(user.getProfileImageUrl())
                .apply(new RequestOptions().centerCrop().circleCrop())
                .into(mProfileImageIV);

        mDisplayNameTV.setText(user.getDisplayName());
        mEmailTV.setText(user.getEmail());
        mOpenOrCloseSwitch.setChecked(business.getOpen() == null ? true : business.getOpen());
    }

    @OnClick(R.id.mcv_fp_business)
    void onCLickedMyBusiness() {
        Intent intent = new Intent(getActivity(), CreateBusinessActivity.class);
        intent.putExtra(CreateBusinessActivity.KEY_BUSINESS, new Gson().toJson(business));
        startActivity(intent);
    }

    @OnClick(R.id.ll_fp_logout)
    void onClickedLogout() {
        AppController.getInstance().signOut(getActivity());
    }

    @OnClick(R.id.fl_sp_open_or_close_switch_layout)
    void onClickedOpenOrCloseSwitch() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_open_or_close_business, null);
        RadioButton openRBtn = dialogView.findViewById(R.id.rbtn_doocb_open);
        RadioButton closeRBtn = dialogView.findViewById(R.id.rbtn_doocb_close);
        Button okBtn = dialogView.findViewById(R.id.btn_doocb_ok);
        Button cancelBtn = dialogView.findViewById(R.id.btn_doocb_cancel);

        boolean isOpen = business.getOpen() == null ? true : business.getOpen();
        openRBtn.setChecked(isOpen);
        closeRBtn.setChecked(!isOpen);

        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.LoadingDialogTheme).setView(dialogView).create();
        dialog.show();

        openRBtn.setOnClickListener(v -> {
            openRBtn.setChecked(true);
            closeRBtn.setChecked(false);
        });
        closeRBtn.setOnClickListener(v -> {
            openRBtn.setChecked(false);
            closeRBtn.setChecked(true);
        });

        cancelBtn.setOnClickListener(view -> dialog.dismiss());
        okBtn.setOnClickListener(view -> {
            boolean open = openRBtn.isChecked();
            if (isOpen != open) {
                FirebaseFirestore.getInstance().collection(getString(R.string.ref_business))
                        .document(business.getBusinessRefId()).update("open", open)
                        .addOnSuccessListener(v -> setUpUi())
                        .addOnFailureListener(error -> Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
            }
            dialog.dismiss();
        });
    }
}
