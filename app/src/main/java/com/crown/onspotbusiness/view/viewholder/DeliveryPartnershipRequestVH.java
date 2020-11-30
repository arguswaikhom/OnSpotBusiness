package com.crown.onspotbusiness.view.viewholder;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.business.BusinessV0;
import com.crown.library.onspotlibrary.model.notification.OSDeliveryPartnershipRequest;
import com.crown.library.onspotlibrary.model.user.UserV1;
import com.crown.library.onspotlibrary.utils.OSFirebaseDocUtils;
import com.crown.library.onspotlibrary.utils.emun.BusinessRequestStatus;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.library.onspotlibrary.views.LoadingBounceDialog;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.AppController;
import com.crown.onspotbusiness.databinding.NotiDeliveryPartnershipRequestBinding;

import java.util.HashMap;
import java.util.Map;

public class DeliveryPartnershipRequestVH extends RecyclerView.ViewHolder {

    private final Context context;
    private final BusinessV0 business;
    private final LoadingBounceDialog loading;
    private final NotiDeliveryPartnershipRequestBinding binding;
    private UserV1 osd;
    private OSDeliveryPartnershipRequest request;

    public DeliveryPartnershipRequestVH(@NonNull View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        loading = new LoadingBounceDialog((Activity) context);
        binding = NotiDeliveryPartnershipRequestBinding.bind(itemView);
        business = OSPreferences.getInstance(context).getObject(OSPreferenceKey.BUSINESS, BusinessV0.class);
        binding.rejectBtn.setOnClickListener(this::onClickedReject);
        binding.acceptBtn.setOnClickListener(this::onClickedAccept);
    }

    public void bind(OSDeliveryPartnershipRequest request) {
        this.request = request;
        OSFirebaseDocUtils.getUser(request.getOsd(), (doc, e) -> {
            if (doc == null) return;
            osd = doc.toObject(UserV1.class);
            Glide.with(context).load(osd.getProfileImageUrl()).circleCrop().into(binding.imageIv);

            if (request.getStatus() == BusinessRequestStatus.PENDING) {
                binding.btnLayoutLl.setVisibility(View.VISIBLE);
                binding.bodyTv.setText(Html.fromHtml("<b>" + osd.getDisplayName() + "</b> wants to be your delivery partner."));
            } else if (request.getStatus() == BusinessRequestStatus.ACCEPTED) {
                binding.btnLayoutLl.setVisibility(View.GONE);
                binding.bodyTv.setText(Html.fromHtml("<b>" + osd.getDisplayName() + "</b> is now your delivery partner."));
            } else if (request.getStatus() == BusinessRequestStatus.REJECTED) {
                binding.btnLayoutLl.setVisibility(View.GONE);
                binding.bodyTv.setText(Html.fromHtml("<b>" + osd.getDisplayName() + "</b>'s delivery partnership request was rejected."));
            }
        });
    }

    void onClickedReject(View view) {
        if (osd == null) return;
        String url = context.getString(R.string.domain) + "/rejectDPRequest/";
        loading.show();

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, url, response -> {
            loading.dismiss();
            Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show();
        }, error -> {
            loading.dismiss();
            Toast.makeText(context, "Something went wrong!!", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> param = new HashMap<>();
                param.put("userId", osd.getUserId());
                param.put("displayName", business.getDisplayName());
                param.put("businessRefId", business.getBusinessRefId());
                param.put("notificationId", request.getId());
                return param;
            }
        });
    }

    void onClickedAccept(View view) {
        if (osd == null) return;
        String url = context.getString(R.string.domain) + "/acceptDPRequest/";
        loading.show();

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, url, response -> {
            loading.dismiss();
            Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show();
        }, error -> {
            loading.dismiss();
            Toast.makeText(context, "Something went wrong!!", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("status", BusinessRequestStatus.ACCEPTED.name());
                param.put("userId", osd.getUserId());
                param.put("businessRefId", business.getBusinessRefId());
                param.put("notificationId", request.getId());
                return param;
            }
        });
    }
}
