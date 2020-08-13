package com.crown.onspotbusiness.view;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.crown.onspotbusiness.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewHolder {
    public static class OrderVH extends RecyclerView.ViewHolder {
        public TableLayout orderTableTL;
        public ImageButton toggleInfoIBtn;
        TextView customerDisplayNameTV;
        TextView orderTimeTV;
        TextView orderDataTV;
        TextView distanceTV;
        Button statusUpdateBtn;
        TextView priceTV;
        CardView orderItemCV;
        ImageButton moreOverflowIBtn;
        Button cancelBtn;

        OrderVH(@NonNull View itemView) {
            super(itemView);
            customerDisplayNameTV = itemView.findViewById(R.id.tv_moi_title);
            orderTimeTV = itemView.findViewById(R.id.tv_moi_order_time);
            orderDataTV = itemView.findViewById(R.id.tv_moi_order_date);
            distanceTV = itemView.findViewById(R.id.tv_moi_distance);
            statusUpdateBtn = itemView.findViewById(R.id.btn_moi_status_update);
            priceTV = itemView.findViewById(R.id.tv_moi_price);
            orderTableTL = itemView.findViewById(R.id.tl_moi_order_list);
            orderItemCV = itemView.findViewById(R.id.cv_moi_order_item);
            toggleInfoIBtn = itemView.findViewById(R.id.ibtn_moi_toggle_info);
            cancelBtn = itemView.findViewById(R.id.btn_moi_cancel);
            moreOverflowIBtn = itemView.findViewById(R.id.ibtn_moi_more_overflow);
        }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView headerTV;

        HeaderVH(@NonNull View itemView) {
            super(itemView);
            headerTV = itemView.findViewById(R.id.tv_sh_header);
        }
    }

    static class CardImageVH extends RecyclerView.ViewHolder {
        ImageView cardImageIV;
        FloatingActionButton removeFab;

        CardImageVH(@NonNull View itemView) {
            super(itemView);
            cardImageIV = itemView.findViewById(R.id.iv_ivhc_image);
            removeFab = itemView.findViewById(R.id.fab_ivhc_remove);
        }
    }
}
