package com.crown.onspotbusiness.view.viewholder;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.controller.OSVolley;
import com.crown.library.onspotlibrary.model.DeliveryPartnerOSB;
import com.crown.library.onspotlibrary.model.business.BusinessOSB;
import com.crown.library.onspotlibrary.model.user.UserV3;
import com.crown.library.onspotlibrary.utils.OSColorUtils;
import com.crown.library.onspotlibrary.utils.OSFirebaseDocUtils;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.OSRatingUtils;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.library.onspotlibrary.utils.emun.BusinessRequestStatus;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.library.onspotlibrary.views.LoadingBounceDialog;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.LiDeliveryPartnerBinding;

import java.util.HashMap;
import java.util.Map;

public class DeliveryPartnerVH extends RecyclerView.ViewHolder {
    private final Context context;
    private final LiDeliveryPartnerBinding binding;
    private final BusinessOSB business;
    private final LoadingBounceDialog loadingDialog;
    private UserV3 user;

    public DeliveryPartnerVH(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        binding = LiDeliveryPartnerBinding.bind(itemView);
        loadingDialog = new LoadingBounceDialog((Activity) context);
        business = OSPreferences.getInstance(context).getObject(OSPreferenceKey.BUSINESS, BusinessOSB.class);
        binding.removeBtn.setOnClickListener(this::onClickedRemoveRequest);
        binding.acceptBtn.setOnClickListener(this::onClickedAcceptRequest);
    }

    public void bind(DeliveryPartnerOSB partner) {
        OSFirebaseDocUtils.getUser(partner.getUserId(), (doc, e) -> {
            if (doc == null) return;
            user = doc.toObject(UserV3.class);
            binding.userInclude.nameTv.setText(user.getDisplayName());
            Glide.with(context).load(user.getProfileImageUrl()).apply(new RequestOptions().circleCrop()).into(binding.userInclude.profileImageIv);
            OSRatingUtils.getReviewInfo(user.getDeliveryRating(), (rating, review) -> {
                binding.userInclude.ratingBar.setRating(Float.parseFloat(rating));
                Drawable drawable = binding.userInclude.ratingBar.getProgressDrawable();
                drawable.setColorFilter(OSColorUtils.getRandomColor(), PorterDuff.Mode.SRC_ATOP);
                binding.userInclude.reviewCountTv.setText(review);
            });
        });
        if (partner.getStatus().equalsIgnoreCase(BusinessRequestStatus.PENDING.name())) {
            binding.acceptBtn.setVisibility(View.VISIBLE);
        } else {
            binding.acceptBtn.setVisibility(View.GONE);
        }
    }

    private void onClickedRemoveRequest(View view) {
        if (user == null || business == null) return;
        String url = OSString.apiRejectDPRequest;
        for (DeliveryPartnerOSB p : business.getOsd()) {
            if (p.getUserId().equals(user.getUserId()) && p.getStatus().equalsIgnoreCase(BusinessRequestStatus.ACCEPTED.name())) {
                url = OSString.apiCancelBusinessPartnership;
                break;
            }
        }
        loadingDialog.show();
        OSVolley.getInstance(context).addToRequestQueue(new StringRequest(Request.Method.POST, url, response -> {
            loadingDialog.dismiss();
            OSMessage.showSToast(context, "Remove successfully");
        }, error -> {
            loadingDialog.dismiss();
            OSMessage.showSToast(context, "Failed to remove!!");
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> param = new HashMap<>();
                param.put(OSString.keyInitiator, OSString.initiatorBusiness);
                param.put(OSString.fieldUserId, user.getUserId());
                param.put(OSString.fieldBusinessRefId, business.getBusinessRefId());
                return param;
            }
        });
    }

    private void onClickedAcceptRequest(View view) {
        if (user == null || business == null) return;
        loadingDialog.show();
        OSVolley.getInstance(context).addToRequestQueue(new StringRequest(Request.Method.POST, OSString.apiAcceptDPRequest, response -> {
            loadingDialog.dismiss();
            OSMessage.showSToast(context, context.getString(R.string.msg_update_successful));
        }, error -> {
            loadingDialog.dismiss();
            OSMessage.showSToast(context, context.getString(R.string.msg_failed_to_update));
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> param = new HashMap<>();
                param.put(OSString.fieldUserId, user.getUserId());
                param.put(OSString.fieldBusinessRefId, business.getBusinessRefId());
                return param;
            }
        });
    }
}
