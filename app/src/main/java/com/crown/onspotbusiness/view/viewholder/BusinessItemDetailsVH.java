package com.crown.onspotbusiness.view.viewholder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.crown.library.onspotlibrary.model.OSPrice;
import com.crown.library.onspotlibrary.model.businessItem.BusinessItemOSB;
import com.crown.library.onspotlibrary.utils.BusinessItemUtils;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.emun.BusinessItemStatus;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.LiBusinessItemDetailsBinding;
import com.crown.onspotbusiness.page.ModifyBusinessItemActivity;
import com.crown.onspotbusiness.view.BusinessItemStockUpdateDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BusinessItemDetailsVH extends RecyclerView.ViewHolder {
    private Context context;
    private BusinessItemOSB item;
    private LiBusinessItemDetailsBinding binding;

    public BusinessItemDetailsVH(@NonNull View itemView) {
        super(itemView);
        binding = LiBusinessItemDetailsBinding.bind(itemView);
        this.context = itemView.getContext();
        binding.moreIbtn.setOnClickListener(this::onClickedMore);
        binding.stockUpdateBtn.setOnClickListener(this::onClickedUpdateStock);
        binding.statusBtn.setOnClickListener(this::onClickedStatus);
    }

    public void bind(BusinessItemOSB item) {
        this.item = item;
        OSPrice price = item.getPrice();

        binding.nameTv.setText(item.getItemName());
        binding.stockCountTv.setText(String.format(Locale.ENGLISH, "Item on stock: %s", item.getOnStock() == null ? "∞" : item.getOnStock().toString()));
        binding.priceTl.removeAllViews();
        binding.priceTl.addView(getPriceView("Price: ", String.format(Locale.ENGLISH, "₹ %.2f", (double) price.getPrice())));
        binding.priceTl.addView(getPriceView("Tax: ", String.format(Locale.ENGLISH, "%s + ₹ %.2f", price.getTax() == null || price.getTax() == 0 ? "" : "(" + price.getTax() + "%)", BusinessItemUtils.getTaxAmount(price))));
        binding.priceTl.addView(getPriceView("Discount: ", String.format(Locale.ENGLISH, "- ₹ %.2f", BusinessItemUtils.getDiscountPrice(price))));
        binding.priceTl.addView(getPriceView("Final price: ", String.format(Locale.ENGLISH, "₹ %.2f", BusinessItemUtils.getFinalPrice(price))));

        if (item.getStatus() == null || item.getStatus() == BusinessItemStatus.AVAILABLE) {
            binding.statusBtn.setBackgroundColor(context.getColor(R.color.item_status_available));
            binding.statusBtn.setText(BusinessItemStatus.AVAILABLE.getName());
        } else if (item.getStatus() == BusinessItemStatus.NOT_AVAILABLE) {
            binding.statusBtn.setBackgroundColor(context.getColor(R.color.item_status_not_available));
            binding.statusBtn.setText(item.getStatus().getName());
        } else if (item.getStatus() == BusinessItemStatus.OUT_OF_STOCK) {
            binding.statusBtn.setBackgroundColor(context.getColor(R.color.item_status_out_of_stock));
            binding.statusBtn.setText(item.getStatus().getName());
        }

        if (item.getRating() != null) {
            binding.ratingBar.setRating(item.getRating().getAverage() == null ? 0f : (float) (double) item.getRating().getAverage());
            binding.ratingCount.setText(item.getRating().getCount() == null ? "" : "(" + (float) (double) item.getRating().getCount() + ")");
        }

        String image = item.getImageUrls() != null && !item.getImageUrls().isEmpty() ? item.getImageUrls().get(0) : null;
        Glide.with(context).load(image).apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(16))).into(binding.imageIv);
    }

    private View getPriceView(String label, String value) {
        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.item_price, null);
        ((TextView) root.findViewById(R.id.tv_ip_label)).setText(label);
        ((TextView) root.findViewById(R.id.tv_ip_price)).setText(value);
        return root;
    }

    private void onClickedStatus(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.more_business_item_card);
        Menu menu = popupMenu.getMenu();
        menu.findItem(R.id.nav_mbic_modify).setVisible(false);
        menu.findItem(R.id.action_mbic_archive).setVisible(false);
        popupMenu.setOnMenuItemClickListener(this::onClickedMenuItem);
        popupMenu.show();
    }

    private void onClickedUpdateStock(View view) {
        BusinessItemStockUpdateDialog dialog = new BusinessItemStockUpdateDialog();
        Bundle bundle = new Bundle();
        bundle.putString(BusinessItemStockUpdateDialog.KEY_MENU_ITEM, new Gson().toJson(item));
        dialog.setArguments(bundle);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "");
    }

    private void onClickedMore(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.more_business_item_card);
        Menu menu = popupMenu.getMenu();
        menu.findItem(R.id.action_mbic_available).setVisible(false);
        menu.findItem(R.id.action_mbic_not_available).setVisible(false);
        menu.findItem(R.id.action_mbic_out_of_stock).setVisible(false);
        popupMenu.setOnMenuItemClickListener(this::onClickedMenuItem);
        popupMenu.show();
    }

    private boolean onClickedMenuItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_mbic_modify:
                Intent intent = new Intent(context, ModifyBusinessItemActivity.class);
                intent.putExtra(ModifyBusinessItemActivity.ITEM, new Gson().toJson(item));
                context.startActivity(intent);
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
        }
        return false;
    }

    private void updateAvailability(BusinessItemStatus state) {
        if (item.getStatus() == null || (item.getStatus() != null && item.getStatus() != state)) {
            Map<String, Object> map = new HashMap<>();
            map.put(context.getString(R.string.field_status), state);
            FirebaseFirestore.getInstance().collection(context.getString(R.string.ref_item))
                    .document(item.getItemId()).update(map)
                    .addOnFailureListener(e -> OSMessage.showSToast(context, "Update failed"));
        }
    }
}
