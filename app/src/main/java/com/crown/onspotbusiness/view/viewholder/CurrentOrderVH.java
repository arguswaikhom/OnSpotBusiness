package com.crown.onspotbusiness.view.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.crown.library.onspotlibrary.controller.OSGlideLoader;
import com.crown.library.onspotlibrary.model.OSOrder;
import com.crown.library.onspotlibrary.model.OSPrice;
import com.crown.library.onspotlibrary.model.OrderStatusRecord;
import com.crown.library.onspotlibrary.model.cart.OSCartLite;
import com.crown.library.onspotlibrary.model.user.UserOrder;
import com.crown.library.onspotlibrary.utils.BusinessItemUtils;
import com.crown.library.onspotlibrary.utils.OSContactReacher;
import com.crown.library.onspotlibrary.utils.OSLocationUtils;
import com.crown.library.onspotlibrary.utils.OSMapUtils;
import com.crown.library.onspotlibrary.utils.OSTimeUtils;
import com.crown.library.onspotlibrary.utils.callback.OnStringResponse;
import com.crown.library.onspotlibrary.utils.emun.OrderStatus;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.LiCurrentOrderBinding;
import com.crown.onspotbusiness.model.StatusRecord;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CurrentOrderVH extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

    private OSOrder order;
    private Context context;
    private LiCurrentOrderBinding binding;

    public CurrentOrderVH(@NonNull View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        this.binding = LiCurrentOrderBinding.bind(itemView);

        binding.moreIbtn.setOnClickListener(this::onClickedMore);
        binding.negativeBtn.setOnClickListener(this::onClickedNegative);
        binding.positiveBtn.setOnClickListener(this::onClickedPositive);
    }

    void onClickedMore(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.order_more_option);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_omo_open_map:
                OSMapUtils.showLocation((Activity) context, order.getCustomer().getLocation().getGeoPoint(), order.getCustomer().getLocation().getAddressLine());
                break;
            case R.id.action_omo_call:
                makeCall(order.getCustomer().getUserId());
                break;
            case R.id.action_omo_call_delivery:
                if (order.getDelivery() != null) makeCall(order.getDelivery().getUserId());
                else
                    new AlertDialog.Builder(context).setMessage("This action can be perform when one of your delivery partner accepted to deliver this order or you can assign it to them.").setPositiveButton("Assign Delivery", (dialog, which) -> assignDelivery()).show();
                break;
            case R.id.action_omo_assign_delivery:
                assignDelivery();
                break;
        }
        return false;
    }

    void onClickedNegative(View view) {
        new AlertDialog.Builder(context).setTitle("Cancel Order").setMessage(Html.fromHtml("Are you sure you want to cancel this order from <b>" + order.getCustomer().getDisplayName() + "</b>?")).setPositiveButton("Yes", (dialog, which) -> {
            Map<String, Object> map = new HashMap<>();
            view.setEnabled(false);
            map.put("status", StatusRecord.Status.CANCELED);
            map.put("statusRecord", FieldValue.arrayUnion(new OrderStatusRecord(OrderStatus.CANCELED, new Timestamp(new Date()))));
            update(map);
        }).setNegativeButton("No", null).show();
    }

    void onClickedPositive(View view) {
        Map<String, Object> map = new HashMap<>();
        view.setEnabled(false);
        switch (order.getStatus()) {
            case ORDERED:
                map.put("status", OrderStatus.ACCEPTED);
                map.put("statusRecord", FieldValue.arrayUnion(new OrderStatusRecord(OrderStatus.ACCEPTED, new Timestamp(new Date()))));
                break;
            case ACCEPTED:
                map.put("status", OrderStatus.PREPARING);
                map.put("statusRecord", FieldValue.arrayUnion(new OrderStatusRecord(OrderStatus.PREPARING, new Timestamp(new Date()))));
                break;
            case PREPARING:
                map.put("status", OrderStatus.READY);
                map.put("statusRecord", FieldValue.arrayUnion(new OrderStatusRecord(OrderStatus.READY, new Timestamp(new Date()))));
                break;
            case READY:
                map.put("status", OrderStatus.ON_THE_WAY);
                map.put("statusRecord", FieldValue.arrayUnion(new OrderStatusRecord(OrderStatus.ON_THE_WAY, new Timestamp(new Date()))));
                break;
            case ON_THE_WAY:
                map.put("status", OrderStatus.DELIVERED);
                map.put("statusRecord", FieldValue.arrayUnion(new OrderStatusRecord(OrderStatus.DELIVERED, new Timestamp(new Date()))));
                break;
        }

        if (!map.isEmpty()) update(map);
    }

    private void assignDelivery() {

    }

    private void makeCall(String uid) {
        OSContactReacher.getUserMobileNumber(context, uid, (OnStringResponse) value -> {
            if (((Activity) context).isFinishing()) return;
            context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + value)));
        }, (e, msg) -> {
            if (((Activity) context).isFinishing()) return;
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void update(Map<String, Object> param) {
        FirebaseFirestore.getInstance().collection(context.getString(R.string.ref_order))
                .document(order.getOrderId()).update(param)
                .addOnFailureListener(error -> Toast.makeText(context, "Update failed!!", Toast.LENGTH_SHORT).show());
    }

    public void bind(OSOrder order) {
        this.order = order;
        UserOrder customer = order.getCustomer();
        UserOrder delivery = order.getDelivery();

        if (!binding.negativeBtn.isEnabled()) binding.negativeBtn.setEnabled(true);
        if (!binding.positiveBtn.isEnabled()) binding.positiveBtn.setEnabled(true);

        delivery = customer;

        int totalItems = 0;
        int finalPrice = 0;
        binding.orderItemOiv.clear();
        for (OSCartLite cart : order.getItems()) {
            int q = (int) (long) cart.getQuantity();
            OSPrice price = cart.getPrice();
            int itemFinalPrice = (int) BusinessItemUtils.getFinalPrice(price);
            totalItems += q;
            finalPrice += q * itemFinalPrice;
            binding.orderItemOiv.addChild(q, cart.getItemName(), itemFinalPrice * q);
        }

        int color = order.getStatus().getColor(context);
        if (color != 0) {
            binding.statusTv.setBackgroundColor(order.getStatus().getColor(context));
            binding.negativeBtn.setTextColor(color);
            binding.positiveBtn.setBackgroundColor(color);
            binding.positiveBtn.setTextColor(context.getColor(android.R.color.white));
        }

        binding.statusTv.setText(order.getStatus().getStatus());
        OSGlideLoader.loadUserProfileImage(context, customer.getUserId(), binding.customerImageIv);
        binding.customerNameTv.setText(Html.fromHtml("<b>" + customer.getDisplayName() + "</>"));
        binding.orderTimeTv.setText(Html.fromHtml("Ordered at: <b>" + OSTimeUtils.getTime(order.getOrderedAt().getSeconds()) + "</>"));
        binding.orderDayTv.setText(OSTimeUtils.getDay(order.getOrderedAt().getSeconds()));
        binding.distanceAwayTv.setText(OSLocationUtils.getDistanceLine(order.getBusiness().getLocation(), customer.getLocation(), "away"));
        binding.itemCountTv.setText(String.format(Locale.ENGLISH, "%d items", totalItems));
        binding.finalPriceTv.setText(String.format("%s %s", context.getString(R.string.inr), finalPrice));
        binding.positiveBtn.setText(order.getStatus().getButtonText());

        // todo: implement real delivery
        if (delivery != null) {
            if (binding.deliveryLl.getVisibility() == View.GONE) {
                binding.deliveryLl.setVisibility(View.VISIBLE);
            }

            OSGlideLoader.loadUserProfileImage(context, delivery.getUserId(), binding.deliveryImageTv);
            binding.deliveryMsgTv.setText(Html.fromHtml("<b>" + delivery.getDisplayName() + "</b> accepted to deliver this order"));
        } else {
            binding.deliveryLl.setVisibility(View.GONE);
        }
    }
}
