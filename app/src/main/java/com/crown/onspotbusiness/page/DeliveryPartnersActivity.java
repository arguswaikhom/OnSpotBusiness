package com.crown.onspotbusiness.page;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.DeliveryPartnerOSB;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.OSListHeader;
import com.crown.library.onspotlibrary.model.business.BusinessOSB;
import com.crown.library.onspotlibrary.utils.OSCommonIntents;
import com.crown.library.onspotlibrary.utils.OSListUtils;
import com.crown.library.onspotlibrary.utils.emun.BusinessRequestStatus;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.ActivityDeliveryPartnersBinding;
import com.crown.onspotbusiness.view.ListItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeliveryPartnersActivity extends AppCompatActivity {
    private final List<ListItem> dataset = new ArrayList<>();
    private ListItemAdapter adapter;
    private BusinessOSB business;
    private ActivityDeliveryPartnersBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryPartnersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter = new ListItemAdapter(this, dataset);
        binding.deliveryPartnerRv.setLayoutManager(new LinearLayoutManager(this));
        binding.deliveryPartnerRv.setAdapter(adapter);

        business = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.BUSINESS, BusinessOSB.class);
        showDataset();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDataset() {
        List<DeliveryPartnerOSB> partners = business.getOsd();
        if (OSListUtils.isEmpty(partners)) {
            handleEmptyDeliveryPartners();
            return;
        }

        dataset.clear();
        List<DeliveryPartnerOSB> acceptedPartners = new ArrayList<>();
        List<DeliveryPartnerOSB> pendingPartners = new ArrayList<>();
        for (DeliveryPartnerOSB partner : partners) {
            BusinessRequestStatus status = BusinessRequestStatus.valueOf(partner.getStatus());
            switch (status) {
                case ACCEPTED:
                    acceptedPartners.add(partner);
                    break;
                case PENDING:
                    pendingPartners.add(partner);
                    break;
            }
        }
        if (!OSListUtils.isEmpty(acceptedPartners)) {
            dataset.add(new OSListHeader("Delivery partners", 22f));
            dataset.addAll(acceptedPartners);
        }
        if (!OSListUtils.isEmpty(pendingPartners)) {
            dataset.add(new OSListHeader("Pending requests", 22f));
            dataset.addAll(pendingPartners);
        }
        adapter.notifyDataSetChanged();
    }

    private void handleEmptyDeliveryPartners() {
        binding.emptyDpLayout.setVisibility(View.VISIBLE);
        binding.deliveryPartnerRv.setVisibility(View.GONE);
        binding.emptyDpInclude.downloadOsdBtn.setOnClickListener(v -> OSCommonIntents.onIntentAppOnPlayStore(this, getString(R.string.package_onspot_delivery)));
    }
}