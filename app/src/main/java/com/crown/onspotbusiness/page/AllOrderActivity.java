package com.crown.onspotbusiness.page;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.UnSupportedContent;
import com.crown.library.onspotlibrary.model.business.BusinessV6;
import com.crown.library.onspotlibrary.model.order.OSOldOrder;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.BuildConfig;
import com.crown.onspotbusiness.databinding.ActivityAllOrderBinding;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AllOrderActivity extends AppCompatActivity {
    private List<ListItem> mDataset;
    private ListItemAdapter mAdapter;
    private ActivityAllOrderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpRecycler();
        getOrders();
    }

    private void setUpRecycler() {
        binding.listRv.setHasFixedSize(true);
        binding.listRv.setLayoutManager(new LinearLayoutManager(this));
        mDataset = new ArrayList<>();
        mAdapter = new ListItemAdapter(this, mDataset);
        binding.listRv.setAdapter(mAdapter);
    }

    private void getOrders() {
        BusinessV6 business = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.BUSINESS, BusinessV6.class);
        FirebaseFirestore.getInstance().collection(OSString.refOrder)
                .whereEqualTo(FieldPath.of(OSString.fieldBusiness, OSString.fieldBusinessRefId), business.getBusinessRefId())
                .orderBy(OSString.fieldOrderedAt, Query.Direction.DESCENDING).get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots == null || snapshots.isEmpty()) {
                        handleEmptyOrder();
                        return;
                    }
                    showOrders(snapshots);
                })
                .addOnFailureListener(e -> handleEmptyOrder());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showOrders(@NonNull QuerySnapshot snapshots) {
        mDataset.clear();
        boolean hasUnsupported = false;
        UserOSB user = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class);
        UnSupportedContent unSupportedContent = new UnSupportedContent(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME, user.getUserId(), AllOrderActivity.class.getName());
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            try {
                OSOldOrder order = doc.toObject(OSOldOrder.class);
                assert order != null;
                order.setOrderId(doc.getId());
                mDataset.add(order);
            } catch (Exception e) {
                e.printStackTrace();
                hasUnsupported = true;
                unSupportedContent.addException(new Gson().toJson(e));
                unSupportedContent.addItem(doc);
            }
        }

        if (hasUnsupported) {
            List<ListItem> temp = new ArrayList<>(mDataset);
            mDataset.clear();
            mDataset.add(unSupportedContent);
            mDataset.addAll(temp);
        }
        mAdapter.notifyDataSetChanged();

        if (mDataset.isEmpty()) handleEmptyOrder();
    }

    private void handleEmptyOrder() {
        binding.noOrderLayout.setVisibility(View.VISIBLE);
        binding.listRv.setVisibility(View.GONE);
    }
}
