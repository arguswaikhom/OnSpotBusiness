package com.crown.onspotbusiness.view.viewholder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.crown.library.onspotlibrary.model.businessItem.BusinessItemOSB;
import com.crown.library.onspotlibrary.utils.OSCommonIntents;
import com.crown.library.onspotlibrary.utils.OSInAppUrlUtils;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.emun.BusinessItemStatus;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.LiBusinessItemCardBinding;
import com.crown.onspotbusiness.page.ModifyBusinessItemActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class BusinessItemCardVH extends RecyclerView.ViewHolder {

    private final Context context;
    private final LiBusinessItemCardBinding binding;
    private BusinessItemOSB item;

    public BusinessItemCardVH(@NonNull View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        binding = LiBusinessItemCardBinding.bind(itemView);
        binding.moreIbtn.setOnClickListener(this::onClickedMore);
    }

    private void onClickedMore(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.more_business_item_card);
        popupMenu.setOnMenuItemClickListener(this::onClickedMenuItem);
        popupMenu.show();
    }

    private boolean onClickedMenuItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_mbic_modify:
                onClickedModifyProduct();
                return true;
            case R.id.action_mbic_available:
                updateAvailability(BusinessItemStatus.AVAILABLE);
                return true;
            case R.id.action_mbic_not_available:
                updateAvailability(BusinessItemStatus.NOT_AVAILABLE);
                return true;
            case R.id.action_mbic_out_of_stock:
                updateAvailability(BusinessItemStatus.OUT_OF_STOCK);
                return true;
            case R.id.action_mbic_archive:
                Map<String, Object> map = new HashMap<>();
                map.put(context.getString(R.string.field_archived), true);
                FirebaseFirestore.getInstance().collection(context.getString(R.string.ref_item))
                        .document(item.getItemId()).update(map)
                        .addOnFailureListener(error -> OSMessage.showSToast(context, "Failed!!"));
                return true;
            case R.id.action_mbic_share_product:
                OSCommonIntents.onIntentShareText(context, OSInAppUrlUtils.getProductUrl(item.getItemId()));
                return true;
        }
        return false;
    }

    private void onClickedModifyProduct() {
        Intent intent = new Intent(context, ModifyBusinessItemActivity.class);
        intent.putExtra(ModifyBusinessItemActivity.ITEM, new Gson().toJson(item));
        context.startActivity(intent);
    }

    public void bind(BusinessItemOSB item) {
        this.item = item;
        String image = item.getImageUrls() != null && !item.getImageUrls().isEmpty() ? item.getImageUrls().get(0) : null;
        Glide.with(context).load(image).into(binding.imageIv);
        binding.nameTv.setText(item.getItemName());
        binding.ratingBar.setRating(item.getProductRating() == null || item.getProductRating().getAverage() == null ? 0f : (float) (double) item.getProductRating().getAverage());
        if (item.getStatus() == null || item.getStatus() == BusinessItemStatus.AVAILABLE) {
            binding.statusTv.setBackgroundResource(R.color.item_status_available);
            binding.statusTv.setText(BusinessItemStatus.AVAILABLE.getName());
        } else if (item.getStatus() == BusinessItemStatus.NOT_AVAILABLE) {
            binding.statusTv.setBackgroundResource(R.color.item_status_not_available);
            binding.statusTv.setText(item.getStatus().getName());
        } else if (item.getStatus() == BusinessItemStatus.OUT_OF_STOCK) {
            binding.statusTv.setBackgroundResource(R.color.item_status_out_of_stock);
            binding.statusTv.setText(item.getStatus().getName());
        }

        if (!item.getIsActive()) {
            binding.inactiveProductInfoFl.setVisibility(View.VISIBLE);
            binding.inactiveProductInfoFl.setOnClickListener(v -> new AlertDialog.Builder(context).setTitle("Inactive product")
                    .setMessage(Html.fromHtml(context.getString(R.string.msg_info_inactive_product)))
                    .setNegativeButton(context.getString(R.string.action_btn_cancel), null)
                    .setPositiveButton("Update", (dialog, which) -> onClickedModifyProduct()).show());
        } else if (binding.inactiveProductInfoFl.getVisibility() != View.GONE)
            binding.inactiveProductInfoFl.setVisibility(View.GONE);
    }

    private void updateAvailability(BusinessItemStatus state) {
        if (item.getStatus() == null || (item.getStatus() != null && item.getStatus() != state)) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", state);
            FirebaseFirestore.getInstance().collection(context.getString(R.string.ref_item))
                    .document(item.getItemId()).update(map)
                    .addOnFailureListener(e -> OSMessage.showSToast(context, "Update failed"));
        }
    }
}
