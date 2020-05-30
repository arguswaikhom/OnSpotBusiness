package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.model.Header;
import com.crown.onspotbusiness.model.Order;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.utils.abstracts.ListItem;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentOrderFragment extends Fragment implements EventListener<QuerySnapshot> {
    private static final String TAG = CurrentOrderFragment.class.getName();

    private List<ListItem> mDataset;
    private ListItemAdapter mAdapter;
    private TextView mNoCurrentOrderMessage;
    private ListenerRegistration mCurrentOrderChangeListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mNoCurrentOrderMessage = root.findViewById(R.id.tv_wtl_warning);
        mNoCurrentOrderMessage.setText("You have no current order");

        Toolbar toolbar = root.findViewById(R.id.tbar_fm_tool_bar);
        toolbar.setTitle("Current Order");
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        setUpRecycler(root);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getOrder();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_om_all_order) {
            startActivity(new Intent(getActivity(), AllOrderActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void getOrder() {
        String businessRefId = Preferences.getInstance(getActivity().getApplicationContext()).getObject(PreferenceKey.USER, User.class).getBusinessRefId();
        mCurrentOrderChangeListener = FirebaseFirestore.getInstance().collection(getString(R.string.ref_order))
                .whereEqualTo("businessRefId", businessRefId).addSnapshotListener(this);
    }

    private void setUpRecycler(View root) {
        RecyclerView mRecyclerView = root.findViewById(R.id.rv_bnrvl_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDataset = new ArrayList<>();
        mAdapter = new ListItemAdapter(getContext(), mDataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
            updateItemList(queryDocumentSnapshots.getDocuments());
        } else {
            mNoCurrentOrderMessage.setVisibility(View.VISIBLE);
        }
    }

    private void updateItemList(List<DocumentSnapshot> documents) {
        mDataset.clear();
        List<ListItem> ordered = new ArrayList<>();
        List<ListItem> accepted = new ArrayList<>();
        List<ListItem> preparing = new ArrayList<>();
        List<ListItem> onTheWay = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            if (doc.exists()) {
                Order order = doc.toObject(Order.class);
                order.setOrderId(doc.getId());
                switch (order.getStatus()) {
                    case ORDERED:
                        ordered.add(order);
                        break;
                    case ACCEPTED:
                        accepted.add(order);
                        break;
                    case PREPARING:
                        preparing.add(order);
                        break;
                    case ON_THE_WAY:
                        onTheWay.add(order);
                        break;
                }
            }
        }
        if (!ordered.isEmpty()) {
            mDataset.add(new Header("New order"));
            Collections.sort(ordered, (o1, o2) -> {
                Timestamp t1 = ((Order) o1).getStatusRecord().get(0).getTimestamp();
                Timestamp t2 = ((Order) o2).getStatusRecord().get(0).getTimestamp();
                return (int) (t2.getSeconds() - t1.getSeconds());
            });
            mDataset.addAll(ordered);
        }
        if (!accepted.isEmpty()) {
            mDataset.add(new Header("Accepted order"));
            Collections.sort(accepted, (o1, o2) -> {
                Timestamp t1 = ((Order) o1).getStatusRecord().get(0).getTimestamp();
                Timestamp t2 = ((Order) o2).getStatusRecord().get(0).getTimestamp();
                return (int) (t2.getSeconds() - t1.getSeconds());
            });
            mDataset.addAll(accepted);
        }
        if (!preparing.isEmpty()) {
            mDataset.add(new Header("Preparing"));
            Collections.sort(preparing, (o1, o2) -> {
                Timestamp t1 = ((Order) o1).getStatusRecord().get(0).getTimestamp();
                Timestamp t2 = ((Order) o2).getStatusRecord().get(0).getTimestamp();
                return (int) (t2.getSeconds() - t1.getSeconds());
            });
            mDataset.addAll(preparing);
        }
        if (!onTheWay.isEmpty()) {
            mDataset.add(new Header("Out for deliver"));
            Collections.sort(onTheWay, (o1, o2) -> {
                Timestamp t1 = ((Order) o1).getStatusRecord().get(0).getTimestamp();
                Timestamp t2 = ((Order) o2).getStatusRecord().get(0).getTimestamp();
                return (int) (t2.getSeconds() - t1.getSeconds());
            });
            mDataset.addAll(onTheWay);
        }
        if (mDataset.size() <= 0) {
            mNoCurrentOrderMessage.setVisibility(View.VISIBLE);
        } else {
            mNoCurrentOrderMessage.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCurrentOrderChangeListener.remove();
    }
}
