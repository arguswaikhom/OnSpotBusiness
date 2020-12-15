package com.crown.onspotbusiness.page;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.DeliveryPartnerOSB;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.business.BusinessV6;
import com.crown.library.onspotlibrary.model.order.OSOrder;
import com.crown.library.onspotlibrary.model.user.UserV3;
import com.crown.library.onspotlibrary.utils.OSCommonIntents;
import com.crown.library.onspotlibrary.utils.OSFirebaseDocUtils;
import com.crown.library.onspotlibrary.utils.OSListUtils;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.OSRatingUtils;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.library.onspotlibrary.utils.emun.BusinessRequestStatus;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.FragmentAssignDeliveryPartnerBottomSheetBinding;
import com.crown.onspotbusiness.model.DeliveryPartnerAssign;
import com.crown.onspotbusiness.view.ListItemAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AssignDeliveryPartnerBottomSheetFragment extends BottomSheetDialogFragment {
    public static final String ORDER_ID = "ORDER_ID";
    private OSOrder order;
    private ListenerRegistration orderListener;
    private FragmentAssignDeliveryPartnerBottomSheetBinding binding;

    public static AssignDeliveryPartnerBottomSheetFragment newInstance(String orderId) {
        final AssignDeliveryPartnerBottomSheetFragment fragment = new AssignDeliveryPartnerBottomSheetFragment();
        final Bundle args = new Bundle();
        args.putString(ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAssignDeliveryPartnerBottomSheetBinding.inflate(inflater, container, false);
        binding.removeAssignedDeliveryIv.setOnClickListener(this::onClickedRemoveAssignedDelivery);
        orderListener = FirebaseFirestore.getInstance().collection(OSString.refOrder).document(getArguments().getString(ORDER_ID)).addSnapshotListener(this::onChangedOrder);
        return binding.getRoot();
    }

    private void onChangedOrder(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        order = documentSnapshot.toObject(OSOrder.class);
        if (order != null && order.getDelivery() != null) {
            OSFirebaseDocUtils.getUser(order.getDelivery().getUserId(), (doc, exception) -> {
                if (doc == null) {
                    hideAssignedDelivery();
                    return;
                }
                showAssignedDelivery();
                UserV3 delivery = doc.toObject(UserV3.class);
                binding.assignedDeliveryInclude.nameTv.setText(delivery.getDisplayName());
                OSRatingUtils.getReviewInfo(delivery.getDeliveryRating(), (avg, count) -> {
                    binding.assignedDeliveryInclude.ratingBar.setRating(Float.parseFloat(avg));
                    binding.assignedDeliveryInclude.reviewCountTv.setText(count);
                });
                Glide.with(getContext()).load(delivery.getProfileImageUrl()).apply(new RequestOptions().circleCrop()).into(binding.assignedDeliveryInclude.profileImageIv);
            });
        } else hideAssignedDelivery();
    }

    private void hideAssignedDelivery() {
        binding.assignedDpHeaderTv.setVisibility(View.GONE);
        binding.assignedDeliveryLl.setVisibility(View.GONE);
    }

    private void showAssignedDelivery() {
        binding.assignedDpHeaderTv.setVisibility(View.VISIBLE);
        binding.assignedDeliveryLl.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.deliveryPartnerList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.deliveryPartnerList.addItemDecoration((new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL)));

        BusinessV6 business = OSPreferences.getInstance(getContext()).getObject(OSPreferenceKey.BUSINESS, BusinessV6.class);
        List<ListItem> acceptedPartners = new ArrayList<>();
        if (!OSListUtils.isEmpty(business.getOsd())) {
            for (DeliveryPartnerOSB d : business.getOsd()) {
                if (d.getStatus().equals(BusinessRequestStatus.ACCEPTED.name())) {
                    acceptedPartners.add(new Gson().fromJson(d.toString(), DeliveryPartnerAssign.class));
                }
            }
        }

        if (OSListUtils.isEmpty(acceptedPartners)) {
            binding.mainContent.setVisibility(View.GONE);
            binding.noDeliveryPartnerInclude.noPartnerLl.setVisibility(View.VISIBLE);
            binding.noDeliveryPartnerInclude.downloadOsdBtn.setOnClickListener(v -> OSCommonIntents.onIntentAppOnPlayStore(getContext(), getString(R.string.package_onspot_delivery)));
            return;
        }

        Map<String, String> param = Collections.singletonMap(ORDER_ID, getArguments().getString(ORDER_ID));
        binding.deliveryPartnerList.setAdapter(new ListItemAdapter(getContext(), acceptedPartners, param));
    }

    private void onClickedRemoveAssignedDelivery(View view) {
        if (order == null || order.getDelivery() == null) return;
        Spanned msg = Html.fromHtml("Remove " + order.getDelivery().getDisplayName() + " from this order");
        new AlertDialog.Builder(getContext()).setTitle("Remove delivery").setMessage(msg).setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> {
                    FirebaseFirestore.getInstance().collection(OSString.refOrder)
                            .document(getArguments().getString(ORDER_ID))
                            .update(Collections.singletonMap(OSString.fieldDelivery, null))
                            .addOnSuccessListener(v -> OSMessage.showSToast(getContext(), "Delivery removed."))
                            .addOnFailureListener(e -> OSMessage.showSToast(getContext(), getContext().getString(R.string.msg_failed_to_update)));
                }).show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (orderListener != null) orderListener.remove();
    }
}