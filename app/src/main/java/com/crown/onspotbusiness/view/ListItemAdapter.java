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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.crown.onspotbusiness.R;
import com.crown.onspotbusiness.controller.ItemHelper;
import com.crown.onspotbusiness.controller.clickHandler.MenuItemCH;
import com.crown.onspotbusiness.controller.clickHandler.OrderCH;
import com.crown.onspotbusiness.model.Business;
import com.crown.onspotbusiness.model.Header;
import com.crown.onspotbusiness.model.MenuItem;
import com.crown.onspotbusiness.model.MenuItemImage;
import com.crown.onspotbusiness.model.Order;
import com.crown.onspotbusiness.model.OrderItem;
import com.crown.onspotbusiness.model.StatusRecord;
import com.crown.onspotbusiness.page.AllOrderActivity;
import com.crown.onspotbusiness.utils.ListItemKey;
import com.crown.onspotbusiness.utils.MenuItemHelper;
import com.crown.onspotbusiness.utils.TimeUtils;
import com.crown.onspotbusiness.utils.abstracts.ListItem;
import com.crown.onspotbusiness.utils.abstracts.OnCardImageRemove;
import com.crown.onspotbusiness.utils.preference.PreferenceKey;
import com.crown.onspotbusiness.utils.preference.Preferences;
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
            case ListItemKey.HEADER: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_header, parent, false);
                return new ViewHolder.HeaderVH(rootView);
            }
            case ListItemKey.MENU_ITEM: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu_item, parent, false);
                return new ViewHolder.MenuItemVH(rootView);
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
            case ListItemKey.MENU_ITEM: {
                setUpMenuItem((ViewHolder.MenuItemVH) holder, (MenuItem) mDataset.get(position));
                break;
            }
        }
    }

    private void setUpMenuItemImage(MenuItemImage menuItemImage, ViewHolder.CardImageVH holder) {
        Glide.with(this.mContext).load(menuItemImage.getImage()).into(holder.cardImageIV);
        holder.removeFab.setOnClickListener(view -> ((OnCardImageRemove) mContext).onCardImageRemove(menuItemImage));
    }

    private void setUpMenuItem(ViewHolder.MenuItemVH holder, MenuItem item) {
        MenuItemCH clickHandler = new MenuItemCH(this, (Activity) mContext, item);

        holder.titleTV.setText(item.getItemName());
        holder.onStockTV.setText(String.format(Locale.ENGLISH, "Item on stock: %s", item.getOnStock() == null ? "∞" : item.getOnStock().toString()));
        holder.priceTL.removeAllViews();
        holder.priceTL.addView(getPriceView("Price: ", "₹ " + item.getPrice()));
        holder.priceTL.addView(getPriceView("Tax: ", "+ ₹ " + item.getTax()));
        holder.priceTL.addView(getPriceView("Discount: ", "- ₹ " + item.getDiscount()));
        holder.priceTL.addView(getPriceView("Final price: ", "₹ " + item.getFinalPrice()));

        if (item.getStatus() != null) {
            holder.statusDropDownBtn.setText(MenuItemHelper.getTitle(item.getStatus()));
        }

        holder.moreOverflowIBtn.setOnClickListener(clickHandler);
        holder.updateOnStockBtn.setOnClickListener(clickHandler);
        holder.statusDropDownBtn.setOnClickListener(clickHandler);

        Glide.with(this.mContext).load(item.getImageUrl()).apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(16))).into(holder.itemImageIV);

    }

    private View getPriceView(String label, String value) {
        LinearLayout root = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.item_price, null);
        ((TextView) root.findViewById(R.id.tv_ip_label)).setText(label);
        ((TextView) root.findViewById(R.id.tv_ip_price)).setText(value);
        return root;
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
