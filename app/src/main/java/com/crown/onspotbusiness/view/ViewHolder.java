package com.crown.onspotbusiness.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crown.onspotbusiness.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewHolder {
    @Deprecated
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
