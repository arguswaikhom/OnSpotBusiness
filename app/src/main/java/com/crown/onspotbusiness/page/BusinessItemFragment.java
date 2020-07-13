package com.crown.onspotbusiness.page;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.model.MenuItem;
import com.crown.onspotbusiness.model.User;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BusinessItemFragment extends Fragment implements EventListener<QuerySnapshot> {

    private final String TAG = BusinessItemFragment.class.getName();

    private ListItemAdapter mAdapter;
    private List<ListItem> mDataset;
    private TextView mNoCurrentOrderMessage;
    private ListenerRegistration mMenuItemChangeListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mDataset = new ArrayList<>();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        mNoCurrentOrderMessage = root.findViewById(R.id.tv_wtl_warning);
        mNoCurrentOrderMessage.setText("Create item to see here");

        Toolbar toolbar = root.findViewById(R.id.tbar_aao_tool_bar);
        toolbar.setTitle("Item");
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        RecyclerView mRecyclerView = root.findViewById(R.id.rv_bnrvl_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ListItemAdapter(getContext(), mDataset);
        mRecyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        String businessREfId = Preferences.getInstance(getActivity().getApplicationContext()).getObject(PreferenceKey.USER, User.class).getBusinessRefId();
        mMenuItemChangeListener = FirebaseFirestore.getInstance().collection(getString(R.string.ref_item)).whereEqualTo("businessRefId", businessREfId).addSnapshotListener(this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item__primary_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.nav_mio_add_item) {
            Intent intent = new Intent(getContext(), CreateItemActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        mMenuItemChangeListener.remove();
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
        Log.v(TAG, "Item changed");
        if (snapshots != null && !snapshots.isEmpty()) {
            updateItemList(snapshots.getDocuments());
        } else {
            mNoCurrentOrderMessage.setVisibility(View.VISIBLE);
        }
    }

    private void updateItemList(List<DocumentSnapshot> documents) {
        mDataset.clear();
        for (DocumentSnapshot doc : documents) {
            if (doc.exists()) {
                MenuItem item = doc.toObject(MenuItem.class);
                assert item != null;
                item.setDeleted((Boolean) doc.get("isDeleted"));
                item.setItemId(doc.getId());
                if (!item.getDeleted()) {
                    mDataset.add(item);
                }
            }
        }
        Collections.sort(mDataset, (o1, o2) -> ((MenuItem) o1).getItemName().compareTo(((MenuItem) o2).getItemName()));
        if (mDataset.size() <= 0) mNoCurrentOrderMessage.setVisibility(View.VISIBLE);
        else mNoCurrentOrderMessage.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
    }
}
