package com.crown.onspotbusiness.page;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.model.Order;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllOrderActivity extends AppCompatActivity implements OnCompleteListener<QuerySnapshot> {
    private static final String TAG = AllOrderActivity.class.getName();

    private List<ListItem> mDataset;
    private ListItemAdapter mAdapter;
    private TextView mNoCurrentOrderMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_order);

        mNoCurrentOrderMessage = findViewById(R.id.tv_wtl_warning);
        mNoCurrentOrderMessage.setText("You have no order to display");

        Toolbar toolbar = findViewById(R.id.tbar_aao_tool_bar);
        toolbar.setTitle("All Order");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpRecycler();
    }

    @Override
    public void onStart() {
        super.onStart();
        String businessRefId = Preferences.getInstance(getApplicationContext()).getObject(PreferenceKey.USER, User.class).getBusinessRefId();
        FirebaseFirestore.getInstance().collection(getString(R.string.ref_order))
                .whereEqualTo("businessRefId", businessRefId)
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
        RecyclerView mRecyclerView = findViewById(R.id.rv_rvl_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDataset = new ArrayList<>();
        mAdapter = new ListItemAdapter(this, mDataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void updateItemList(List<DocumentSnapshot> documents) {
        mDataset.clear();
        for (DocumentSnapshot doc : documents) {
            if (doc.exists()) {
                Order order = doc.toObject(Order.class);
                order.setOrderId(doc.getId());
                // if (order.getStatus() == StatusRecord.Status.DELIVERED || order.getStatus() == StatusRecord.Status.CANCELED)
                mDataset.add(order);
            }
        }
        Collections.sort(mDataset, (o1, o2) -> {
            Timestamp t1 = ((Order) o1).getStatusRecord().get(0).getTimestamp();
            Timestamp t2 = ((Order) o2).getStatusRecord().get(0).getTimestamp();
            return (int) (t2.getSeconds() - t1.getSeconds());
        });

        if (mDataset.size() <= 0) {
            mNoCurrentOrderMessage.setVisibility(View.VISIBLE);
        } else {
            mNoCurrentOrderMessage.setVisibility(View.GONE);
        }

        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            updateItemList(task.getResult().getDocuments());
        } else {
            mNoCurrentOrderMessage.setVisibility(View.VISIBLE);
        }
    }
}
