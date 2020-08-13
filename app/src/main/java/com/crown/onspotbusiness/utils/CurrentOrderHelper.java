package com.crown.onspotbusiness.utils;

import android.content.Context;

import com.crown.library.onspotlibrary.controller.OSPreferences;
import com.crown.library.onspotlibrary.model.ListItem;
import com.crown.library.onspotlibrary.model.OSOrder;
import com.crown.library.onspotlibrary.model.UnSupportedContent;
import com.crown.library.onspotlibrary.model.user.UserOSB;
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey;
import com.crown.library.onspotlibrary.utils.emun.OrderStatus;
import com.crown.onspotbusiness.BuildConfig;
import com.crown.onspotbusiness.page.CurrentOrderFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentOrderHelper {

    private Context context;
    private List<TabContainer> tabContainers = new ArrayList<>();
    private String[] tabLabels = new String[]{"New", "Accepted", "Preparing", "Ready", "Out For Deliver"};
    private OrderStatus[] tabStatus = new OrderStatus[]{OrderStatus.ORDERED, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.ON_THE_WAY};
    private int length = tabLabels.length;

    public CurrentOrderHelper(Context context) {
        this.context = context;
        for (int i = 0; i < length; i++) {
            tabContainers.add(new TabContainer(tabLabels[i], tabStatus[i]));
        }
    }

    public String[] getTabLabels() {
        return tabLabels;
    }

    public List<TabContainer> getTabContainers() {
        return tabContainers;
    }

    public List<ListItem> getDataset(List<DocumentSnapshot> docs) {
        boolean hasUnSupportedItem = false;
        List<ListItem> finalOrderList = new ArrayList<>();
        List<List<ListItem>> distributedList = new ArrayList<>();
        UserOSB user = OSPreferences.getInstance(context).getObject(OSPreferenceKey.USER, UserOSB.class);
        UnSupportedContent unSupportedContent = new UnSupportedContent(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME, user.getUserId(), CurrentOrderFragment.class.getName());
        for (int i = 0; i < length; i++) distributedList.add(new ArrayList<>());
        for (DocumentSnapshot doc : docs) {
            if (doc.exists()) {
                try {
                    OSOrder order = doc.toObject(OSOrder.class);
                    if (order == null) continue;
                    order.setOrderId(doc.getId());

                    switch (order.getStatus()) {
                        case ORDERED:
                            distributedList.get(0).add(order);
                            tabContainers.get(0).count += 1;
                            break;
                        case ACCEPTED:
                            distributedList.get(1).add(order);
                            tabContainers.get(1).count += 1;
                            break;
                        case PREPARING:
                            distributedList.get(2).add(order);
                            tabContainers.get(2).count += 1;
                            break;
                        case READY:
                            distributedList.get(3).add(order);
                            tabContainers.get(3).count += 1;
                            break;
                        case ON_THE_WAY:
                            distributedList.get(4).add(order);
                            tabContainers.get(4).count += 1;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hasUnSupportedItem = true;
                    unSupportedContent.addItem(doc);
                    unSupportedContent.addException(new Gson().toJson(e));
                }
            }
        }

        int position = 0;
        for (int i = 0; i < distributedList.size(); i++) {
            Collections.sort(distributedList.get(i), (o1, o2) -> (int) (((OSOrder) o2).getOrderedAt().getSeconds() - ((OSOrder) o1).getOrderedAt().getSeconds()));
            tabContainers.get(i).listPosition = position;
            position += tabContainers.get(i).count;
            finalOrderList.addAll(distributedList.get(i));
        }

        if (hasUnSupportedItem) finalOrderList.add(unSupportedContent);

        return finalOrderList;
    }

    public static class TabContainer {
        public String label;
        public int count;
        public int listPosition;
        public OrderStatus status;

        public TabContainer(String label, OrderStatus status) {
            this(label, status, 0, -1);
        }

        public TabContainer(String label, OrderStatus status, int count, int listPosition) {
            this.label = label;
            this.status = status;
            this.count = count;
            this.listPosition = listPosition;
        }
    }
}
