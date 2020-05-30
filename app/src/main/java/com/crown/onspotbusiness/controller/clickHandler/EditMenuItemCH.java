package com.crown.onspotbusiness.controller.clickHandler;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.AppController;
import com.crown.onspotbusiness.model.MenuItem;
import com.crown.onspotbusiness.model.MenuItemImage;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.page.CreateOrEditBusinessItemActivity;
import com.crown.onspotbusiness.utils.ImagePicker;
import com.crown.onspotbusiness.utils.InputFilterMinMax;
import com.crown.onspotbusiness.utils.MessageUtils;
import com.crown.onspotbusiness.utils.abstracts.ListItem;
import com.crown.onspotbusiness.utils.compression.ImageCompression;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditMenuItemCH implements View.OnClickListener {

    private final String TAG = EditMenuItemCH.class.getName();
    TextInputEditText mDescriptionTIEL;
    private Activity mActivity;
    private Button mDiscountTypeBtn;
    private TextInputLayout mItemNameTIL;
    private TextInputLayout mPriceTIL;
    private TextInputLayout mItemCategoryTIL;
    private TextInputLayout mDiscountValueTIL;
    private TextInputLayout mTaxTIL;
    private TextInputEditText mDiscountValueTIET;
    private MenuItem menuItem;
    private AlertDialog mLoadingDialog;

    public EditMenuItemCH(Activity activity) {
        this.mActivity = activity;
        View dialogView = LayoutInflater.from(mActivity).inflate(R.layout.dialog_loading, null);
        mLoadingDialog = new AlertDialog.Builder(mActivity, R.style.LoadingDialogTheme).setView(dialogView).setCancelable(false).create();
        setUpViews();
    }

    public EditMenuItemCH(Activity activity, MenuItem item) {
        this(activity);
        this.menuItem = item;
        setUpViews();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_aami_cancel: {
                onClickedCancel();
                break;
            }
            case R.id.btn_aami_submit: {
                onClickedSubmit();
                break;
            }
            case R.id.btn_aami_discount_type: {
                onClickedDiscountType(view);
                break;
            }
        }
    }

    private void onClickedDiscountType(View view) {
        PopupMenu menu = new PopupMenu(mActivity, view);
        menu.getMenu().add(MenuItem.Discount.NO_DISCOUNT.toString().replace("_", " "));
        menu.getMenu().add(MenuItem.Discount.PERCENT.toString());
        menu.getMenu().add(MenuItem.Discount.PRICE.toString());

        menu.setOnMenuItemClickListener(item -> {
            mDiscountValueTIET.getText().clear();
            mDiscountTypeBtn.setText(item.getTitle());
            String discountType = item.getTitle().toString().replace(" ", "_");
            if (discountType.equals(MenuItem.Discount.NO_DISCOUNT.toString())) {
                mDiscountValueTIET.setEnabled(false);
            } else {
                mDiscountValueTIET.setEnabled(true);
            }

            if (discountType.equals(MenuItem.Discount.PERCENT.toString())) {
                mDiscountValueTIET.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
            } else {
                mDiscountValueTIET.setFilters(new InputFilter[]{});
            }
            return true;
        });
        menu.show();
    }

    private void setUpViews() {
        mDiscountTypeBtn = mActivity.findViewById(R.id.btn_aami_discount_type);
        mItemNameTIL = mActivity.findViewById(R.id.til_aami_item_name);
        mItemCategoryTIL = mActivity.findViewById(R.id.til_aami_categories);
        mPriceTIL = mActivity.findViewById(R.id.til_aami_price);
        mDiscountValueTIL = mActivity.findViewById(R.id.til_aami_discount_value);
        mTaxTIL = mActivity.findViewById(R.id.til_aami_tax);
        mDiscountValueTIET = mActivity.findViewById(R.id.tiet_aami_discount_value);
        mDescriptionTIEL = mActivity.findViewById(R.id.tiet_aami_description);
    }

    private void onClickedCancel() {
        mActivity.onBackPressed();
    }

    private void onClickedSubmit() {
        String itemName = mItemNameTIL.getEditText().getText().toString().trim();
        String priceStr = mPriceTIL.getEditText().getText().toString().trim();
        String discountValueStr = mDiscountValueTIL.getEditText().getText().toString().trim();
        String taxStr = mTaxTIL.getEditText().getText().toString().trim();
        String itemCategory = mItemCategoryTIL.getEditText().getText().toString();

        if (TextUtils.isEmpty(itemName)) {
            mItemNameTIL.setError("Input require");
            return;
        }

        if (itemCategory.length() > 30) {
            mItemCategoryTIL.setError("Invalid Input");
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            mPriceTIL.setError("Input require");
            return;
        }

        List<ListItem> images = ((CreateOrEditBusinessItemActivity) mActivity).getImageUris();
        if (images == null || images.size() == 0) {
            String message = "At least add an image";
            MessageUtils.showActionIndefiniteSnackBar(mActivity.findViewById(android.R.id.content), message, "ADD IMAGE", 0, (view, requestCode) -> {
                new ImagePicker(mActivity, ImagePicker.RC_SELECT_MULTIPLE_IMAGES).fromGallery();
            });
            return;
        }

        double price = Double.parseDouble(priceStr);
        double discountValue = 0;
        double tax = 0;

        String discountType = mDiscountTypeBtn.getText().toString().replace(" ", "_");
        if (!discountType.equals(MenuItem.Discount.NO_DISCOUNT.toString())) {
            if (TextUtils.isEmpty(discountValueStr)) {
                mDiscountValueTIL.setError("Input require");
                return;
            }
            discountValue = Double.parseDouble(discountValueStr);
            if (discountType.equals(MenuItem.Discount.PRICE.toString())) {
                if (discountValue > price) {
                    mDiscountValueTIL.setError("Discount can't be higher than the actual price");
                    return;
                }
            }
        }

        if (!TextUtils.isEmpty(taxStr)) {
            tax = Double.parseDouble(taxStr);
        }

        Map<String, Object> param = new HashMap<>();
        param.put("itemName", itemName);
        param.put("category", itemCategory);
        param.put("price", price);
        param.put("discountType", discountType);
        param.put("discountValue", discountValue);
        param.put("tax", tax);
        param.put("description", mDescriptionTIEL.getText().toString());
        param.put("businessRefId", Preferences.getInstance(mActivity.getApplicationContext()).getObject(PreferenceKey.USER, User.class).getBusinessRefId());

        if (menuItem != null) {
            updateMenuItem(param);
        } else {
            createMenuItem(param);
        }
    }

    private void updateMenuItem(Map<String, Object> param) {
        mLoadingDialog.show();
        FirebaseFirestore.getInstance().collection(mActivity.getString(R.string.ref_item))
                .document(menuItem.getItemId()).update(param)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uploadItemImages(menuItem.getItemId());
                        mLoadingDialog.dismiss();
                        Toast.makeText(mActivity, "Uploading...", Toast.LENGTH_SHORT).show();
                        mActivity.onBackPressed();
                    } else {
                        Log.v(TAG, "Error: " + task.getException());
                    }
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(mActivity, "Failed to upload", Toast.LENGTH_SHORT).show();
                    mLoadingDialog.dismiss();
                });
    }

    private void createMenuItem(final Map<String, Object> param) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mLoadingDialog.show();

        db.collection(mActivity.getResources().getString(R.string.ref_item)).add(param)
                .addOnSuccessListener(reference -> {
                    uploadItemImages(reference.getId());
                    mLoadingDialog.dismiss();
                    Toast.makeText(mActivity, "Uploading...", Toast.LENGTH_SHORT).show();
                    mActivity.onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(mActivity, "Failed to upload", Toast.LENGTH_SHORT).show();
                    mLoadingDialog.dismiss();
                });
    }

    private void uploadItemImages(String refId) {
        List<MenuItemImage> imageUris = (List<MenuItemImage>) (List<?>) ((CreateOrEditBusinessItemActivity) mActivity).getImageUris();

        String businessRefId = Preferences.getInstance(mActivity.getApplicationContext()).getObject(PreferenceKey.USER, User.class).getBusinessRefId();
        String userId = AppController.getInstance().getFirebaseAuth().getUid();

        for (MenuItemImage menuImage : imageUris) {
            Log.v(TAG, "image: " + menuImage.getImage().toString());

            if (menuImage.getImageSource() == MenuItemImage.SOURCE_SERVER) continue;
            try {
                String storageRef = mActivity.getResources().getString(R.string.ref_storage_item_image);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(storageRef);
                final StorageReference imageStorageReference = storageReference.child(businessRefId + "_" + userId + "_" + new Date().getTime() + "_" + getFileNameFromUri((Uri) menuImage.getImage()));

                ImageCompression compression = new ImageCompression(mActivity, (Uri) menuImage.getImage());
                File image = compression.compress();

                UploadTask uploadTask = imageStorageReference.putFile(Uri.fromFile(image));
                uploadTask.addOnProgressListener(taskSnapshot -> {

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
                            addImageUrlToItem(refId, downloadUri.toString());
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addImageUrlToItem(String refId, String url) {
        String menuSubCollection = mActivity.getResources().getString(R.string.ref_item);
        FirebaseFirestore.getInstance().collection(menuSubCollection)
                .document(refId).update("imageUrls", FieldValue.arrayUnion(url))
                .addOnSuccessListener(obj -> Log.v(TAG, "Image added: " + url))
                .addOnFailureListener(e -> Log.v(TAG, e.toString()));
    }


    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = mActivity.getContentResolver().query(uri, null, null, null, null)) {
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
