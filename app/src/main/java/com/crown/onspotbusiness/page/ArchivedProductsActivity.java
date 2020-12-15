package com.crown.onspotbusiness.page;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.business.BusinessV0;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.databinding.ActivityArchivedProductsBinding;
import com.crown.onspotbusiness.model.ArchivedProduct;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArchivedProductsActivity extends AppCompatActivity {

    private final List<ListItem> dataset = new ArrayList<>();
    private ListItemAdapter adapter;
    private ListenerRegistration archivedProductListener;
    private ActivityArchivedProductsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArchivedProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter = new ListItemAdapter(this, dataset);
        binding.archivedProductRv.setLayoutManager(new LinearLayoutManager(this));
        binding.archivedProductRv.setAdapter(adapter);
        binding.archivedProductRv.addItemDecoration((new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)));

        BusinessV0 business = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.BUSINESS, BusinessV0.class);
        archivedProductListener = FirebaseFirestore.getInstance().collection(OSString.refItem)
                .whereEqualTo(OSString.fieldBusinessRefId, business.getBusinessRefId())
                .whereEqualTo(OSString.fieldArchived, true).addSnapshotListener(this::onChangedArchiveProduct);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (archivedProductListener != null) archivedProductListener.remove();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onChangedArchiveProduct(QuerySnapshot snapshots, FirebaseFirestoreException e) {
        dataset.clear();
        if (snapshots == null || snapshots.isEmpty()) {
            handleEmptyArchive();
            return;
        }

        showContentLayout();
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            ArchivedProduct product = doc.toObject(ArchivedProduct.class);
            if (product == null) continue;
            dataset.add(product);
        }
        Collections.sort(dataset, (o1, o2) -> ((ArchivedProduct) o1).getItemName().compareToIgnoreCase(((ArchivedProduct) o2).getItemName()));
        adapter.notifyDataSetChanged();

        if (dataset.isEmpty()) handleEmptyArchive();
    }

    private void handleEmptyArchive() {
        binding.emptyArchiveLayout.setVisibility(View.VISIBLE);
        binding.archivedProductRv.setVisibility(View.GONE);
    }

    private void showContentLayout() {
        binding.emptyArchiveLayout.setVisibility(View.GONE);
        binding.archivedProductRv.setVisibility(View.VISIBLE);
    }
}