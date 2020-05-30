package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.clickHandler.EditMenuItemCH;
import com.crown.onspotbusiness.model.MenuItem;
import com.crown.onspotbusiness.model.MenuItemImage;
import com.crown.onspotbusiness.utils.ImagePicker;
import com.crown.onspotbusiness.utils.MockData;
import com.crown.onspotbusiness.utils.abstracts.ListItem;
import com.crown.onspotbusiness.utils.abstracts.OnCardImageRemove;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateOrEditBusinessItemActivity extends AppCompatActivity implements TextWatcher, OnCardImageRemove {

    public static final String KEY_MENU_ITEM = "MENU_ITEM";
    private static final String TAG = CreateOrEditBusinessItemActivity.class.getName();
    private ListItemAdapter mAdapter;

    private TextInputLayout mItemNameTIL;
    private TextInputLayout mPriceTIL;
    private TextInputLayout mDiscountValueTIL;
    private TextInputLayout mItemCategoryTIL;

    private TextInputEditText mItemNameTIET;
    private TextInputEditText mPriceTIET;
    private TextInputEditText mDiscountValueTIET;
    private TextInputEditText mTaxTIET;
    private AutoCompleteTextView mItemCategoryACTV;
    private Button mDiscountTypeBtn;

    private List<ListItem> mImageUris;
    private MenuItem item;
    private boolean hasEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_menu_item);

        Toolbar toolbar = findViewById(R.id.tbar_fm_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String json = getIntent().getStringExtra(KEY_MENU_ITEM);
        if (json != null) {
            item = new Gson().fromJson(json, MenuItem.class);
            hasEditMode = true;
            getSupportActionBar().setTitle("Edit Item");
        }
        setUpRecycler();
        initiateUI();
        if (hasEditMode) setUpUi();
        // getCategory();
    }

    private void setUpRecycler() {
        RecyclerView mRecyclerView = findViewById(R.id.rv_aami_card_image);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mImageUris = new ArrayList<>();
        mAdapter = new ListItemAdapter(this, mImageUris);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setUpUi() {
        List<String> urls = item.getImageUrls();
        if (urls != null && !urls.isEmpty()) {
            for (String url : urls)
                mImageUris.add(new MenuItemImage(url, MenuItemImage.SOURCE_SERVER));
            mAdapter.notifyDataSetChanged();
        }
        mItemNameTIET.setText(item.getItemName());
        mItemCategoryACTV.setText(item.getCategory());
        mPriceTIET.setText(String.format(Locale.ENGLISH, "%d", item.getPrice()));
        mDiscountValueTIET.setText(String.format(Locale.ENGLISH, "%d", item.getDiscountValue()));
        mTaxTIET.setText(String.format(Locale.ENGLISH, "%d", item.getTax()));

        MenuItem.Discount discount = item.getDiscountType();
        mDiscountTypeBtn.setText(discount.toString().replace("_", " "));
        if (discount != MenuItem.Discount.NO_DISCOUNT) {
            mDiscountValueTIET.setEnabled(true);
        }
    }

    private void getCategory() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, MockData.categories());
        mItemCategoryACTV.setAdapter(adapter);
    }

    public MenuItem getItem() {
        return this.item;
    }

    private void initiateUI() {
        mDiscountTypeBtn = findViewById(R.id.btn_aami_discount_type);
        mItemNameTIL = findViewById(R.id.til_aami_item_name);
        mPriceTIL = findViewById(R.id.til_aami_price);
        mDiscountValueTIL = findViewById(R.id.til_aami_discount_value);
        mItemNameTIET = findViewById(R.id.tiet_aami_item_name);
        mPriceTIET = findViewById(R.id.tiet_aami_price);
        mDiscountValueTIET = findViewById(R.id.tiet_aami_discount_value);
        mItemCategoryTIL = findViewById(R.id.til_aami_categories);
        mItemCategoryACTV = findViewById(R.id.actv_aami_categories);
        mTaxTIET = findViewById(R.id.tiet_aami_tax);

        EditMenuItemCH mClickHandler = new EditMenuItemCH(this, item);
        mDiscountTypeBtn.setOnClickListener(mClickHandler);
        findViewById(R.id.btn_aami_cancel).setOnClickListener(mClickHandler);
        findViewById(R.id.btn_aami_submit).setOnClickListener(mClickHandler);
        findViewById(R.id.btn_aami_discount_type).setOnClickListener(mClickHandler);

        mItemNameTIET.addTextChangedListener(this);
        mPriceTIET.addTextChangedListener(this);
        mDiscountValueTIET.addTextChangedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_add_or_create_business, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.nav_omaocb_select_image:
                new ImagePicker(this, ImagePicker.RC_SELECT_MULTIPLE_IMAGES).fromGallery();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.RC_SELECT_MULTIPLE_IMAGES && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // mImageUris.clear();
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    mImageUris.add(new MenuItemImage(imageUri, MenuItemImage.SOURCE_DEVICE));
                }
                Toast.makeText(this, count + " images selected", Toast.LENGTH_SHORT).show();
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public List<ListItem> getImageUris() {
        return mImageUris;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        int hashCode = s.hashCode();
        if (mItemNameTIET.getText().hashCode() == hashCode) {
            mItemNameTIL.setErrorEnabled(false);
        } else if (mPriceTIET.getText().hashCode() == hashCode) {
            mPriceTIL.setErrorEnabled(false);
        } else if (mDiscountValueTIET.getText().hashCode() == hashCode) {
            mDiscountValueTIL.setErrorEnabled(false);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onCardImageRemove(MenuItemImage menuItemImage) {
        if (menuItemImage.getImageSource() == MenuItemImage.SOURCE_SERVER) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(menuItemImage.getImage().toString());

            FirebaseFirestore.getInstance().collection(getString(R.string.ref_item))
                    .document(item.getItemId())
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
}