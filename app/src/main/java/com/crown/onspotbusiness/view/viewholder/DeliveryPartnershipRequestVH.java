package com.crown.onspotbusiness.view.viewholder;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.crown.library.onspotlibrary.model.notification.DeliveryPartnershipRequest;
import com.crown.library.onspotlibrary.utils.emun.BusinessRequestStatus;
import com.crown.library.onspotlibrary.views.LoadingBounceDialog;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.AppController;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeliveryPartnershipRequestVH extends RecyclerView.ViewHolder {

    @BindView(R.id.iv_ndpr_image)
    ImageView imageIV;
    @BindView(R.id.tv_ndpr_body)
    TextView bodyTV;
    @BindView(R.id.ll_ndpr_btn_layout)
    LinearLayout btnLayoutLL;

    private Context context;
    private LoadingBounceDialog loading;
    private DeliveryPartnershipRequest request;

    public DeliveryPartnershipRequestVH(@NonNull View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        loading = new LoadingBounceDialog((Activity) context);
        ButterKnife.bind(this, itemView);
    }

    public void bind(DeliveryPartnershipRequest request) {
        this.request = request;
        Glide.with(context).load(request.getOsd().getProfileImageUrl()).circleCrop().into(imageIV);

        if (request.getStatus() == BusinessRequestStatus.PENDING) {
            btnLayoutLL.setVisibility(View.VISIBLE);
            bodyTV.setText(Html.fromHtml("<b>" + request.getOsd().getDisplayName() + "</b> wants to be your delivery partner."));
        } else if (request.getStatus() == BusinessRequestStatus.ACCEPTED) {
            btnLayoutLL.setVisibility(View.GONE);
            bodyTV.setText(Html.fromHtml("<b>" + request.getOsd().getDisplayName() + "</b> is now your delivery partner."));
        } else if (request.getStatus() == BusinessRequestStatus.REJECTED) {
            btnLayoutLL.setVisibility(View.GONE);
            bodyTV.setText(Html.fromHtml("<b>" + request.getOsd().getDisplayName() + "</b>'s delivery partnership request was rejected."));
        }

    }

    @OnClick(R.id.btn_ndpr_reject)
    void onClickedReject() {
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
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("userId", request.getOsd().getUserId());
                param.put("displayName", request.getOsb().getDisplayName());
                param.put("businessRefId", request.getOsb().getBusinessRefId());
                param.put("notificationId", request.getId());
                return param;
            }
        });
    }

    @OnClick(R.id.btn_ndpr_accept)
    void onClickedAccept() {
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
                param.put("userId", request.getOsd().getUserId());
                param.put("businessRefId", request.getOsb().getBusinessRefId());
                param.put("notificationId", request.getId());
                return param;
            }
        });
    }
}
