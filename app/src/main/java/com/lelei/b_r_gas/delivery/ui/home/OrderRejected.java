package com.lelei.b_r_gas.delivery.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.lelei.b_r_gas.Model.Request;
import com.lelei.b_r_gas.R;
import com.lelei.b_r_gas.common.Common;
import com.lelei.b_r_gas.delivery.DashboardDelivery;
import com.lelei.b_r_gas.delivery.OrderDetailDelivery;
import dmax.dialog.SpotsDialog;

public class OrderRejected extends AppCompatActivity {

    android.app.AlertDialog dialog;
    private DatabaseReference requests;
    private RecyclerView recyclerView;

    TextView textView;

    RecyclerView.LayoutManager layoutManager;

    private FirebaseRecyclerAdapter<Request, OrderViewHolderDelivery> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders_rejected);

        Toolbar toolbar = findViewById(R.id.toolbar_rejected);
        toolbar.setTitle("Rejected Order");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Loading ...")
                .setTheme(R.style.DialogCustom)
                .build();
        dialog.show();

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        textView = findViewById(R.id.order_empty);

        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        if (Common.isConnectedToInternet(this)){
            loadOrders();
        }else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(getResources().getString(R.string.no_internet));
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void loadOrders() {

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolderDelivery>(
                Request.class,
                R.layout.order_layout_delivery,
                OrderViewHolderDelivery.class,
                requests
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolderDelivery orderViewHolder, final Request request, final int i) {

                final String OrderId, OrderUserName;

                OrderId = adapter.getRef(i).getKey();
                OrderUserName = request.getName();

                if (request.getStatus().equals("3")){

                    textView.setVisibility(View.GONE);

                    orderViewHolder.itemView.setVisibility(View.VISIBLE);
                    orderViewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    orderViewHolder.order_detail_container.setVisibility(View.GONE);
                    orderViewHolder.order_id_info.setVisibility(View.VISIBLE);
                    orderViewHolder.order_id_info.setText(OrderId);
                    orderViewHolder.order_id_info.setTextColor(ContextCompat.getColor(OrderRejected.this,R.color.overlayBackground));

                    orderViewHolder.txtOrderRejectedInfo.setVisibility(View.VISIBLE);
                    orderViewHolder.txtOrderRejectedInfo.setText(String.format("Name :  %s",OrderUserName));

                    orderViewHolder.btndetails.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent orderDetail = new Intent(OrderRejected.this, OrderDetailDelivery.class);
                            orderDetail.putExtra("OrderId",OrderId);
                            orderDetail.putExtra("OrderStatus",request.getStatus());
                            startActivity(orderDetail);

                        }
                    });

                    if (dialog.isShowing()){
                        dialog.dismiss();
                    }

                    orderViewHolder.btndirection.setVisibility(View.GONE);

                }else {
                    orderViewHolder.itemView.setVisibility(View.GONE);
                    orderViewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
            }
        };

        if (String.valueOf(adapter.getItemCount()).equals("0")){
            if (dialog.isShowing()){
                dialog.dismiss();
            }
            textView.setVisibility(View.VISIBLE);
            textView.setText("Don't Panic! No Orders Haven't Rejected");
        }

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(OrderRejected.this, DashboardDelivery.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
