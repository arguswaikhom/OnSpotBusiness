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
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.BuildConfig;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.ActivityAllOrderBinding;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AllOrderActivity extends AppCompatActivity implements OnCompleteListener<QuerySnapshot> {
    private static final String TAG = AllOrderActivity.class.getName();

    private List<ListItem> mDataset;
    private ListItemAdapter mAdapter;
    private ActivityAllOrderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("All Order");
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpRecycler();
    }

    @Override
    public void onStart() {
        super.onStart();
        BusinessV6 business = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.BUSINESS, BusinessV6.class);
        FirebaseFirestore.getInstance().collection(getString(R.string.ref_order))
                .whereEqualTo(FieldPath.of(getString(R.string.field_business), getString(R.string.field_business_ref_id)), business.getBusinessRefId())
                .orderBy(getString(R.string.field_ordered_at), Query.Direction.DESCENDING)
                .get().addOnCompleteListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecycler() {
        binding.listRv.setHasFixedSize(true);
        binding.listRv.setLayoutManager(new LinearLayoutManager(this));
        mDataset = new ArrayList<>();
        mAdapter = new ListItemAdapter(this, mDataset);
        binding.listRv.setAdapter(mAdapter);
    }

    private void updateItemList(List<DocumentSnapshot> documents) {
        mDataset.clear();
        boolean hasUnsupported = false;
        UserOSB user = OSPreferences.getInstance(getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class);
        UnSupportedContent unSupportedContent = new UnSupportedContent(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME, user.getUserId(), AllOrderActivity.class.getName());
        for (DocumentSnapshot doc : documents) {
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

        if (mDataset.isEmpty()) {
            setInfoMessage("No order found");
        } else {
            binding.warningInclude.warningTv.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            List<DocumentSnapshot> docs = task.getResult().getDocuments();
            if (docs.isEmpty()) {
                setInfoMessage("No order found");
            } else {
                updateItemList(task.getResult().getDocuments());
            }
        } else {
            setInfoMessage("Can't get order");
        }
    }

    private void setInfoMessage(String msg) {
        binding.warningInclude.warningTv.setText(msg);
        binding.warningInclude.warningTv.setVisibility(View.VISIBLE);
    }
}
