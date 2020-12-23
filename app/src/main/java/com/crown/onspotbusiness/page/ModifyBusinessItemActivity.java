package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.OSDiscount;
import com.crown.library.onspotlibrary.model.OSPrice;
import com.crown.library.onspotlibrary.model.business.BusinessV6;
import com.crown.library.onspotlibrary.model.businessItem.BusinessItemOSB;
import com.crown.library.onspotlibrary.utils.BusinessItemUtils;
import com.crown.library.onspotlibrary.utils.InputFilterMinMax;
import com.crown.library.onspotlibrary.utils.OSImagePicker;
import com.crown.library.onspotlibrary.utils.OSListUtils;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.library.onspotlibrary.utils.emun.BusinessItemPriceUnit;
import com.crown.library.onspotlibrary.utils.emun.OSDiscountType;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.library.onspotlibrary.views.LoadingBounceDialog;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.ActivityModifyBusinessItemBinding;
import com.crown.onspotbusiness.model.MenuItemImage;
import com.crown.onspotbusiness.utils.abstracts.OnCardImageRemove;
import com.crown.onspotbusiness.utils.compression.ImageCompression;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ModifyBusinessItemActivity extends AppCompatActivity implements OnCardImageRemove {

    public static final String ITEM = "ITEM";
    private static final String TAG = ModifyBusinessItemActivity.class.getName();
    public ActivityModifyBusinessItemBinding binding;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        @SuppressWarnings("ConstantConditions")
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            try {
                int hashCode = s.hashCode();
                if (binding.nameTiet.getText().hashCode() == hashCode) {
                    binding.nameTil.setErrorEnabled(false);
                } else if (binding.priceTiet.getText().hashCode() == hashCode) {
                    binding.priceTil.setErrorEnabled(false);
                } else if (binding.quantityTiet.getText().hashCode() == hashCode) {
                    binding.quantityTil.setErrorEnabled(false);
                } else if (binding.discountValueTiet.getText().hashCode() == hashCode) {
                    binding.discountValueTil.setErrorEnabled(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                int hashCode = s.hashCode();
                String value = s.toString().trim();
                if (binding.nameTiet.getText().hashCode() == hashCode) {
                    updatedItem.setItemName(value);
                } else if (binding.categoriesActv.getText().hashCode() == hashCode) {
                    updatedItem.setCategory(value);
                } else if (binding.descriptionTiet.getText().hashCode() == hashCode) {
                    updatedItem.setDescription(value);
                } else if (binding.priceTiet.getText().hashCode() == hashCode) {
                    OSPrice price = updatedItem.getPrice();
                    price.setPrice(Long.valueOf(value));
                    updatedItem.setPrice(price);
                } else if (binding.quantityTiet.getText().hashCode() == hashCode) {
                    OSPrice price = updatedItem.getPrice();
                    price.setQuantity(Long.valueOf(value));
                    updatedItem.setPrice(price);
                } else if (binding.discountValueTiet.getText().hashCode() == hashCode) {
                    OSDiscount discount = updatedItem.getPrice().getDiscount();
                    discount.setValue(Long.parseLong(value));
                    OSPrice price = updatedItem.getPrice();
                    price.setDiscount(discount);
                    updatedItem.setPrice(price);
                } else if (binding.taxTiet.getText().hashCode() == hashCode) {
                    OSPrice price = updatedItem.getPrice();
                    price.setTax(Long.valueOf(value));
                    updatedItem.setPrice(price);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private ListItemAdapter mAdapter;
    private List<ListItem> mImageUris;
    private BusinessItemOSB originalItem;
    private BusinessItemOSB updatedItem;
    private BusinessV6 business;
    private LoadingBounceDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModifyBusinessItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        business = OSPreferences.getInstance(this).getObject(OSPreferenceKey.BUSINESS, BusinessV6.class);
        loading = new LoadingBounceDialog(this);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpRecycler();
        initiateUI();

        String json = getIntent().getStringExtra(ITEM);
        if (json != null) {
            originalItem = new Gson().fromJson(json, BusinessItemOSB.class);
            updatedItem = new Gson().fromJson(json, BusinessItemOSB.class);
            getSupportActionBar().setTitle("Edit Item");
            setUpUIFromItem();
        } else {
            updatedItem = new BusinessItemOSB();
            binding.discountTypeBtn.setText(OSDiscountType.NO_DISCOUNT.getName());
        }
        addProductCategories();
    }

    private void addProductCategories() {
        if (business == null || OSListUtils.isEmpty(business.getProductCategories())) return;
        List<String> categories = business.getProductCategories();
        Collections.sort(categories, (String::compareToIgnoreCase));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, categories);
        binding.categoriesActv.setAdapter(adapter);
    }

    private void setUpRecycler() {
        mImageUris = new ArrayList<>();
        binding.imageRv.setHasFixedSize(true);
        binding.imageRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new ListItemAdapter(this, mImageUris);
        binding.imageRv.setAdapter(mAdapter);
    }

    private void setUpUIFromItem() {
        List<String> urls = originalItem.getImageUrls();
        if (urls != null && !urls.isEmpty()) {
            for (String url : urls)
                mImageUris.add(new MenuItemImage(url, MenuItemImage.SOURCE_SERVER));
            mAdapter.notifyDataSetChanged();
        }
        OSPrice price = originalItem.getPrice();
        binding.nameTiet.setText(originalItem.getItemName());
        binding.categoriesActv.setText(originalItem.getCategory());
        binding.priceTiet.setText(String.format(Locale.ENGLISH, "%s", price.getPrice() == null ? "" : price.getPrice()));
        binding.quantityTiet.setText(String.format(Locale.ENGLISH, "%s", price.getQuantity() == null ? "" : price.getQuantity()));
        binding.unitBtn.setText(price.getUnit() == null ? getString(R.string.action_set_item_unit) : price.getUnit().name());
        binding.discountValueTiet.setText(String.format(Locale.ENGLISH, "%s", price.getDiscount().getValue() == null ? "" : price.getDiscount().getValue()));
        binding.taxTiet.setText(String.format(Locale.ENGLISH, "%d", price.getTax()));
        binding.descriptionTiet.setText(originalItem.getDescription());

        OSDiscountType discountType = price.getDiscount().getType();
        binding.discountTypeBtn.setText(discountType == null ? OSDiscountType.NO_DISCOUNT.getName() : discountType.getName());
        if (discountType != OSDiscountType.NO_DISCOUNT) {
            binding.discountValueTiet.setEnabled(true);
        }
    }

    private void initiateUI() {
        binding.unitBtn.setOnClickListener(this::OnClickedUnit);
        binding.discountTypeBtn.setOnClickListener(this::onClickedDiscount);
        binding.cancelBtn.setOnClickListener(v -> onBackPressed());
        binding.submitBtn.setOnClickListener(this::onClickedSubmit);

        binding.nameTiet.addTextChangedListener(textWatcher);
        binding.categoriesActv.addTextChangedListener(textWatcher);
        binding.descriptionTiet.addTextChangedListener(textWatcher);
        binding.priceTiet.addTextChangedListener(textWatcher);
        binding.quantityTiet.addTextChangedListener(textWatcher);
        binding.discountValueTiet.addTextChangedListener(textWatcher);
        binding.taxTiet.addTextChangedListener(textWatcher);

        binding.quantityTiet.setFilters(new InputFilter[]{new InputFilterMinMax(0, 1000)});
        binding.taxTiet.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
    }

    private void OnClickedUnit(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        List<String> items = BusinessItemPriceUnit.getStringAll();
        for (String item : items) menu.getMenu().add(item);

        menu.setOnMenuItemClickListener(item -> {
            binding.unitBtn.setText(item.getTitle());
            OSPrice price = updatedItem.getPrice();
            price.setUnit(BusinessItemPriceUnit.valueOf(item.getTitle().toString()));
            updatedItem.setPrice(price);
            return true;
        });
        menu.show();
    }

    private void onClickedDiscount(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenu().add(OSDiscountType.NO_DISCOUNT.getName());
        menu.getMenu().add(OSDiscountType.PERCENT.getName());
        menu.getMenu().add(OSDiscountType.PRICE.getName());

        menu.setOnMenuItemClickListener(item -> {
            binding.discountValueTiet.getText().clear();
            binding.discountTypeBtn.setText(item.getTitle());
            String discountType = item.getTitle().toString();
            OSDiscount discount = updatedItem.getPrice().getDiscount();
            if (discountType.equals(OSDiscountType.NO_DISCOUNT.getName())) {
                discount.setType(OSDiscountType.NO_DISCOUNT);
                discount.setValue(0L);
                binding.discountValueTiet.setEnabled(false);
                binding.discountValueTil.setErrorEnabled(false);
            } else {
                binding.discountValueTiet.setEnabled(true);
            }

            if (discountType.equals(OSDiscountType.PERCENT.getName())) {
                discount.setType(OSDiscountType.PERCENT);
                binding.discountValueTiet.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
            } else {
                binding.discountValueTiet.setFilters(new InputFilter[]{});
            }

            if (discountType.equals(OSDiscountType.PRICE.getName())) {
                discount.setType(OSDiscountType.PRICE);
            }

            OSPrice price = updatedItem.getPrice();
            price.setDiscount(discount);
            updatedItem.setPrice(price);
            return true;
        });
        menu.show();
    }

    private void onClickedSubmit(View view) {
        Log.d("debug", new Gson().toJson(updatedItem));
        boolean isAllFilled = true;
        String errorMsg = "Input require";

        if (TextUtils.isEmpty(updatedItem.getItemName())) {
            isAllFilled = false;
            binding.nameTil.setError(errorMsg);
        }

        if (TextUtils.isEmpty(binding.priceTiet.getText())) {
            isAllFilled = false;
            binding.priceTil.setError(errorMsg);
        }

        if (TextUtils.isEmpty(binding.quantityTiet.getText())) {
            isAllFilled = false;
            binding.quantityTil.setError(errorMsg);
        }

        // If the tax is provided, set it to 0
        OSPrice price = updatedItem.getPrice();
        if (TextUtils.isEmpty(binding.taxTiet.getText())) {
            price.setTax(0L);
            updatedItem.setPrice(price);
        }

        // Item has discount but doesn't provide a discount value
        String discountType = binding.discountTypeBtn.getText().toString();

        if (discountType.equals(OSDiscountType.NO_DISCOUNT.getName())) {
            OSDiscount discount = price.getDiscount();
            discount.setValue(0L);
            discount.setType(OSDiscountType.NO_DISCOUNT);
            price.setDiscount(discount);
            updatedItem.setPrice(price);
        }

        if (!discountType.equals(OSDiscountType.NO_DISCOUNT.getName()) && TextUtils.isEmpty(binding.discountValueTiet.getText())) {
            isAllFilled = false;
            binding.discountValueTil.setError(errorMsg);
        }

        if (!isAllFilled) return;

        if (discountType.equals(OSDiscountType.PRICE.getName()) && BusinessItemUtils.getDiscountPrice(price) > updatedItem.getPrice().getPrice()) {
            binding.discountValueTil.setError("Invalid!!");
            OSMessage.showSBar(this, "Discount can't bigger than the price.");
            return;
        }

        if (BusinessItemUtils.getFinalPrice(price) < 0) {
            OSMessage.showLBar(this, "Final amount " + getString(R.string.inr) + " " + BusinessItemUtils.getFinalPrice(price) + " is invalid.");
            return;
        }

        if (binding.unitBtn.getText().toString().equals(getString(R.string.action_set_item_unit))) {
            binding.unitBtn.performClick();
            OSMessage.showSToast(this, "Select price unit");
            return;
        }

        if (mImageUris == null || mImageUris.size() == 0) {
            OSMessage.showAIBar(this, "At least add an image", "Add Image", v -> new OSImagePicker(this, OSImagePicker.RC_SELECT_MULTIPLE_IMAGES).fromGallery());
            return;
        }

        if (mImageUris.size() > 3) {
            OSMessage.showLToast(this, "Maximum 3 images can add in a product");
            return;
        }

        uploadBusinessItem();
    }

    private void uploadBusinessItem() {
        loading.show();
        String itemId;
        Task<Void> voidTask;
        if (originalItem == null) {
            DocumentReference doc = FirebaseFirestore.getInstance().collection(OSString.refItem).document();
            updatedItem.setBusinessRefId(business.getBusinessRefId());
            updatedItem.setItemId(doc.getId());
            voidTask = doc.set(updatedItem, SetOptions.merge());
            itemId = doc.getId();
        } else {
            itemId = originalItem.getItemId();
            voidTask = FirebaseFirestore.getInstance().collection(OSString.refItem).document(originalItem.getItemId()).set(updatedItem, SetOptions.merge());
        }
        voidTask.addOnCompleteListener(task -> loading.dismiss()).addOnSuccessListener(aVoid -> {
            uploadItemImages(itemId);
            OSMessage.showSToast(this, "Updated");
            onBackPressed();
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            OSMessage.showSToast(this, "Update failed!!");
        });
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
                new OSImagePicker(this, OSImagePicker.RC_SELECT_MULTIPLE_IMAGES).fromGallery();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OSImagePicker.RC_SELECT_MULTIPLE_IMAGES && resultCode == RESULT_OK && data != null) {
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

    @Override
    public void onCardImageRemove(MenuItemImage menuItemImage) {
        if (menuItemImage.getImageSource() == MenuItemImage.SOURCE_SERVER) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(menuItemImage.getImage().toString());

            FirebaseFirestore.getInstance().collection(OSString.refItem)
                    .document(updatedItem.getItemId())
                    .update(OSString.fieldImageUrls, FieldValue.arrayRemove(menuItemImage.getImage().toString()))
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

    private void uploadItemImages(String itemId) {
        List<MenuItemImage> imageUris = (List<MenuItemImage>) (List<?>) mImageUris;

        for (MenuItemImage menuImage : imageUris) {
            Log.v(TAG, "image: " + menuImage.getImage().toString());

            if (menuImage.getImageSource() == MenuItemImage.SOURCE_SERVER) continue;
            try {
                StorageReference ref = FirebaseStorage.getInstance().getReference().child(OSString.bucketItemImage).child(business.getBusinessRefId()).child(itemId);
                final StorageReference imgRef = ref.child(new Date().getTime() + "-" + getFileNameFromUri((Uri) menuImage.getImage()));

                ImageCompression compression = new ImageCompression(this, (Uri) menuImage.getImage());
                File image = compression.compress();

                UploadTask uploadTask = imgRef.putFile(Uri.fromFile(image));
                uploadTask.addOnProgressListener(taskSnapshot -> {

                }).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return null;
                    }
                    return imgRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            Log.v(TAG, downloadUri.toString());
                            addImageUrlToItem(itemId, downloadUri.toString());
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addImageUrlToItem(String itemId, String url) {
        String menuSubCollection = OSString.refItem;
        FirebaseFirestore.getInstance().collection(menuSubCollection)
                .document(itemId).update("imageUrls", FieldValue.arrayUnion(url))
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