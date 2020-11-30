package com.crown.onspotbusiness.view.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.crown.library.onspotlibrary.utils.OSColorUtils;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.LiArchivedProductBinding;
import com.crown.onspotbusiness.model.ArchivedProduct;
import com.crown.onspotbusiness.page.ModifyBusinessItemActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.Collections;

public class ArchivedProductVH extends RecyclerView.ViewHolder {

    private final Context context;
    private final LiArchivedProductBinding binding;
    private ArchivedProduct product;

    public ArchivedProductVH(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        binding = LiArchivedProductBinding.bind(itemView);
        itemView.setOnClickListener(this::onClickedArchivedItem);
    }

    private void onClickedArchivedItem(View view) {
        if (product == null) return;
        Intent intent = new Intent(context, ModifyBusinessItemActivity.class);
        intent.putExtra(ModifyBusinessItemActivity.ITEM, new Gson().toJson(product));
        context.startActivity(intent);
    }

    public void bind(ArchivedProduct product) {
        this.product = product;
        binding.productNameTv.setText(product.getItemName());
        int color = OSColorUtils.getRandomColor();
        binding.productNameTv.setTextColor(color);
        binding.unarchiveIv.setOnClickListener(this::onClickedUnarchive);
        ImageViewCompat.setImageTintList(binding.unarchiveIv, ColorStateList.valueOf(OSColorUtils.getRandomColor()));
    }

    private void onClickedUnarchive(View view) {
        if (product == null || TextUtils.isEmpty(product.getItemId())) return;
        FirebaseFirestore.getInstance().collection(OSString.refItem).document(product.getItemId())
                .update(Collections.singletonMap(OSString.fieldArchived, false))
                .addOnSuccessListener(v -> OSMessage.showSToast(context, "Unarchived successfully"))
                .addOnFailureListener(e -> OSMessage.showSToast(context, context.getString(R.string.msg_failed_to_update)));
    }
}
