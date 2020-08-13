package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.crown.library.onspotlibrary.model.OSDeliveryCharge;
import com.crown.onspotbusiness.databinding.ActivityShippingChargeBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ShippingChargeActivity extends AppCompatActivity {

    public static final String DATA = "data";
    public static final String D_RANGE = "dRange";
    public static final String MAX_RANGE = "maxRange";
    private double dRange;
    private ActivityShippingChargeBinding binding;
    private List<OSDeliveryCharge> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShippingChargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.submitBtn.setOnClickListener(this::onClickedSubmitBtn);

        dRange = getIntent().getDoubleExtra(D_RANGE, 50);
        binding.infoTv.setText(String.format("Fill up-to %s km", dRange));

        String data = getIntent().getStringExtra(DATA);
        if (data != null) {
            List<OSDeliveryCharge> charges = new Gson().fromJson(data, new TypeToken<List<OSDeliveryCharge>>() {
            }.getType());
            showExistingCharges(charges);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void onClickedSubmitBtn(View view) {
        String km0to10 = binding.km0to10Value.getText().toString().trim();
        String km10to20 = binding.km10to20Value.getText().toString().trim();
        String km20to30 = binding.km20to30Value.getText().toString().trim();
        String km30to40 = binding.km30to40Value.getText().toString().trim();
        String km40to50 = binding.km40to50Value.getText().toString().trim();

        int[] startRanges = new int[]{0, 10, 20, 30, 40};
        int[] endRanges = new int[]{10, 20, 30, 40, 50};
        String[] charges = new String[]{km0to10, km10to20, km20to30, km30to40, km40to50};

        int i = 0;
        for (; i < charges.length; i++) {
            if (i == 0 && TextUtils.isEmpty(charges[i])) {
                binding.km0to10.setError("Invalid!!");
                return;
            }

            if (TextUtils.isEmpty(charges[i]) && dRange > startRanges[i]) {
                Toast.makeText(this, "Filed up to " + dRange + " km", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(charges[i])) break;
        }

        for (int j = 0; j < i; j++) {
            list.add(new OSDeliveryCharge(j, startRanges[j], endRanges[j], Integer.valueOf(charges[j])));
        }

        Intent data = new Intent();
        data.putExtra(MAX_RANGE, endRanges[i - 1]);
        data.putExtra(DATA, list.toString());
        setResult(RESULT_OK, data);
        finish();
    }

    private void showExistingCharges(List<OSDeliveryCharge> charges) {
        for (OSDeliveryCharge c : charges) {
            if (c.getFrom() == 0 && c.getTo() == 10) {
                binding.km0to10Value.setText(String.valueOf(c.getCharge()));
            } else if (c.getFrom() == 10 && c.getTo() == 20) {
                binding.km10to20Value.setText(String.valueOf(c.getCharge()));
            } else if (c.getFrom() == 20 && c.getTo() == 30) {
                binding.km20to30Value.setText(String.valueOf(c.getCharge()));
            } else if (c.getFrom() == 30 && c.getTo() == 40) {
                binding.km30to40Value.setText(String.valueOf(c.getCharge()));
            } else if (c.getFrom() == 40 && c.getTo() == 50) {
                binding.km40to50Value.setText(String.valueOf(c.getCharge()));
            }
        }
    }
}