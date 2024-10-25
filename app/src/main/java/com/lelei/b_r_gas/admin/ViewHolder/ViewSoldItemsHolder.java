package com.lelei.b_r_gas.admin.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lelei.b_r_gas.R;

public class ViewSoldItemsHolder extends RecyclerView.ViewHolder {

    public TextView product_name, product_value;

    public ViewSoldItemsHolder(@NonNull View itemView) {
        super(itemView);

        product_name = itemView.findViewById(R.id.product_name);
        product_value = itemView.findViewById(R.id.product_value);
    }
}
