package com.crown.onspotbusiness.page;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.controller.OSViewAnimation;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.OSLocation;
import com.crown.library.onspotlibrary.model.OSShippingCharge;
import com.crown.library.onspotlibrary.model.business.BusinessV6;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.OSImagePicker;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.OSTimeUtils;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.library.onspotlibrary.views.LoadingBounceDialog;
import com.crown.library.onspotlibrary.views.OSCreateLocationDialog;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.ActivityCreateShopBinding;
import com.crown.onspotbusiness.databinding.IvEditBusinessContactBinding;
import com.crown.onspotbusiness.databinding.IvEditBusinessInfoBinding;
import com.crown.onspotbusiness.databinding.IvEditBusinessMoreBinding;
import com.crown.onspotbusiness.model.MenuItemImage;
import com.crown.onspotbusiness.utils.WeekDayHelper;
import com.crown.onspotbusiness.utils.abstracts.OnCardImageRemove;
import com.crown.onspotbusiness.utils.compression.ImageCompression;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class ModifyBusinessActivity extends AppCompatActivity implements OnCardImageRemove, OSCreateLocationDialog.OnLocationResponse {

    private final String TAG = ModifyBusinessActivity.class.getName();
    private final int RC_INTENT_VERIFY_MOBILE_NUMBER = 2;
    private final int RC_SHIPPING_CHARGE = 101;
    boolean mVerifiedNo = false;
    private ListItemAdapter mAdapter;
    private List<ListItem> mImageUris;
    private BusinessV6 mOBuss;
    private IvEditBusinessInfoBinding infoV;
    private IvEditBusinessContactBinding contactV;
    private IvEditBusinessMoreBinding moreV;
    private ActivityCreateShopBinding binding;

    private UserOSB mCurrentUser;
    private BusinessV6 mMBuss = new BusinessV6();
    private final TextWatcher mOnTextChanges = new TextWatcher() {
        @Override
        @SuppressWarnings("ConstantConditions")
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            int hashCode = s.hashCode();
            try {
                if (infoV.nameTiet.getText().hashCode() == hashCode) {
                    infoV.nameTil.setErrorEnabled(false);
                } else if (contactV.mobileNoTiet.getText().hashCode() == hashCode) {
                    contactV.mobileNoTil.setErrorEnabled(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int code = s.hashCode();
            String value = s.toString();

            try {
                if (code == infoV.nameTiet.getText().hashCode()) {
                    mMBuss.setDisplayName(value);
                } else if (code == infoV.typeActv.getText().hashCode()) {
                    mMBuss.setBusinessType(value);
                } else if (code == contactV.mobileNoTiet.getText().hashCode()) {
                    mMBuss.setMobileNumber(value);
                    mVerifiedNo = value.equals(mOBuss.getMobileNumber());
                } else if (code == contactV.emailTiet.getText().hashCode()) {
                    mMBuss.setEmail(value);
                } else if (code == contactV.websiteTiet.getText().hashCode()) {
                    mMBuss.setWebsite(value);
                } else if (code == moreV.minOrderTiet.getText().hashCode()) {
                    mMBuss.setMinOrder(Long.parseLong(value));
                } else if (code == moreV.deliveryRangeValue.getText().hashCode()) {
                    mMBuss.setDeliveryRange(Long.valueOf(value));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private LoadingBounceDialog loadingDialog;
    private HashSet<String> mSelectedOpeningDays = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateShopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.tbarAsc);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initiateUI();
        getBusinessType();

        OSPreferences preferences = OSPreferences.getInstance(this.getApplicationContext());
        mCurrentUser = preferences.getObject(OSPreferenceKey.USER, UserOSB.class);
        mOBuss = preferences.getObject(OSPreferenceKey.BUSINESS, BusinessV6.class);

        if (mOBuss != null) {
            getSupportActionBar().setTitle("Edit Business");
            mMBuss = preferences.getObject(OSPreferenceKey.BUSINESS, BusinessV6.class);
            if (mMBuss.getMobileNumber() != null) mVerifiedNo = true;
            setUpUiFromBusiness();
        } else if (mCurrentUser != null) {
            setUpUiFromUser();
        }
    }

    // start: OnClicks
    void onClickedSelectOpeningTime(View view) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            String opensAt = selectedHour + ":" + selectedMinute;
            mMBuss.setOpensAt(opensAt);
            OSTimeUtils.extractTime(opensAt, (hr, min, zone) -> moreV.openingTimeTV.setText(String.format(Locale.ENGLISH, "%s:%s %s", hr, min, zone)));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        TextView titleTV = new TextView(this);
        titleTV.setGravity(Gravity.CENTER);
        titleTV.setTextColor(Color.WHITE);
        titleTV.setBackgroundColor(getColor(R.color.colorAccent));
        titleTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f);
        titleTV.setPadding(20, 20, 20, 20);
        titleTV.setText(Html.fromHtml("<b>Select opening time</b>"));

        timePickerDialog.setCustomTitle(titleTV);
        timePickerDialog.show();
    }

    void onClickedSelectClosingTime(View view) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            String closesAt = selectedHour + ":" + selectedMinute;
            mMBuss.setClosesAt(closesAt);
            OSTimeUtils.extractTime(closesAt, (hr, min, zone) -> moreV.closingTimeTv.setText(String.format(Locale.ENGLISH, "%s:%s %s", hr, min, zone)));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        TextView titleTV = new TextView(this);
        titleTV.setGravity(Gravity.CENTER);
        titleTV.setTextColor(Color.WHITE);
        titleTV.setBackgroundColor(getColor(R.color.colorAccent));
        titleTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f);
        titleTV.setPadding(20, 20, 20, 20);
        titleTV.setText(Html.fromHtml("<b>Select closing time</b>"));

        timePickerDialog.setCustomTitle(titleTV);
        timePickerDialog.show();
    }

    void onClickedSelectDays(View view) {
        String title = "Choose opening days";
        String[] weekDays = new WeekDayHelper().getWeekDays();
        HashSet<String> newSet = (HashSet<String>) mSelectedOpeningDays.clone();

        boolean[] checkedItem = new boolean[weekDays.length];
        for (int i = 0; i < weekDays.length; i++)
            if (mSelectedOpeningDays.contains(weekDays[i])) checkedItem[i] = true;

        new AlertDialog.Builder(this).setTitle(title).setMultiChoiceItems(weekDays, checkedItem, (dialog, which, isChecked) -> {
            if (isChecked) newSet.add(weekDays[which]);
            else newSet.remove(weekDays[which]);
        }).setPositiveButton("OK", (dialog, which) -> {
            mSelectedOpeningDays = newSet;
            mMBuss.setOpeningDays(new WeekDayHelper().getWeekDaysCode(new ArrayList<>(newSet).toArray(new String[0])));
        }).setNegativeButton("Cancel", null).create().show();
    }

    void onCheckedHod(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            OSViewAnimation.expand(moreV.hodContent);
            if (mMBuss.getMinOrder() != null)
                moreV.minOrderTiet.setText(String.valueOf(mMBuss.getMinOrder()));
            if (mMBuss.getDeliveryRange() != null)
                moreV.deliveryRangeValue.setText(String.valueOf(mMBuss.getDeliveryRange()));
        } else OSViewAnimation.collapse(moreV.hodContent);
        mMBuss.setHodAvailable(isChecked);
    }

    void onClickedHodInfo(View view) {
        String msg = "With your delivery service, you can use <b>OnSpot</b> as online retailers to sell your product to your customers. Even if you don't have delivery services you can still use <b>OnSpot</b> to showcase your product online.";
        new AlertDialog.Builder(this).setTitle("Home Delivery").setMessage(Html.fromHtml(msg)).setPositiveButton("Got It", null).show();
    }

    void onCheckFreeShipping(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) moreV.shippingCharge.setVisibility(View.INVISIBLE);
        else moreV.shippingCharge.setVisibility(View.VISIBLE);
        mMBuss.setFsAvailable(isChecked);
    }

    private void onClickedShippingCharge(View view) {
        String dRange = moreV.deliveryRangeValue.getText().toString().trim();
        if (TextUtils.isEmpty(dRange)) {
            moreV.deliveryRange.setError("Invalid!!");
            return;
        }

        Intent intent = new Intent(this, DeliveryChargeActivity.class);
        if (mMBuss.getShippingCharges() != null)
            intent.putExtra(DeliveryChargeActivity.PRE_DELIVERY_CHARGE, new Gson().toJson(mMBuss.getShippingCharges()));
        startActivityForResult(intent, RC_SHIPPING_CHARGE);
    }

    void onClickedPassiveOpenInfo(View view) {
        String title = "Passive Open";
        String message = "Enabling passive open will allow customers to place order even when the business is not actively open. You can accept the order later to deliver.";
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("GOT IT", null).create().show();
    }

    void showPlacePicker(View view) {
        OSCreateLocationDialog dialog = new OSCreateLocationDialog();
        if (mMBuss.getLocation() != null) {
            Bundle bundle = new Bundle();
            bundle.putString(OSCreateLocationDialog.KEY_LOCATION, new Gson().toJson(mMBuss.getLocation()));
            dialog.setArguments(bundle);
        }
        dialog.show(getSupportFragmentManager(), "");
    }
    // end: onClicks

    @SuppressWarnings("ConstantConditions")
    void onClickedSubmit(View btnView) {
        if (TextUtils.isEmpty(infoV.nameTiet.getText().toString().trim())) {
            infoV.nameTil.setError("Input require");
            return;
        }
        /*if (TextUtils.isEmpty(infoV.idTiet.getText().toString().trim())) {
            infoV.idTil.setError("Input require");
            return;
        }*/
        if (TextUtils.isEmpty(contactV.mobileNoTiet.getText().toString().trim())) {
            contactV.mobileNoTil.setError("Input require");
            return;
        }
        if (!mVerifiedNo) {
            verifyMobileNumber(mMBuss.getMobileNumber());
            return;
        }
        if (TextUtils.isEmpty(mMBuss.getOpensAt()) || !OSTimeUtils.isValidTime(mMBuss.getOpensAt())) {
            showToast("Select opening time");
            moreV.openingTimeBtn.performClick();
            return;
        }
        if (TextUtils.isEmpty(mMBuss.getClosesAt()) || !OSTimeUtils.isValidTime(mMBuss.getClosesAt())) {
            showToast("Select closing time");
            moreV.closingTimeBtn.performClick();
            return;
        }

        if (OSTimeUtils.isFirstBugger(mMBuss.getOpensAt(), mMBuss.getClosesAt())) {
            showToast("Invalid opening or closing time");
            return;
        }

        if (mSelectedOpeningDays == null || mSelectedOpeningDays.isEmpty()) {
            moreV.selectDaysBtn.performClick();
            showToast("Select opening days");
            return;
        }

        String minOrder = moreV.minOrderTiet.getText().toString().trim();
        String deliveryRange = moreV.deliveryRangeValue.getText().toString().trim();
        if (moreV.hod.isChecked()) {
            if (!moreV.freeShipping.isChecked()) {
                if (mMBuss.getShippingCharges() == null || mMBuss.getShippingCharges().getPerOrder() == null) {
                    new AlertDialog.Builder(this).setTitle("Set shipping charge")
                            .setMessage("Shipping charge is require if your business don't provide free shipping")
                            .setPositiveButton("Set Charge", ((dialog, which) -> moreV.shippingCharge.performClick()))
                            .show();
                    return;
                }
            }

            if (TextUtils.isEmpty(minOrder)) mMBuss.setMinOrder(0L);
            if (TextUtils.isEmpty(deliveryRange)) {
                moreV.deliveryRange.setError("Invalid input");
                return;
            }
        }
        if (mMBuss.getLocation() == null) {
            showToast("Add business location.");
            return;
        }
        if (mImageUris == null || mImageUris.size() == 0) {
            String message = "At least add an image";
            OSMessage.showAIBar(this, message, "Add image", v -> new OSImagePicker(this, OSImagePicker.RC_SELECT_MULTIPLE_IMAGES).fromGallery());
            return;
        }

        if (mImageUris.size() > 5) {
            OSMessage.showLToast(this, "Maximum 5 images can add in a business profile");
            return;
        }

        uploadBusiness();
    }

    private void initiateUI() {
        infoV = binding.includeAcsInfo;
        contactV = binding.includeAcsContact;
        moreV = binding.includeAcsMore;

        infoV.nameTiet.addTextChangedListener(mOnTextChanges);
        infoV.typeActv.addTextChangedListener(mOnTextChanges);
        contactV.mobileNoTiet.addTextChangedListener(mOnTextChanges);
        contactV.emailTiet.addTextChangedListener(mOnTextChanges);
        contactV.websiteTiet.addTextChangedListener(mOnTextChanges);
        moreV.minOrderTiet.addTextChangedListener(mOnTextChanges);
        moreV.deliveryRangeValue.addTextChangedListener(mOnTextChanges);

        moreV.openingTimeBtn.setOnClickListener(this::onClickedSelectOpeningTime);
        moreV.closingTimeBtn.setOnClickListener(this::onClickedSelectClosingTime);
        moreV.selectDaysBtn.setOnClickListener(this::onClickedSelectDays);
        moreV.hod.setOnCheckedChangeListener(this::onCheckedHod);
        moreV.hodInfoIv.setOnClickListener(this::onClickedHodInfo);
        moreV.freeShipping.setOnCheckedChangeListener(this::onCheckFreeShipping);
        moreV.shippingCharge.setOnClickListener(this::onClickedShippingCharge);
        binding.selectLocationBtn.setOnClickListener(this::showPlacePicker);
        binding.submitBtn.setOnClickListener(this::onClickedSubmit);

        RecyclerView mRecyclerView = findViewById(R.id.image_rv);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mImageUris = new ArrayList<>();
        mAdapter = new ListItemAdapter(this, mImageUris);
        mRecyclerView.setAdapter(mAdapter);
        loadingDialog = new LoadingBounceDialog(this);
    }

    private void setUpUiFromBusiness() {
        List<String> urls = mOBuss.getImageUrls();
        if (urls != null && !urls.isEmpty()) {
            for (String url : urls)
                mImageUris.add(new MenuItemImage(url, MenuItemImage.SOURCE_SERVER));
            mAdapter.notifyDataSetChanged();
        }

        infoV.nameTiet.setText(mOBuss.getDisplayName());
        infoV.typeActv.setText(mOBuss.getBusinessType());
        contactV.mobileNoTiet.setText(mOBuss.getMobileNumber());
        contactV.emailTiet.setText(mOBuss.getEmail());
        contactV.websiteTiet.setText(mOBuss.getWebsite());

        if (OSTimeUtils.isValidTime(mOBuss.getOpensAt())) {
            OSTimeUtils.extractTime(mOBuss.getOpensAt(), (hr, min, zone) ->
                    moreV.openingTimeTV.setText(String.format("%s:%s %s", hr, min, zone)));
        }
        if (OSTimeUtils.isValidTime(mOBuss.getClosesAt())) {
            OSTimeUtils.extractTime(mOBuss.getClosesAt(), (hr, min, zone) ->
                    moreV.closingTimeTv.setText(String.format("%s:%s %s", hr, min, zone)));
        }

        if (mOBuss.getOpeningDays() != null)
            mSelectedOpeningDays = new HashSet<>(Arrays.asList(new WeekDayHelper().decodeDays(mOBuss.getOpeningDays())));

        if (mOBuss.getHodAvailable() != null && mOBuss.getHodAvailable()) {
            moreV.hod.setChecked(mOBuss.getHodAvailable());
            moreV.minOrderTiet.setText(String.format("%s", mOBuss.getMinOrder()));
            moreV.deliveryRangeValue.setText(String.format("%s", mOBuss.getDeliveryRange()));

            if (mOBuss.getFsAvailable() != null)
                moreV.freeShipping.setChecked(mOBuss.getFsAvailable());
        }

        OSLocation location = mOBuss.getLocation();
        if (location != null) setUpLocation(location);
    }

    private void setUpUiFromUser() {
        contactV.emailTiet.setText(mCurrentUser.getEmail());
        contactV.mobileNoTiet.setText(mCurrentUser.getPhoneNumber());
    }

    private void setUpLocation(OSLocation location) {
        binding.selectLocationBtn.setText("Change location");
        String howToReach = location.getHowToReach() == null ? "" : "\n\n" + location.getHowToReach();
        binding.selectedLocationTv.setText(String.format("%s%s", location.getAddressLine(), howToReach));
    }

    private void getBusinessType() {
        FirebaseFirestore.getInstance().collection(getString(R.string.ref_crown_onspot)).document(getString(R.string.doc_business_type)).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (ModifyBusinessActivity.this.isFinishing()) return;
                ArrayList<String> businessTypes = (ArrayList<String>) documentSnapshot.get(getString(R.string.field_business_type));
                if (businessTypes == null) return;
                Collections.sort(businessTypes, (String::compareToIgnoreCase));
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ModifyBusinessActivity.this, R.layout.dropdown_menu_popup_item, businessTypes);
                infoV.typeActv.setAdapter(adapter);
            }
        }).addOnFailureListener(e -> Log.v(TAG, "Error: " + e));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_add_or_create_business, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.nav_omaocb_select_image: {
                new OSImagePicker(this, OSImagePicker.RC_SELECT_MULTIPLE_IMAGES).fromGallery();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SHIPPING_CHARGE: {
                if (resultCode == RESULT_OK && data != null) {
                    String deliveryCharge = data.getStringExtra(DeliveryChargeActivity.DELIVERY_CHARGE);
                    mMBuss.setShippingCharges(new Gson().fromJson(deliveryCharge, OSShippingCharge.class));
                }
                break;
            }
            case RC_INTENT_VERIFY_MOBILE_NUMBER: {
                if (resultCode == RESULT_OK && data != null) {
                    mVerifiedNo = true;
                    mMBuss.setMobileNumber(data.getStringExtra(PhoneVerificationActivity.KEY_PHONE_NO));
                    binding.submitBtn.performClick();
                }
                break;
            }
            case OSImagePicker.RC_SELECT_MULTIPLE_IMAGES: {
                if (resultCode == RESULT_OK && data != null && data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        mImageUris.add(new MenuItemImage(imageUri, MenuItemImage.SOURCE_DEVICE));
                    }
                    Toast.makeText(this, count + " images selected", Toast.LENGTH_SHORT).show();
                    mAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
    }

    private void uploadBusiness() {
        loadingDialog.show();
        CollectionReference ref = FirebaseFirestore.getInstance().collection(this.getString(R.string.ref_business));
        if (mOBuss == null) {
            ref.add(mMBuss).addOnSuccessListener(doc -> onUploadSuccess(doc.getId())).addOnFailureListener(this::onUploadFailed);
        } else {
            ref.document(mOBuss.getBusinessRefId()).set(mMBuss, SetOptions.merge())
                    .addOnSuccessListener(v -> onUploadSuccess(mOBuss.getBusinessRefId())).addOnFailureListener(this::onUploadFailed);
        }
    }

    private void onUploadSuccess(String bussRefId) {
        loadingDialog.dismiss();
        uploadItemImages(bussRefId);
        Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    private void onUploadFailed(Exception e) {
        loadingDialog.dismiss();
        Toast.makeText(this, "Failed to upload!!", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Upload failed: " + e.getMessage());
        e.printStackTrace();
    }

    private void verifyMobileNumber(String mobileNumber) {
        Intent intent = new Intent(this, PhoneVerificationActivity.class);
        intent.putExtra(PhoneVerificationActivity.KEY_PHONE_NO, mobileNumber.replace("+91", ""));
        startActivityForResult(intent, RC_INTENT_VERIFY_MOBILE_NUMBER);
    }

    @Override
    public void onCardImageRemove(MenuItemImage menuItemImage) {
        if (menuItemImage.getImageSource() == MenuItemImage.SOURCE_SERVER) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(menuItemImage.getImage().toString());

            FirebaseFirestore.getInstance().collection(getString(R.string.ref_business))
                    .document(mOBuss.getBusinessRefId())
                    .update(getString(R.string.field_image_urls), FieldValue.arrayRemove(menuItemImage.getImage().toString()))
                    .addOnSuccessListener(aVoid -> {
                        mImageUris.remove(menuItemImage);
                        mAdapter.notifyDataSetChanged();
                        ref.delete();
                    })
                    .addOnFailureListener(error -> Toast.makeText(this, "Failed to remove.", Toast.LENGTH_SHORT).show());
        } else if (menuItemImage.getImageSource() == MenuItemImage.SOURCE_DEVICE) {
            mImageUris.remove(menuItemImage);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    /**
     * Upload item image to the {@link FirebaseStorage}
     *
     * @param businessRefId Reference ID of the business which the item belongs to
     */
    private void uploadItemImages(String businessRefId) {
        UserOSB user = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class);

        for (MenuItemImage menuImage : ((List<MenuItemImage>) (List<?>) mImageUris)) {
            Log.v(TAG, "image: " + menuImage.getImage().toString());

            if (menuImage.getImageSource() == MenuItemImage.SOURCE_SERVER) continue;
            try {
                StorageReference sRef = FirebaseStorage.getInstance().getReference().child(getString(R.string.sref_business_profile)).child(businessRefId);
                final StorageReference imageSRef = sRef.child(businessRefId + "-" + user.getUserId() + "-" + new Date().getTime() + "-" + getFileNameFromUri((Uri) menuImage.getImage()));

                ImageCompression compression = new ImageCompression(this, (Uri) menuImage.getImage());
                File image = compression.compress();

                UploadTask uploadTask = imageSRef.putFile(Uri.fromFile(image));
                uploadTask.addOnProgressListener(taskSnapshot -> {
                    // mLoadingPBar.setVisibility(View.VISIBLE);
                }).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return null;
                    }
                    return imageSRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            Log.v(TAG, downloadUri.toString());
                            addImageUrlToItem(businessRefId, downloadUri.toString());
                        }
                    }
                    // mLoadingPBar.setVisibility(View.INVISIBLE);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addImageUrlToItem(String refId, String url) {
        FirebaseFirestore.getInstance().collection(getString(R.string.ref_business))
                .document(refId).update("imageUrls", FieldValue.arrayUnion(url))
                .addOnSuccessListener(obj -> Log.v(TAG, "Image added: " + url))
                .addOnFailureListener(e -> Log.v(TAG, e.toString()));
    }


    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    @Override
    public void onLocationResponse(OSLocation location) {
        mMBuss.setLocation(location);
        setUpLocation(location);
    }
}
