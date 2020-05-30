package com.crown.onspotbusiness.controller.clickHandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.ViewAnimation;
import com.crown.onspotbusiness.model.Order;
import com.crown.onspotbusiness.model.StatusRecord;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.crown.onspotbusiness.view.ViewHolder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrderCH implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private Activity activity;
    private ListItemAdapter adapter;
    private ViewHolder.OrderVH holder;
    private Order order;

    public OrderCH(Activity activity, ListItemAdapter adapter, ViewHolder.OrderVH holder, Order order) {
        this.activity = activity;
        this.adapter = adapter;
        this.holder = holder;
        this.order = order;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cv_moi_order_item: {
                rotateReflect(holder.toggleInfoIBtn, holder.orderTableTL);
                break;
            }
            case R.id.btn_moi_cancel: {
                onClickedCancelOrder(v);
                break;
            }
            case R.id.btn_moi_status_update: {
                onCLickedOrderStatusUpdate(v);
                break;
            }
            case R.id.ibtn_moi_more_overflow: {
                onClickedMoreOverflow(v);
                break;
            }
        }

    }

    private void onClickedMoreOverflow(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.order_more_option);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    private void onClickedCancelOrder(View view) {
        Map<String, Object> map = new HashMap<>();
        view.setEnabled(false);
        map.put("status", StatusRecord.Status.CANCELED);
        map.put("statusRecord", FieldValue.arrayUnion(new StatusRecord(StatusRecord.Status.CANCELED, new Timestamp(new Date()))));
        FirebaseFirestore.getInstance().collection(activity.getString(R.string.ref_order))
                .document(order.getOrderId()).update(map)
                .addOnFailureListener(error -> {
                    view.setEnabled(true);
                    Toast.makeText(activity, "Update failed!!", Toast.LENGTH_SHORT).show();
                });
    }

    private void onCLickedOrderStatusUpdate(View view) {
        Map<String, Object> map = new HashMap<>();
        // view.setEnabled(false);
        switch (order.getStatus()) {
            case ORDERED:
                map.put("status", StatusRecord.Status.ACCEPTED);
                map.put("statusRecord", FieldValue.arrayUnion(new StatusRecord(StatusRecord.Status.ACCEPTED, new Timestamp(new Date()))));
                break;
            case ACCEPTED:
                map.put("status", StatusRecord.Status.PREPARING);
                map.put("statusRecord", FieldValue.arrayUnion(new StatusRecord(StatusRecord.Status.PREPARING, new Timestamp(new Date()))));
                break;
            case PREPARING:
                map.put("status", StatusRecord.Status.ON_THE_WAY);
                map.put("statusRecord", FieldValue.arrayUnion(new StatusRecord(StatusRecord.Status.ON_THE_WAY, new Timestamp(new Date()))));
                break;
            case ON_THE_WAY:
                map.put("status", StatusRecord.Status.DELIVERED);
                map.put("statusRecord", FieldValue.arrayUnion(new StatusRecord(StatusRecord.Status.DELIVERED, new Timestamp(new Date()))));
                break;
        }

        if (!map.isEmpty()) {
            FirebaseFirestore.getInstance().collection(activity.getString(R.string.ref_order))
                    .document(order.getOrderId()).update(map)
                    .addOnCompleteListener(task -> {
                        // view.setEnabled(true);
                    })
                    .addOnFailureListener(error -> {
                        // view.setEnabled(true);
                        Toast.makeText(activity, "Update failed!!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void rotateReflect(View clickedView, View reflectView) {
        if (performRotation(clickedView)) {
            ViewAnimation.expand(reflectView, () -> {
                // onFinish
            });
        } else {
            ViewAnimation.collapse(reflectView);
        }
    }

    private boolean performRotation(View view) {
        if (view.getRotation() == 0.0f) {
            view.animate().setDuration(200).rotation(180.0f);
            return true;
        }
        view.animate().setDuration(200).rotation(0.0f);
        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_omo_open_map:
                openMap();
                return true;
            case R.id.action_omo_call:
                callCustomer();
                return true;
        }
        return false;
    }

    private void callCustomer() {
        if (activity == null) return;
        String phoneNumber = order.getContact().getPhoneNo();
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        activity.startActivity(intent);
    }

    private void openMap() {
        GeoPoint geoPoint = order.getDestination().getGeoPoint();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + geoPoint.getLatitude() + "," + geoPoint.getLongitude() + "(" + order.getDestination().getAddressLine() + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(mapIntent);
        }
    }
}
