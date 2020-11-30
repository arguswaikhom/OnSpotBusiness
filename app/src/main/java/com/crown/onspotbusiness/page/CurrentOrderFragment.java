package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.business.BusinessOSB;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.library.onspotlibrary.utils.emun.OrderStatus;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.FragmentHomeBinding;
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
    private FragmentHomeBinding binding;
    private CurrentOrderHelper orderHelper;
    private ListenerRegistration mCurrentOrderChangeListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        binding.toolbar.setTitle("Current order");
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);

        orderHelper = new CurrentOrderHelper(getContext().getApplicationContext());
        initTabLayout();
        setUpRecycler();
        getOrder();
        return binding.getRoot();
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
    public void onResume() {
        super.onResume();
        BusinessOSB b = OSPreferences.getInstance(getContext()).getObject(OSPreferenceKey.BUSINESS, BusinessOSB.class);
        if (b != null && !b.getIsActive())
            binding.inactiveBusinessInclude.inactiveBusinessOib.setVisibility(View.VISIBLE);
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
            showInfoMsg("No order found");
        }
    }

    private void initTabLayout() {
        for (String tabLabel : orderHelper.getTabLabels()) {
            TabLayout.Tab tab = binding.tabBar.newTab();
            tab.setText(tabLabel);
            binding.tabBar.addTab(tab);
        }

        binding.tabBar.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                CurrentOrderHelper.TabContainer currentTab = orderHelper.getTabContainers().get(tab.getPosition());
                if (currentTab.listPosition != -1) {
                    binding.listRv.post(() -> {
                        float y = binding.listRv.getY() + binding.listRv.getChildAt(currentTab.listPosition).getY();
                        binding.nestedScrollView.smoothScrollTo(0, (int) y);
                    });
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void getOrder() {
        String businessRefId = OSPreferences.getInstance(getActivity().getApplicationContext()).getObject(OSPreferenceKey.USER, UserOSB.class).getBusinessRefId();
        String[] filter = new String[]{OrderStatus.ORDERED.name(), OrderStatus.ACCEPTED.name(), OrderStatus.PREPARING.name(), OrderStatus.READY.name(), OrderStatus.ON_THE_WAY.name()};
        mCurrentOrderChangeListener = FirebaseFirestore.getInstance().collection(getString(R.string.ref_order))
                .whereEqualTo(FieldPath.of(getString(R.string.field_business), getString(R.string.field_business_ref_id)), businessRefId)
                .whereIn(getString(R.string.field_status), Arrays.asList(filter))
                .addSnapshotListener(this);
    }

    private void setUpRecycler() {
        binding.listRv.setHasFixedSize(true);
        binding.listRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mDataset = new ArrayList<>();
        mAdapter = new ListItemAdapter(getContext(), mDataset);
        binding.listRv.setAdapter(mAdapter);
    }

    private void displayCurrentOrder(List<DocumentSnapshot> documents) {
        mDataset.clear();
        mDataset.addAll(orderHelper.getDataset(documents));
        mAdapter.notifyDataSetChanged();
    }

    private void showInfoMsg(String msg) {
        binding.warningInclude.warningTv.setText(msg);
        binding.warningInclude.warningTv.setVisibility(View.VISIBLE);
    }
}
