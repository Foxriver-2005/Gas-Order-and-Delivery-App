package com.lelei.b_r_gas.admin.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.lelei.b_r_gas.Model.ItemOrder;
import com.lelei.b_r_gas.R;

class MyViewHolder extends RecyclerView.ViewHolder{

    public TextView name,quantity,price,discount, product_dis_value, product_dis_price;
    public LinearLayout discount_container;;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.product_name);
        quantity = itemView.findViewById(R.id.product_quantity);
        price = itemView.findViewById(R.id.product_price);
        discount = itemView.findViewById(R.id.product_discount);

        product_dis_value = itemView.findViewById(R.id.product_dis_value);
        product_dis_price = itemView.findViewById(R.id.product_dis_price);

        discount_container = itemView.findViewById(R.id.discount_container);
    }
}

public class OrderDetailAdapterAdmin extends RecyclerView.Adapter<MyViewHolder> {

    private List<ItemOrder> myOrders;
    private Context context;

    public OrderDetailAdapterAdmin(List<ItemOrder> myOrders) {
        this.myOrders = myOrders != null ? myOrders : new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_detail_layout, parent, false);
        context = parent.getContext();
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (myOrders != null && position < myOrders.size()) {
            ItemOrder itemOrder = myOrders.get(position);
            holder.name.setText(String.format("Name : %s", itemOrder.getProductName()));
            holder.quantity.setText(String.format("Quantity : %s", itemOrder.getQuantity()));
            holder.price.setText(String.format("Price : %s", itemOrder.getPrice()));
            holder.discount.setText(String.format("Discount : %s", itemOrder.getDiscount()));

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
    }

    @Override
    public int getItemCount() {
        return myOrders != null ? myOrders.size() : 0;
    }
}
