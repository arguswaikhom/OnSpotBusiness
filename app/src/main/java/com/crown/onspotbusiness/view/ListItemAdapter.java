package com.crown.onspotbusiness.view;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.OSOrder;
import com.crown.library.onspotlibrary.model.UnSupportedContent;
import com.crown.library.onspotlibrary.model.businessItem.BusinessItemOSB;
import com.crown.library.onspotlibrary.model.notification.DeliveryPartnershipRequest;
import com.crown.library.onspotlibrary.utils.ListItemType;
import com.crown.library.onspotlibrary.views.viewholder.UnSupportedContentVH;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.ItemHelper;
import com.crown.onspotbusiness.controller.clickHandler.OrderCH;
import com.crown.onspotbusiness.model.Business;
import com.crown.onspotbusiness.model.Header;
import com.crown.onspotbusiness.model.MenuItemImage;
import com.crown.onspotbusiness.model.Order;
import com.crown.onspotbusiness.model.OrderItem;
import com.crown.onspotbusiness.model.StatusRecord;
import com.crown.onspotbusiness.page.AllOrderActivity;
import com.crown.onspotbusiness.utils.ListItemKey;
import com.crown.onspotbusiness.utils.TimeUtils;
import com.crown.onspotbusiness.utils.abstracts.OnCardImageRemove;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
import com.crown.onspotbusiness.view.viewholder.BusinessItemCardVH;
import com.crown.onspotbusiness.view.viewholder.BusinessItemDetailsVH;
import com.crown.onspotbusiness.view.viewholder.CurrentOrderVH;
import com.crown.onspotbusiness.view.viewholder.DeliveryPartnershipRequestVH;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Locale;

public class ListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = ListItemAdapter.class.getName();
    private Context mContext;
    private List<ListItem> mDataset;

    public ListItemAdapter(Context context, List<ListItem> dataset) {
        this.mContext = context;
        this.mDataset = dataset;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;
        switch (viewType) {
            case ListItemType.BUSINESS_ITEM_CARD: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_business_item_card, parent, false);
                return new BusinessItemCardVH(rootView);
            }
            case ListItemType.UNSUPPORTED_CONTENT: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_unsupported_content, parent, false);
                return new UnSupportedContentVH(rootView);
            }
            case ListItemType.OS_ORDER: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_current_order, parent, false);
                return new CurrentOrderVH(rootView);
            }
            case ListItemType.DELIVERY_PARTNERSHIP_REQUEST: {
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
            case ListItemKey.ORDER:
            default: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_my_order, parent, false);
                return new ViewHolder.OrderVH(rootView);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
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
            case ListItemType.DELIVERY_PARTNERSHIP_REQUEST: {
                ((DeliveryPartnershipRequestVH) holder).bind(((DeliveryPartnershipRequest) mDataset.get(position)));
                break;
            }
            case ListItemKey.ORDER: {
                setUpOrder((ViewHolder.OrderVH) holder, (Order) mDataset.get(position), position);
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

    private void setUpOrder(ViewHolder.OrderVH holder, Order order, int position) {
        Timestamp orderedTime = order.getStatusRecord().get(0).getTimestamp();
        OrderCH handler = new OrderCH((Activity) mContext, this, holder, order);
        holder.customerDisplayNameTV.setText(order.getCustomerDisplayName());
        holder.orderTimeTV.setText(String.format("Ordered at %s", TimeUtils.getTime(orderedTime.getSeconds())));
        holder.orderDataTV.setText(TimeUtils.getDay(orderedTime.getSeconds()));
        holder.priceTV.setText(String.format("Total price: ₹ %s", order.getFinalPrice()));
        holder.statusUpdateBtn.setText(StatusRecord.getButtonText(order.getStatus()));
        holder.orderItemCV.setOnClickListener(handler);
        holder.statusUpdateBtn.setOnClickListener(handler);
        holder.cancelBtn.setOnClickListener(handler);
        holder.moreOverflowIBtn.setOnClickListener(handler);

        handler.rotateReflect(holder.toggleInfoIBtn, holder.orderTableTL);
        if (order.getStatus() == StatusRecord.Status.DELIVERED) {
            holder.cancelBtn.setEnabled(false);
            holder.statusUpdateBtn.setEnabled(false);
        } else if (order.getStatus() == StatusRecord.Status.CANCELED) {
            holder.cancelBtn.setEnabled(false);
            holder.statusUpdateBtn.setEnabled(false);
        }

        if (mContext instanceof AllOrderActivity) {
            holder.cancelBtn.setVisibility(View.GONE);
            holder.statusUpdateBtn.setVisibility(View.GONE);
        }

        Business business = Preferences.getInstance(mContext.getApplicationContext()).getObject(PreferenceKey.BUSINESS, Business.class);
        if (business.getLocation() != null) {
            GeoPoint businessGeoPoint = business.getLocation().getGeoPoint();
            Location startLocation = new Location("start");
            startLocation.setLatitude(businessGeoPoint.getLatitude());
            startLocation.setLongitude(businessGeoPoint.getLongitude());

            Location endLocation = new Location("end");
            endLocation.setLatitude(order.getDestination().getGeoPoint().getLatitude());
            endLocation.setLongitude(order.getDestination().getGeoPoint().getLongitude());

            String distance = String.format(Locale.ENGLISH, "<b>%.2f KM</b> from your business location", startLocation.distanceTo(endLocation) / 1000);
            holder.distanceTV.setText(Html.fromHtml(distance));
        } else {
            holder.distanceTV.setVisibility(View.GONE);
        }

        holder.orderTableTL.removeAllViews();
        for (OrderItem item : order.getItems()) {
            long price = item.getQuantity() * (new ItemHelper(item).getFinalAmount());

            LinearLayout root = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.order__order_item, null);
            ((TextView) root.findViewById(R.id.tv_ooi_quantity)).setText(String.format(Locale.ENGLISH, "x%d", item.getQuantity()));
            ((TextView) root.findViewById(R.id.tv_ooi_item_name)).setText(item.getItemName());
            ((TextView) root.findViewById(R.id.tv_ooi_price)).setText(String.format(Locale.ENGLISH, "₹ %d", price));
            holder.orderTableTL.addView(root);
        }
    }
}
