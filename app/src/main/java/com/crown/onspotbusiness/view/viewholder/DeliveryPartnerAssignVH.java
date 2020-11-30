package com.crown.onspotbusiness.view.viewholder;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crown.library.onspotlibrary.model.user.UserOrder;
import com.crown.library.onspotlibrary.model.user.UserV3;
import com.crown.library.onspotlibrary.utils.OSFirebaseDocUtils;
import com.crown.library.onspotlibrary.utils.OSMessage;
import com.crown.library.onspotlibrary.utils.OSRatingUtils;
import com.crown.library.onspotlibrary.utils.OSString;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.databinding.LiDeliveryPartnerAssignBinding;
import com.crown.onspotbusiness.model.DeliveryPartnerAssign;
import com.crown.onspotbusiness.page.AssignDeliveryPartnerBottomSheetFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.Map;

public class DeliveryPartnerAssignVH extends RecyclerView.ViewHolder {
    private final Context context;
    private final LiDeliveryPartnerAssignBinding binding;
    private UserV3 user;
    private Map<String, String> param;

    public DeliveryPartnerAssignVH(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        itemView.setOnClickListener(this::onCLickedView);
        binding = LiDeliveryPartnerAssignBinding.bind(itemView);
    }

    private void onCLickedView(View view) {
        if (user == null && param != null) return;
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("Assign partner").setMessage("Confirm delivery partner")
                .setNegativeButton(context.getString(R.string.action_btn_cancel), null)
                .setPositiveButton(context.getString(R.string.action_btn_confirm), (dialog1, which) -> {
                    UserOrder newDelivery = new UserOrder(user.getUserId(), user.getDisplayName(), user.getLocation());
                    FirebaseFirestore.getInstance().collection(OSString.refOrder)
                            .document(param.get(AssignDeliveryPartnerBottomSheetFragment.ORDER_ID))
                            .update(Collections.singletonMap(OSString.fieldDelivery, newDelivery))
                            .addOnSuccessListener(v -> OSMessage.showSToast(context, "Delivery assigned."))
                            .addOnFailureListener(e -> OSMessage.showSToast(context, context.getString(R.string.msg_failed_to_update)));
                }).create();
        dialog.setOnShowListener(dialog2 -> {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getColor(R.color.colorAccent));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorAccent));
        });
        dialog.show();
    }

    public void bind(DeliveryPartnerAssign partner, Map<String, String> param) {
        OSFirebaseDocUtils.getUser(partner.getUserId(), ((doc, e) -> {
            if (doc == null) {
                //todo: remove this view from list
                return;
            }

            this.param = param;
            this.user = doc.toObject(UserV3.class);

            binding.nameTv.setText(user.getDisplayName());
            OSRatingUtils.getReviewInfo(user.getDeliveryRating(), (avg, count) -> {
                binding.ratingBar.setRating(Float.parseFloat(avg));
                binding.reviewCountTv.setText(count);
            });
            Glide.with(context).load(user.getProfileImageUrl()).apply(new RequestOptions().circleCrop()).into(binding.profileImageIv);
        }));
    }
}
