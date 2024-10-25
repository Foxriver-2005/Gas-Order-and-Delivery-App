package com.lelei.b_r_gas.user.navigation_drawer.cart_user;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import com.lelei.b_r_gas.R;

public class CartViewHolder extends RecyclerView.ViewHolder{

    public TextView txt_cart_name, txt_price , discount_tv;
    public RelativeLayout view_background;
    public LinearLayout view_foreground;
    public ElegantNumberButton bt_quantity;
    public ImageView cart_image, delete_f;
    public LinearLayout root_cart;

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_cart_name = itemView.findViewById(R.id.cart_item_name);
        txt_price = itemView.findViewById(R.id.cart_item_price);
        bt_quantity = itemView.findViewById(R.id.bt_quantity);
        cart_image = itemView.findViewById(R.id.cart_image);
        delete_f = itemView.findViewById(R.id.delete_f);
        discount_tv = itemView.findViewById(R.id.discount_tv);

        view_background = itemView.findViewById(R.id.view_background);
        view_foreground = itemView.findViewById(R.id.view_foreground);
        root_cart = itemView.findViewById(R.id.root_cart);
    }

}
