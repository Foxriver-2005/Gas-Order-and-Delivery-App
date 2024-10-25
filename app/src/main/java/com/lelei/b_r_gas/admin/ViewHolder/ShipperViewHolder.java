package com.lelei.b_r_gas.admin.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lelei.b_r_gas.Interface.ItemClickListener;
import com.lelei.b_r_gas.R;
import com.lelei.b_r_gas.common.Common;

public class ShipperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

    public TextView shipperName, shipperEmail, shipperPhone;

    private ItemClickListener itemClickListener;

    public ShipperViewHolder(@NonNull View itemView) {
        super(itemView);

        shipperName = itemView.findViewById(R.id.shipper_name);
        shipperEmail = itemView.findViewById(R.id.shipper_email);
        shipperPhone = itemView.findViewById(R.id.shipper_number);

        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        if (itemClickListener != null) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select an Action");
        contextMenu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0, 0, getAdapterPosition(), Common.DELETE);
    }
}
