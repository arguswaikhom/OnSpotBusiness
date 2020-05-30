package com.crown.onspotbusiness.controller.clickHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.model.Menu;
import com.crown.onspotbusiness.model.MenuItem;
import com.crown.onspotbusiness.page.CreateOrEditBusinessItemActivity;
import com.crown.onspotbusiness.utils.MenuItemHelper;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.crown.onspotbusiness.view.MenuItemOnStockUpdateDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MenuItemCH implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private final String TAG = MenuItemCH.class.getName();

    private ListItemAdapter adapter;
    private Activity activity;
    private MenuItem item;

    public MenuItemCH(ListItemAdapter adapter, Activity activity, MenuItem item) {
        this.adapter = adapter;
        this.activity = activity;
        this.item = item;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mi_status_dropdown: {
                onClickedStatusDropDownBtn(view);
                break;
            }
            case R.id.btn_mi_update_on_stock: {
                onClickUpdateOnStock();
                break;
            }
            case R.id.ibtn_mi_more_overflow: {
                onClickMoreOverflow(view);
                break;
            }
        }
    }

    private void onClickUpdateOnStock() {
        MenuItemOnStockUpdateDialog dialog = new MenuItemOnStockUpdateDialog();
        Bundle bundle = new Bundle();
        bundle.putString(MenuItemOnStockUpdateDialog.KEY_MENU_ITEM, new Gson().toJson(item));
        dialog.setArguments(bundle);
        dialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), "");
    }

    private void onClickMoreOverflow(View view) {
        PopupMenu menu = new PopupMenu(activity, view);
        menu.inflate(R.menu.menu_item__more_overflow);
        menu.setOnMenuItemClickListener(this);
        menu.show();
    }

    private void onClickedItemDelete() {
        Map<String, Object> map = new HashMap<>();
        map.put("isDeleted", true);

        FirebaseFirestore.getInstance().collection(activity.getString(R.string.ref_item))
                .document(item.getItemId())
                .update(map)
                .addOnFailureListener(error -> Toast.makeText(activity, "Delete failed", Toast.LENGTH_SHORT).show());

    }

    private void onClickedStatusDropDownBtn(View view) {
        PopupMenu menu = new PopupMenu(activity, view);
        menu.getMenu().add(android.view.Menu.NONE, MenuItemHelper.Status.AVAILABLE.getValue(), android.view.Menu.NONE, MenuItemHelper.getTitle(MenuItemHelper.Status.AVAILABLE));
        menu.getMenu().add(android.view.Menu.NONE, MenuItemHelper.Status.NOT_AVAILABLE.getValue(), android.view.Menu.NONE, MenuItemHelper.getTitle(MenuItemHelper.Status.NOT_AVAILABLE));
        menu.getMenu().add(android.view.Menu.NONE, MenuItemHelper.Status.OUT_OF_STOCK.getValue(), android.view.Menu.NONE, MenuItemHelper.getTitle(MenuItemHelper.Status.OUT_OF_STOCK));

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    updateAvailability(MenuItemHelper.Status.NOT_AVAILABLE); // Not available
                    break;
                case 2:
                    updateAvailability(MenuItemHelper.Status.OUT_OF_STOCK);  // Out of stock
                    break;
                case 0:
                default:
                    updateAvailability(MenuItemHelper.Status.AVAILABLE); // Available
                    break;
            }
            return true;
        });
        menu.show();
    }

    private void updateAvailability(MenuItemHelper.Status state) {
        if (item.getStatus() == null || (item.getStatus() != null && item.getStatus() != state)) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", state);
            FirebaseFirestore.getInstance().collection(activity.getString(R.string.ref_item))
                    .document(item.getItemId()).update(map)
                    .addOnFailureListener(e -> Toast.makeText(activity, "Update failed", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onMenuItemClick(android.view.MenuItem item) {
        int id = item.getItemId();
        if (Menu.Status.all().containsKey(id)) {
            Toast.makeText(activity, Menu.Status.get(id), Toast.LENGTH_SHORT).show();
        }

        switch (id) {
            case R.id.option_mimo_edit:
                Intent intent = new Intent(activity, CreateOrEditBusinessItemActivity.class);
                intent.putExtra(CreateOrEditBusinessItemActivity.KEY_MENU_ITEM, new Gson().toJson(this.item));
                activity.startActivity(intent);
                break;
            case R.id.option_mimo_delete:
                onClickedItemDelete();
                break;
        }
        return false;
    }
}
