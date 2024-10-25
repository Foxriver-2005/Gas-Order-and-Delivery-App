package com.lelei.b_r_gas.user.navigation_drawer.order_user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.lelei.b_r_gas.Model.ItemOrder;
import com.lelei.b_r_gas.R;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailHolder> {

    private List<ItemOrder> myOrders;
    private Context context;

    public OrderDetailAdapter(List<ItemOrder> myOrders) {
        this.myOrders = (myOrders != null) ? myOrders : new ArrayList<>();
    }

    @NonNull
    @Override
    public OrderDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_detail_layout, parent, false);
        context = parent.getContext();
        return new OrderDetailHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailHolder holder, int position) {
        if (myOrders == null || position >= myOrders.size()) return;

        ItemOrder itemOrder = myOrders.get(position);
        holder.name.setText(String.format("Name : %s", itemOrder.getProductName()));
        holder.quantity.setText(String.format("Quantity : %s", itemOrder.getQuantity()));
        holder.price.setText(String.format("Price : %s Ksh", itemOrder.getPrice()));
        holder.discount.setText(String.format("Discount : %s%%", itemOrder.getDiscount()));

        if (!itemOrder.getDiscount().equals("0")) {
            holder.discount_container.setVisibility(View.VISIBLE);
            String product_price = itemOrder.getPrice();
            String order_quan = itemOrder.getQuantity();
            String product_dis = itemOrder.getDiscount();

            int final_price = Integer.parseInt(product_price) * Integer.parseInt(order_quan);
            double dis = final_price * (Double.parseDouble(product_dis) / 100);
            double dis_price = final_price - dis;

            holder.product_dis_value.setText(String.format("%s%% Discount Applied", itemOrder.getDiscount()));
            holder.product_dis_price.setText(String.format("%s Ksh", String.valueOf(dis_price)));
        } else {
            holder.discount_container.setVisibility(View.VISIBLE);
            holder.discount_container.setBackground(context.getResources().getDrawable(R.drawable.button_background_dark_green));
            holder.product_dis_value.setText("No Discounts Applied");
            holder.product_dis_price.setText(String.format("%s.0 Ksh", itemOrder.getPrice()));
        }
    }

    @Override
    public int getItemCount() {
        return (myOrders != null) ? myOrders.size() : 0;
    }

    public void updateOrders(List<ItemOrder> newOrders) {
        this.myOrders = (newOrders != null) ? newOrders : new ArrayList<>();
        notifyDataSetChanged();
    }
}