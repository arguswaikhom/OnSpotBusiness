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

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.library.onspotlibrary.utils.emun.OrderStatus;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.utils.CurrentOrderHelper;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CurrentOrderFragment extends Fragment implements EventListener<QuerySnapshot> {
    private static final String TAG = CurrentOrderFragment.class.getName();

    private List<ListItem> mDataset;
    private ListItemAdapter mAdapter;
    private TextView mNoCurrentOrderMessage;
    private TabLayout tabLayout;
    private RecyclerView mRecyclerView;
    private CurrentOrderHelper orderHelper;
    private ListenerRegistration mCurrentOrderChangeListener;
    TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            CurrentOrderHelper.TabContainer currentTab = orderHelper.getTabContainers().get(tab.getPosition());
            if (currentTab.listPosition != -1) {
                mRecyclerView.smoothScrollToPosition(currentTab.listPosition);
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mNoCurrentOrderMessage = root.findViewById(R.id.tv_wtl_warning);
        mNoCurrentOrderMessage.setText("You have no current order");

        tabLayout = root.findViewById(R.id.tl_fh_tab);
        Toolbar toolbar = root.findViewById(R.id.tbar_fm_tool_bar);
        toolbar.setTitle("Current Order");
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        orderHelper = new CurrentOrderHelper(getContext().getApplicationContext());
        initTabLayout();
        getOrder();
        setUpRecycler(root);
        return root;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCurrentOrderChangeListener != null) mCurrentOrderChangeListener.remove();
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
            displayCurrentOrder(queryDocumentSnapshots.getDocuments());
        } else {
            mDataset.clear();
            mAdapter.notifyDataSetChanged();
            mNoCurrentOrderMessage.setVisibility(View.VISIBLE);
        }
    }

    private void initTabLayout() {
        for (String tabLabel : orderHelper.getTabLabels()) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(tabLabel);
            tabLayout.addTab(tab);
        }
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
    }

    private void getOrder() {
        String businessRefId = OSPreferences.getInstance(getActivity().getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class).getBusinessRefId();
        String[] filter = new String[]{OrderStatus.ORDERED.name(), OrderStatus.ACCEPTED.name(), OrderStatus.PREPARING.name(), OrderStatus.READY.name(), OrderStatus.ON_THE_WAY.name()};
        mCurrentOrderChangeListener = FirebaseFirestore.getInstance().collection(getString(R.string.ref_order))
                .whereEqualTo(FieldPath.of(getString(R.string.field_business), getString(R.string.field_business_ref_id)), businessRefId)
                .whereIn(getString(R.string.field_status), Arrays.asList(filter))
                .addSnapshotListener(this);
    }

    private void setUpRecycler(View root) {
        mRecyclerView = root.findViewById(R.id.rv_fh_list);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDataset = new ArrayList<>();
        mAdapter = new ListItemAdapter(getContext(), mDataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void displayCurrentOrder(List<DocumentSnapshot> documents) {
        mDataset.clear();
        mDataset.addAll(orderHelper.getDataset(documents));
        mAdapter.notifyDataSetChanged();
    }
}
