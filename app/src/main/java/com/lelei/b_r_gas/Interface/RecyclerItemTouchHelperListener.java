package com.lelei.b_r_gas.Interface;

import androidx.recyclerview.widget.RecyclerView;

public interface RecyclerItemTouchHelperListener {

    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}