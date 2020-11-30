package com.crown.onspotbusiness.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.crown.library.onspotlibrary.model.DeliveryPartnerOSB;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.OSListHeader;
import com.crown.library.onspotlibrary.model.UnSupportedContent;
import com.crown.library.onspotlibrary.model.businessItem.BusinessItemOSB;
import com.crown.library.onspotlibrary.model.notification.OSDeliveryPartnershipRequest;
import com.crown.library.onspotlibrary.model.order.OSOldOrder;
import com.crown.library.onspotlibrary.model.order.OSOrder;
import com.crown.library.onspotlibrary.utils.ListItemType;
import com.crown.library.onspotlibrary.views.viewholder.ListHeaderVH;
import com.crown.library.onspotlibrary.views.viewholder.UnSupportedContentVH;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.model.ArchivedProduct;
import com.crown.onspotbusiness.model.DeliveryPartnerAssign;
import com.crown.onspotbusiness.model.Header;
import com.crown.onspotbusiness.model.MenuItemImage;
import com.crown.onspotbusiness.utils.ListItemKey;
import com.crown.onspotbusiness.utils.abstracts.OnCardImageRemove;
import com.crown.onspotbusiness.view.viewholder.ArchivedProductVH;
import com.crown.onspotbusiness.view.viewholder.BusinessItemCardVH;
import com.crown.onspotbusiness.view.viewholder.BusinessItemDetailsVH;
import com.crown.onspotbusiness.view.viewholder.CurrentOrderVH;
import com.crown.onspotbusiness.view.viewholder.DeliveryPartnerAssignVH;
import com.crown.onspotbusiness.view.viewholder.DeliveryPartnerVH;
import com.crown.onspotbusiness.view.viewholder.DeliveryPartnershipRequestVH;
import com.crown.onspotbusiness.view.viewholder.OldOrderVH;

import java.util.List;
import java.util.Map;

public class ListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = ListItemAdapter.class.getName();
    private final Context mContext;
    private final List<ListItem> mDataset;
    private Map<String, String> param;

    public ListItemAdapter(Context context, List<ListItem> dataset) {
        this.mContext = context;
        this.mDataset = dataset;
    }

    public ListItemAdapter(Context context, List<ListItem> dataset, Map<String, String> param) {
        this.param = param;
        this.mContext = context;
        this.mDataset = dataset;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;
        switch (viewType) {
            case ListItemType.LIST_HEADER: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_list_header, parent, false);
                return new ListHeaderVH(rootView);
            }
            case ListItemType.DELIVERY_PARTNER_OSB: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_delivery_partner, parent, false);
                return new DeliveryPartnerVH(rootView);
            }
            case ListItemType.ARCHIVED_PRODUCT: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_archived_product, parent, false);
                return new ArchivedProductVH(rootView);
            }
            case ListItemType.DELIVERY_PARTNER_ASSIGN: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_delivery_partner_assign, parent, false);
                return new DeliveryPartnerAssignVH(rootView);
            }
            case ListItemType.BUSINESS_ITEM_CARD: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_business_item_card, parent, false);
                return new BusinessItemCardVH(rootView);
            }
            case ListItemType.OS_ORDER: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_current_order, parent, false);
                return new CurrentOrderVH(rootView);
            }
            case ListItemType.OS_OLD_ORDER: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_old_order, parent, false);
                return new OldOrderVH(rootView);
            }
            case ListItemType.NOTI_DELIVERY_PARTNERSHIP_REQUEST: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.noti_delivery_partnership_request, parent, false);
                return new DeliveryPartnershipRequestVH(rootView);
            }
            case ListItemKey.HEADER: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_header, parent, false);
                return new ViewHolder.HeaderVH(rootView);
            }
            case ListItemType.BUSINESS_ITEM_OSB: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_business_item_details, parent, false);
                return new BusinessItemDetailsVH(rootView);
            }
            case ListItemKey.MENU_ITEM_IMAGE: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_image_view_holder_card, parent, false);
                return new ViewHolder.CardImageVH(rootView);
            }
            case ListItemType.UNSUPPORTED_CONTENT:
            default: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_unsupported_content, parent, false);
                return new UnSupportedContentVH(rootView);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ListItemType.LIST_HEADER: {
                ((ListHeaderVH) holder).bind((OSListHeader) mDataset.get(position));
                break;
            }
            case ListItemType.ARCHIVED_PRODUCT: {
                ((ArchivedProductVH) holder).bind((ArchivedProduct) mDataset.get(position));
                break;
            }
            case ListItemType.DELIVERY_PARTNER_OSB: {
                ((DeliveryPartnerVH) holder).bind((DeliveryPartnerOSB) mDataset.get(position));
                break;
            }
            case ListItemType.DELIVERY_PARTNER_ASSIGN: {
                ((DeliveryPartnerAssignVH) holder).bind((DeliveryPartnerAssign) mDataset.get(position), param);
                break;
            }
            case ListItemType.UNSUPPORTED_CONTENT: {
                ((UnSupportedContentVH) holder).bind((UnSupportedContent) mDataset.get(position));
                break;
            }
            case ListItemType.BUSINESS_ITEM_OSB: {
                ((BusinessItemDetailsVH) holder).bind((BusinessItemOSB) mDataset.get(position));
                break;
            }
            case ListItemType.BUSINESS_ITEM_CARD: {
                ((BusinessItemCardVH) holder).bind((BusinessItemOSB) mDataset.get(position));
                break;
            }
            case ListItemType.OS_ORDER: {
                ((CurrentOrderVH) holder).bind(((OSOrder) mDataset.get(position)));
                break;
            }
            case ListItemType.OS_OLD_ORDER: {
                ((OldOrderVH) holder).bind((OSOldOrder) mDataset.get(position));
                break;
            }
            case ListItemType.NOTI_DELIVERY_PARTNERSHIP_REQUEST: {
                ((DeliveryPartnershipRequestVH) holder).bind(((OSDeliveryPartnershipRequest) mDataset.get(position)));
                break;
            }
            case ListItemKey.HEADER: {
                setUpHeader((ViewHolder.HeaderVH) holder, (Header) mDataset.get(position), position);
                break;
            }
            case ListItemKey.MENU_ITEM_IMAGE: {
                setUpMenuItemImage((MenuItemImage) mDataset.get(position), (ViewHolder.CardImageVH) holder);
                break;
            }
        }
    }

    private void setUpMenuItemImage(MenuItemImage menuItemImage, ViewHolder.CardImageVH holder) {
        Glide.with(this.mContext).load(menuItemImage.getImage()).into(holder.cardImageIV);
        holder.removeFab.setOnClickListener(view -> ((OnCardImageRemove) mContext).onCardImageRemove(menuItemImage));
    }

    private void setUpHeader(ViewHolder.HeaderVH holder, Header header, int position) {
        holder.headerTV.setText(header.getHeader());
        holder.headerTV.setTextColor(mContext.getResources().getColor(R.color.dark_blue));
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getItemType();
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
