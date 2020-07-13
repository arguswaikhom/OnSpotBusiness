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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.Validate;
import com.crown.onspotbusiness.model.Business;
import com.crown.onspotbusiness.model.Location;
import com.crown.onspotbusiness.model.MenuItemImage;
import com.crown.onspotbusiness.model.Time;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.utils.HttpVolleyRequest;
import com.crown.onspotbusiness.utils.ImagePicker;
import com.crown.onspotbusiness.utils.JsonParse;
import com.crown.onspotbusiness.utils.MessageUtils;
import com.crown.onspotbusiness.utils.WeekDayHelper;
import com.crown.onspotbusiness.utils.abstracts.OnCardImageRemove;
import com.crown.onspotbusiness.utils.abstracts.OnHttpResponse;
import com.crown.onspotbusiness.utils.compression.ImageCompression;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
import com.crown.onspotbusiness.view.CreateLocationDialog;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CreateBusinessActivity extends AppCompatActivity implements TextWatcher, OnHttpResponse, CreateLocationDialog.OnLocationDialogActionClicked, OnCardImageRemove {

    public static final String KEY_BUSINESS_ID = "BUSINESS_ID";
    public static final String KEY_BUSINESS_REF_ID = "BUSINESS_REF_ID";
    public static final String KEY_BUSINESS = "BUSINESS";
    public static final String KEY_USER = "USER";
    private final String TAG = CreateBusinessActivity.class.getName();
    private final int RC_INTENT_VERIFY_MOBILE_NUMBER = 2;
    private final int RC_NETWORK_CREATE_BUSINESS = 10;
    private final int RC_NETWORK_EDIT_BUSINESS = 100;
    /**
     * Business info section views
     */
    @BindView(R.id.til_acs_display_name)
    TextInputLayout mDisplayNameTIL;
    @BindView(R.id.til_acs_business_id)
    TextInputLayout mBusinessIdTIL;
    @BindView(R.id.til_acs_business_type)
    TextInputLayout mBusinessTypeTIL;
    @BindView(R.id.tiet_acs_display_name)
    TextInputEditText mDisplayNameTIET;
    @BindView(R.id.tiet_acs_business_id)
    TextInputEditText mBusinessIdTIET;
    @BindView(R.id.actv_acs_business_type)
    AutoCompleteTextView mBusinessTypeACTV;

    /**
     * Contact section views
     */
    @BindView(R.id.til_acs_mobile_no)
    TextInputLayout mMobileNumberTIL;
    @BindView(R.id.til_acs_email)
    TextInputLayout mEmailTIL;
    @BindView(R.id.til_acs_website)
    TextInputLayout mWebsiteTIL;
    @BindView(R.id.tiet_acs_email)
    TextInputEditText mEmailTIET;
    @BindView(R.id.tiet_acs_mobile_no)
    TextInputEditText mMobileNumberTIET;

    /**
     * More details section views
     */
    @BindView(R.id.tv_acs_opening)
    TextView mOpeningTimeTV;
    @BindView(R.id.tv_acs_closing)
    TextView mClosingTimeTV;
    @BindView(R.id.til_acs_delivery_range)
    TextInputLayout mDeliveryRangeTIL;
    @BindView(R.id.tiet_acs_delivery_range)
    TextInputEditText mDeliveryRangeTIET;
    @BindView(R.id.cb_acs_enable_passive_open)
    CheckBox mPassiveOpenCB;
    @BindView(R.id.iv_acs_passive_open_info)
    ImageView mPassiveOpenInfoIV;

    /**
     * Location section views
     */
    @BindView(R.id.btn_acs_select_location)
    Button mAddOrChangeLocationBtn;
    @BindView(R.id.tv_acs_selected_location)
    TextView mSelectedLocationTV;

    private ListItemAdapter mAdapter;
    private List<ListItem> mImageUris;
    private boolean mHasVerifiedMobileNumber = false;
    private String mVerifiedMobileNumber;
    private boolean hasEditMode = false;
    private Business updatedBusiness = new Business();
    private Business originalBusiness;
    private User user;
    private HashSet<String> mSelectedOpeningDays = new HashSet<>();
    private Time mOpeningTime;
    private Time mClosingTime;
    private AlertDialog mLoadingDialog;
    private String mOldBusinessId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shop);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.tbar_fm_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initiateUI();
        getBusinessType();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
        mLoadingDialog = new AlertDialog.Builder(this, R.style.LoadingDialogTheme).setView(dialogView).setCancelable(false).create();

        String BJson = getIntent().getStringExtra(KEY_BUSINESS);
        String UJson = getIntent().getStringExtra(KEY_USER);
        if (BJson != null) {
            originalBusiness = new Gson().fromJson(BJson, Business.class);
            updatedBusiness = new Gson().fromJson(BJson, Business.class);
            hasEditMode = true;
            getSupportActionBar().setTitle("Edit Business");
            setUpUiFromBusiness();
        } else if (UJson != null) {
            user = new Gson().fromJson(UJson, User.class);
            setUpUiFromUser();
        }
    }

    private void initiateUI() {
        mMobileNumberTIET.addTextChangedListener(this);
        mDisplayNameTIET.addTextChangedListener(this);
        mBusinessIdTIET.addTextChangedListener(this);

        RecyclerView mRecyclerView = findViewById(R.id.rv_aami_card_image);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mImageUris = new ArrayList<>();
        mAdapter = new ListItemAdapter(this, mImageUris);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setUpUiFromBusiness() {
        mDisplayNameTIET.setText(updatedBusiness.getDisplayName());
        mBusinessIdTIET.setText(updatedBusiness.getBusinessId());
        mBusinessTypeACTV.setText(updatedBusiness.getBusinessType());
        mMobileNumberTIET.setText(updatedBusiness.getMobileNumber());
        mEmailTIET.setText(updatedBusiness.getEmail());
        mWebsiteTIL.getEditText().setText(updatedBusiness.getWebsite());

        Location location = updatedBusiness.getLocation();
        if (location != null) {
            setUpLocation(location);
        }

        if (updatedBusiness.getMobileNumber() != null && !updatedBusiness.getMobileNumber().isEmpty() && Validate.isPhoneNumber(updatedBusiness.getMobileNumber())) {
            mHasVerifiedMobileNumber = true;
            mVerifiedMobileNumber = updatedBusiness.getMobileNumber();
        }

        List<String> urls = updatedBusiness.getImageUrls();
        if (urls != null && !urls.isEmpty()) {
            for (String url : urls)
                mImageUris.add(new MenuItemImage(url, MenuItemImage.SOURCE_SERVER));
            mAdapter.notifyDataSetChanged();
        }

        mOldBusinessId = updatedBusiness.getBusinessId();
        mOpeningTime = updatedBusiness.getOpeningTime();
        if (mOpeningTime != null)
            mOpeningTimeTV.setText(String.format(Locale.ENGLISH, "%d:%d %s", mOpeningTime.getHour(), mOpeningTime.getMinute(), mOpeningTime.getZone()));

        mClosingTime = updatedBusiness.getClosingTime();
        if (mClosingTime != null)
            mClosingTimeTV.setText(String.format(Locale.ENGLISH, "%d:%d %s", mClosingTime.getHour(), mClosingTime.getMinute(), mClosingTime.getZone()));

        if (updatedBusiness.getOpeningDays() != null)
            mSelectedOpeningDays = new HashSet<>(Arrays.asList(new WeekDayHelper().decodeDays(updatedBusiness.getOpeningDays())));

        if (updatedBusiness.getDeliveryRange() != null)
            mDeliveryRangeTIET.setText(String.format("%s", updatedBusiness.getDeliveryRange()));

        if (updatedBusiness.getPassiveOpenEnable() != null)
            mPassiveOpenCB.setChecked(updatedBusiness.getPassiveOpenEnable());
    }

    private void setUpUiFromUser() {
        mEmailTIET.setText(user.getEmail());
        mMobileNumberTIET.setText(user.getPhoneNumber());

        if (user.isHasPhoneNumberVerified() && user.getPhoneNumber() != null && !TextUtils.isEmpty(user.getPhoneNumber()) && Validate.isPhoneNumber(user.getPhoneNumber())) {
            mHasVerifiedMobileNumber = true;
            mVerifiedMobileNumber = user.getPhoneNumber();
        }
    }

    private void setUpLocation(Location location) {
        updatedBusiness.setLocation(location);
        mAddOrChangeLocationBtn.setText("Change location");
        String howToReach = location.getHowToReach() == null ? "" : "\n\n" + location.getHowToReach();
        mSelectedLocationTV.setText(String.format("%s%s", location.getAddressLine(), howToReach));
    }

    private void getBusinessType() {
        FirebaseFirestore.getInstance().collection(getString(R.string.ref_crown_onspot)).document(getString(R.string.doc_business_type)).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (CreateBusinessActivity.this.isFinishing()) return;
                ArrayList<String> businessTypes = (ArrayList<String>) documentSnapshot.get(getString(R.string.field_business_type));
                if (businessTypes == null) return;
                Collections.sort(businessTypes, (String::compareToIgnoreCase));
                ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateBusinessActivity.this, R.layout.dropdown_menu_popup_item, businessTypes);
                mBusinessTypeACTV.setAdapter(adapter);
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
                new ImagePicker(this, ImagePicker.RC_SELECT_MULTIPLE_IMAGES).fromGallery();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_INTENT_VERIFY_MOBILE_NUMBER: {
                if (resultCode == RESULT_OK && data != null) {
                    mHasVerifiedMobileNumber = true;
                    mVerifiedMobileNumber = data.getStringExtra(PhoneVerificationActivity.KEY_PHONE_NO);
                    createBusiness();
                }
                break;
            }
            case ImagePicker.RC_SELECT_MULTIPLE_IMAGES: {
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

    @OnClick(R.id.btn_acs_select_location)
    void showPlacePicker() {
        CreateLocationDialog dialog = new CreateLocationDialog();
        if (hasEditMode && updatedBusiness.getLocation() != null) {
            Bundle bundle = new Bundle();
            bundle.putString(CreateLocationDialog.KEY_LOCATION, new Gson().toJson(updatedBusiness.getLocation()));
            dialog.setArguments(bundle);
        }
        dialog.show(getSupportFragmentManager(), "");
    }

    @OnClick(R.id.btn_acs_opening)
    void onClickedSelectOpeningTime() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            mOpeningTime = new Time(selectedHour, selectedMinute, selectedHour > 12 ? Time.PM : Time.AM);
            mOpeningTimeTV.setText(String.format(Locale.ENGLISH, "%d:%d %s", mOpeningTime.getHour(), mOpeningTime.getMinute(), mOpeningTime.getZone()));
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

    @OnClick(R.id.btn_acs_closing)
    void onClickedSelectClosingTime() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            mClosingTime = new Time(selectedHour, selectedMinute, selectedHour > 12 ? Time.PM : Time.AM);
            mClosingTimeTV.setText(String.format(Locale.ENGLISH, "%d:%d %s", mClosingTime.getHour(), mClosingTime.getMinute(), mClosingTime.getZone()));
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

    @OnClick(R.id.btn_acs_select_days)
    void onClickedSelectDays() {
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
            // TODO: display selected days
            mSelectedOpeningDays = newSet;
        }).setNegativeButton("Cancel", null).create().show();
    }

    @OnClick(R.id.iv_acs_passive_open_info)
    void onClickedPassiveOpenInfo() {
        String title = "Passive Open";
        String message = "Enabling passive open will allow customers to place order even when the business is not actively open. You can accept the order later to deliver.";
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("GOT IT", null).create().show();
    }

    @OnClick(R.id.tv_acs_submit)
    void onClickedSubmit() {
        String displayName = mDisplayNameTIL.getEditText().getText().toString().trim();
        String businessId = mBusinessIdTIL.getEditText().getText().toString().trim();
        String businessType = mBusinessTypeTIL.getEditText().getText().toString().trim();
        String mobileNumber = mMobileNumberTIL.getEditText().getText().toString().trim();
        String email = mEmailTIL.getEditText().getText().toString().trim();
        String website = mWebsiteTIL.getEditText().getText().toString().trim();
        String deliveryRange = mDeliveryRangeTIL.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(displayName)) {
            mDisplayNameTIL.setError("Input require");
            return;
        }
        if (TextUtils.isEmpty(businessId)) {
            mBusinessIdTIL.setError("Input require");
            return;
        }
        if (TextUtils.isEmpty(mobileNumber)) {
            mMobileNumberTIL.setError("Input require");
            return;
        }
        if (mOpeningTime == null) {
            showToast("Select opening time");
            onClickedSelectOpeningTime();
            return;
        }
        if (mClosingTime == null) {
            showToast("Select closing time");
            onClickedSelectClosingTime();
            return;
        }
        if (!mClosingTime.isBuggerThan(mOpeningTime)) {
            showToast("Invalid opening or closing time");
            return;
        }
        if (mSelectedOpeningDays == null || mSelectedOpeningDays.isEmpty()) {
            onClickedSelectDays();
            showToast("Select opening days");
            return;
        }
        if (TextUtils.isEmpty(deliveryRange)) {
            mDeliveryRangeTIL.setError("Invalid input");
            return;
        }
        if (updatedBusiness.getLocation() == null) {
            showToast("Add business location.");
            return;
        }
        if (mImageUris == null || mImageUris.size() == 0) {
            String message = "At least add an image";
            MessageUtils.showActionShortSnackBar(findViewById(android.R.id.content), message, "ADD IMAGE", 0, (view, requestCode) -> {
                new ImagePicker(this, ImagePicker.RC_SELECT_MULTIPLE_IMAGES).fromGallery();
            });
            return;
        }

        Log.v(TAG, mSelectedOpeningDays.toString());

        updatedBusiness.setCreator(Preferences.getInstance(getApplicationContext()).getObject(PreferenceKey.USER, User.class).getUserId());
        updatedBusiness.setDisplayName(displayName);
        updatedBusiness.setBusinessId(businessId);
        updatedBusiness.setBusinessType(businessType);
        updatedBusiness.setMobileNumber(mobileNumber);
        updatedBusiness.setEmail(email);
        updatedBusiness.setWebsite(website);
        updatedBusiness.setOpeningTime(mOpeningTime);
        updatedBusiness.setClosingTime(mClosingTime);
        updatedBusiness.setOpeningDays(new WeekDayHelper().getWeekDaysCode(new ArrayList<>(mSelectedOpeningDays).toArray(new String[0])));
        updatedBusiness.setDeliveryRange(Double.valueOf(deliveryRange));
        updatedBusiness.setPassiveOpenEnable(mPassiveOpenCB.isChecked());

        if (mVerifiedMobileNumber != null && !mVerifiedMobileNumber.equals(mobileNumber)) {
            mHasVerifiedMobileNumber = false;
        }

        Log.d(TAG, "mHasVerifiedMobileNumber: " + mHasVerifiedMobileNumber);
        if (mHasVerifiedMobileNumber) {
            createBusiness();
        } else {
            verifyMobileNumber(mobileNumber);
        }
    }

    private void createBusiness() {
        Map<String, String> map = new HashMap<>();
        map.put("data", new Gson().toJson(updatedBusiness));

        if (hasEditMode) {
            // TODO: Override equals() and compare
            Log.v(TAG, "originalBusiness.equals(updatedBusiness): " + originalBusiness.equals(updatedBusiness));
            if (originalBusiness.equals(updatedBusiness)) return;

            mLoadingDialog.show();
            map.put("oldBusinessId", mOldBusinessId);
            map.put("oldDeliveryRange", originalBusiness.getDeliveryRange().toString());
            String url = getResources().getString(R.string.domain) + "/updateBusiness/";
            HttpVolleyRequest httpVolleyRequest = new HttpVolleyRequest(Request.Method.POST, url, null, RC_NETWORK_EDIT_BUSINESS,
                    null, map, this);
            httpVolleyRequest.execute();
        } else {
            mLoadingDialog.show();
            String url = getResources().getString(R.string.domain) + "/createBusiness/";
            HttpVolleyRequest httpVolleyRequest = new HttpVolleyRequest(Request.Method.POST, url, null, RC_NETWORK_CREATE_BUSINESS,
                    null, map, this);
            httpVolleyRequest.execute();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        int hashCode = s.hashCode();
        if (mDisplayNameTIET.getText().hashCode() == hashCode) {
            mDisplayNameTIL.setErrorEnabled(false);
        } else if (mBusinessIdTIET.getText().hashCode() == hashCode) {
            mBusinessIdTIL.setErrorEnabled(false);
        } else if (mMobileNumberTIET.getText().hashCode() == hashCode) {
            mMobileNumberTIL.setErrorEnabled(false);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mMobileNumberTIET.getText().hashCode() == s.hashCode()) {
            String no = s.toString();
            Log.v(TAG, "Phone no. " + mVerifiedMobileNumber);
            Log.v(TAG, "Phone no. " + no);
            mHasVerifiedMobileNumber = mVerifiedMobileNumber != null && mVerifiedMobileNumber.equals(s.toString());
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onHttpResponse(String response, int request) {
        Log.d(TAG, response);
        mLoadingDialog.dismiss();
        if (request == RC_NETWORK_CREATE_BUSINESS) {
            JSONObject jsonResponse = JsonParse.stringToObject(response);
            Double status = JsonParse.numberFromObject(jsonResponse, "status");
            if (status != null && status == 200) {
                uploadItemImages(JsonParse.stringFromObject(jsonResponse, "businessRefId"));

                Intent intent = new Intent();
                intent.putExtra(KEY_BUSINESS_ID, JsonParse.stringFromObject(jsonResponse, "businessId"));
                intent.putExtra(KEY_BUSINESS_REF_ID, JsonParse.stringFromObject(jsonResponse, "businessRefId"));
                setResult(RESULT_OK, intent);
                finish();
            } else if (status != null && status == 401) {
                mBusinessIdTIL.setError("Business ID is not available");
                Toast.makeText(this, "Business ID is not available.", Toast.LENGTH_SHORT).show();
            } else if (status != null && status == 204) {
                String message = getString(R.string.app_name) + " is not available in the selected area.";
                mBusinessIdTIL.setError(message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } else if (request == RC_NETWORK_EDIT_BUSINESS) {
            JSONObject jsonResponse = JsonParse.stringToObject(response);
            Double status = JsonParse.numberFromObject(jsonResponse, "status");
            if (status != null && status == 200) {
                uploadItemImages(updatedBusiness.getBusinessRefId());
                Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else if (status != null && status == 401) {
                mBusinessIdTIL.setError("Business ID is not available");
                Toast.makeText(this, "Business ID is not available.", Toast.LENGTH_SHORT).show();
            } else if (status != null && status == 204) {
                String message = getString(R.string.app_name) + " is not available in the selected area.";
                mBusinessIdTIL.setError(message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onHttpErrorResponse(VolleyError error, int request) {
        mLoadingDialog.dismiss();
        if (request == RC_NETWORK_CREATE_BUSINESS || request == RC_NETWORK_EDIT_BUSINESS) {
            Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, error.toString());
        }
    }

    private void verifyMobileNumber(String mobileNumber) {
        Intent intent = new Intent(this, PhoneVerificationActivity.class);
        intent.putExtra(PhoneVerificationActivity.KEY_PHONE_NO, mobileNumber.replace("+91", ""));
        startActivityForResult(intent, RC_INTENT_VERIFY_MOBILE_NUMBER);
    }

    @Override
    public void onLocationDialogPositiveActionClicked(Location location) {
        setUpLocation(location);
    }

    @Override
    public void onCardImageRemove(MenuItemImage menuItemImage) {
        if (menuItemImage.getImageSource() == MenuItemImage.SOURCE_SERVER) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(menuItemImage.getImage().toString());

            FirebaseFirestore.getInstance().collection(getString(R.string.ref_business))
                    .document(updatedBusiness.getBusinessRefId())
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
        String userId = Preferences.getInstance(getApplicationContext()).getObject(PreferenceKey.USER, User.class).getUserId();

        for (MenuItemImage menuImage : ((List<MenuItemImage>) (List<?>) mImageUris)) {
            Log.v(TAG, "image: " + menuImage.getImage().toString());

            if (menuImage.getImageSource() == MenuItemImage.SOURCE_SERVER) continue;
            try {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(getString(R.string.ref_storage_business_image));
                final StorageReference imageStorageReference = storageReference.child(businessRefId + "_" + userId + "_" + new Date().getTime() + "_" + getFileNameFromUri((Uri) menuImage.getImage()));

                ImageCompression compression = new ImageCompression(this, (Uri) menuImage.getImage());
                File image = compression.compress();

                UploadTask uploadTask = imageStorageReference.putFile(Uri.fromFile(image));
                uploadTask.addOnProgressListener(taskSnapshot -> {
                    // mLoadingPBar.setVisibility(View.VISIBLE);
                }).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return null;
                    }
                    return imageStorageReference.getDownloadUrl();
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
}
