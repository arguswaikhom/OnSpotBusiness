package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.crown.library.onspotlibrary.model.OSShippingCharge;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.ActivityDeliveryChargeBinding;
import com.google.gson.Gson;

public class DeliveryChargeActivity extends AppCompatActivity {

    public static final String DELIVERY_CHARGE = "DELIVERY_CHARGE";
    public static final String PRE_DELIVERY_CHARGE = "PRE_DELIVERY_CHARGE";
    private ActivityDeliveryChargeBinding binding;
    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            binding.deliveryChargeTil.setErrorEnabled(false);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private OSShippingCharge shippingCharge = new OSShippingCharge();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryChargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.deliveryChargeTiet.addTextChangedListener(watcher);
        binding.minOrderMoreInfoIv.setOnClickListener(v -> new AlertDialog.Builder(this).setMessage(getString(R.string.text_info_min_order_delivery_charge_more_info)).setPositiveButton(getString(R.string.action_btn_got_it), (dialog, which) -> dialog.dismiss()).show());
        binding.maxDistanceMoreInfoIv.setOnClickListener(v -> new AlertDialog.Builder(this).setMessage(getString(R.string.text_info_max_distance_free_delivery_charge_more_info)).setPositiveButton(getString(R.string.action_btn_got_it), (dialog, which) -> dialog.dismiss()).show());
        binding.submitBtn.setOnClickListener(this::onSubmitButton);

        String json = getIntent().getStringExtra(PRE_DELIVERY_CHARGE);
        if (json != null) {
            shippingCharge = new Gson().fromJson(json, OSShippingCharge.class);
            if (shippingCharge.getPerOrder() != null)
                binding.deliveryChargeTiet.setText(String.valueOf(shippingCharge.getPerOrder()));
            if (shippingCharge.getFreeShippingPrice() != null)
                binding.minOrderPriceTiet.setText(String.valueOf(shippingCharge.getFreeShippingPrice()));
            if (shippingCharge.getFreeShippingDistance() != null)
                binding.maxDistanceTiet.setText(String.valueOf(shippingCharge.getFreeShippingDistance()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("ConstantConditions")
    private void onSubmitButton(View view) {
        String deliveryCharge = binding.deliveryChargeTiet.getText().toString().trim();

        if (TextUtils.isEmpty(deliveryCharge)) {
            binding.deliveryChargeTil.setError(getString(R.string.msg_invalid_input));
            return;
        }

        String minOrderPrice = binding.minOrderPriceTiet.getText().toString().trim();
        String maxDistance = binding.maxDistanceTiet.getText().toString().trim();

        shippingCharge.setPerOrder(Long.parseLong(deliveryCharge));
        if (!TextUtils.isEmpty(minOrderPrice))
            shippingCharge.setFreeShippingPrice(Long.parseLong(minOrderPrice));
        if (!TextUtils.isEmpty(maxDistance))
            shippingCharge.setFreeShippingDistance(Long.parseLong(maxDistance));

        Intent data = new Intent();
        data.putExtra(DELIVERY_CHARGE, new Gson().toJson(shippingCharge));
        setResult(RESULT_OK, data);
        finish();
    }
}