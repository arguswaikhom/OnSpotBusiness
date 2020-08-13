package com.crown.onspotbusiness.view;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.crown.library.onspotlibrary.model.businessItem.BusinessItemOSB;
import com.crown.onspotbusiness.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class BusinessItemStockUpdateDialog extends DialogFragment {
    public static final String KEY_MENU_ITEM = "MENU_ITEM";
    private BusinessItemOSB item;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_menu_item_update, null);

        TextInputLayout onStockTIL = root.findViewById(R.id.til_miu_stock);
        TextInputEditText onStockTIET = root.findViewById(R.id.tiet_miu_stock);
        root.findViewById(R.id.btn_miu_undefine).setOnClickListener(view -> {
            if (item.getOnStock() != null) updateOnStock(null);
            BusinessItemStockUpdateDialog.this.getDialog().cancel();
        });

        Bundle argument = getArguments();
        if (argument != null) {
            String json = argument.getString(KEY_MENU_ITEM);
            item = new Gson().fromJson(json, BusinessItemOSB.class);
            if (item.getOnStock() != null) onStockTIET.setText(String.valueOf(item.getOnStock()));
        }

        builder.setView(root)
                .setPositiveButton(R.string.action_ok, (dialog, id) -> {
                    String updatedStock = onStockTIL.getEditText().getText().toString();
                    if (TextUtils.isEmpty(updatedStock)) {
                        onStockTIL.setError("Invalid input");
                        return;
                    }

                    updateOnStock(Long.valueOf(updatedStock));
                })
                .setNegativeButton(R.string.action_cancel, (dialog, id) -> BusinessItemStockUpdateDialog.this.getDialog().cancel());
        return builder.create();
    }

    private void updateOnStock(Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put("onStock", value);

        FirebaseFirestore.getInstance().collection(getString(R.string.ref_item))
                .document(item.getItemId())
                .update(map)
                .addOnFailureListener(error -> Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
    }
}
