package com.lelei.b_r_gas.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.lelei.b_r_gas.Database.Database;
import com.lelei.b_r_gas.Model.MyResponse;
import com.lelei.b_r_gas.Model.Notification;
import com.lelei.b_r_gas.Model.Request;
import com.lelei.b_r_gas.Model.Sender;
import com.lelei.b_r_gas.Model.ItemOrder;
import com.lelei.b_r_gas.Model.Token;
import com.lelei.b_r_gas.R;
import com.lelei.b_r_gas.Remote.APIService;
import com.lelei.b_r_gas.common.Common;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderPlaceStatus extends AppCompatActivity {

    private RelativeLayout parentLayout;

    private TextView orderId, username, orderTotal, pay_with, order_placed;
    private String order_number, name, order_total, pay_method, address, latlng;

    private Button home;

    private List<ItemOrder> cart = new ArrayList<>();

    private DatabaseReference requests, sweets, soldItemsDB;

    private APIService mService;
    private FirebaseUser mUser;

    private boolean checkCart;

    private String avaQuantity;
    private int finalQantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order_status);

        //Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        Paper.init(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mService = Common.getFCMService();

        sweets = database.getReference("Products");
        requests = database.getReference("Requests");
        soldItemsDB = database.getReference("SoldItems");

        parentLayout = findViewById(R.id.container_parent);

        home = findViewById(R.id.home);

        orderId = findViewById(R.id.orderId);
        username = findViewById(R.id.username);
        orderTotal = findViewById(R.id.orderTotal);
        pay_with = findViewById(R.id.pay_with);
        order_placed = findViewById(R.id.order_placed);

        name = Common.Name;

        if (getIntent() != null){

            order_number = getIntent().getStringExtra("OrderId");
            order_total = getIntent().getStringExtra("Price");
            pay_method = getIntent().getStringExtra("PaymentMode");
            address = getIntent().getStringExtra("Address");
            latlng = getIntent().getStringExtra("LatLng");

        }

        if (Common.list.size() != 0){
            cart = Common.list;
            checkCart = false;
        }else {
            cart = new Database(getApplicationContext()).getCarts(Common.USER_Phone);
            checkCart = true;
        }

        final String feedback_check = Paper.book().read(Common.feedback_remember);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (feedback_check !=null){

                    if (feedback_check.equals("1")){

                        //feedback();

                    }else {

                        startActivity(new Intent(OrderPlaceStatus.this,DashboardUser.class));
                        Common.intentOpenAnimation(OrderPlaceStatus.this);
                        finish();

                    }

                }else {

                    startActivity(new Intent(OrderPlaceStatus.this,DashboardUser.class));
                    Common.intentOpenAnimation(OrderPlaceStatus.this);
                    finish();
                }
            }
        });

        init();
        makeOrder();

    }

    private void feedback(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.feedback));
        builder.setMessage("Enjoying the app, would you mind to give us a feedback");
        builder.setCancelable(false);

        View checkBoxView = View.inflate(this, R.layout.feedback_checkbox_user, null);
        CheckBox checkBox =  checkBoxView.findViewById(R.id.checkbox_feed);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Paper.book().write(Common.feedback_remember,"0");
            }
        });

        builder.setView(checkBoxView);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(OrderPlaceStatus.this,FeedbackActivity.class));
                Common.intentOpenAnimation(OrderPlaceStatus.this);
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(OrderPlaceStatus.this,DashboardUser.class));
                Common.intentOpenAnimation(OrderPlaceStatus.this);
                finish();
            }
        });

        builder.show();

    }

    private void init(){

        orderId.setText(order_number);
        username.setText(name);
        orderTotal.setText(order_total);
        pay_with.setText(pay_method);

    }

    private void makeOrder() {

        Date dt = Calendar.getInstance().getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String orderTime = timeFormat.format(dt);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        final String orderDate = dateFormat.format(dt);
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM-yyyy");
        final String orderMonth = dateFormatMonth.format(dt);

        //Assign SoldItem Date
        soldItemsDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.child(orderMonth).exists()){
                    soldItemsDB.child(orderMonth).push();
                }

                if (!dataSnapshot.child(orderMonth).child(orderDate).exists()){
                    soldItemsDB.child(orderMonth).child(orderDate).child("Products").push();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Request request = new Request(
                order_number,
                Common.Name,
                Common.USER_Phone,
                address,
                order_total,
                "0",         //init status
                orderTime,
                orderDate,
                "empty",    //Reject Reason Initially empty
                pay_method,
                "0",        //init isPicked
                "0",        //init Picked By
                mUser.getEmail(),
                latlng,
                cart
        );

        for (int i=0; i<cart.size(); i++){

            final String productName = cart.get(i).getProductName();
            final String id = cart.get(i).getProductId();
            final String orderQuantity = cart.get(i).getQuantity();

            sweets.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //sweet = dataSnapshot.getValue(Sweet.class);

                    avaQuantity = dataSnapshot.child("avaQuantity").getValue(String.class);

                    if (Integer.parseInt(avaQuantity) >= Integer.parseInt(orderQuantity)){

                        soldItemsDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(orderMonth).child(orderDate).child("Products").child(productName).exists()){

                                    String Aq = dataSnapshot.child(orderMonth).child(orderDate).child("Products").child(productName).getValue(String.class);

                                    int q = Integer.parseInt(Aq) + Integer.parseInt(orderQuantity);

                                    soldItemsDB.child(orderMonth).child(orderDate).child("Products").child(productName).setValue(String.valueOf(q));
                                }else {

                                    soldItemsDB.child(orderMonth).child(orderDate).child("Products").child(productName).setValue(orderQuantity);

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        finalQantity = Integer.parseInt(avaQuantity) - Integer.parseInt(orderQuantity);
                        Log.d("Final Quantity", String.valueOf(finalQantity));
                        sweets.child(id).child("avaQuantity").setValue(String.valueOf(finalQantity));

                    }else {
                        toast("Some Items Quantity is more than the Available Quantity, Your Order May get Rejected, Sorry For Inconvenience ");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        //Update to Firebase using milisec to key
        requests.child(order_number).setValue(request);

        //Update Sold Items
        soldItemsDB.child(orderMonth).child(orderDate).child("Requests").child(order_number).setValue(request);

        //Delete Cart After Updating
        if (checkCart){
            new Database(getBaseContext()).cleanCart(Common.USER_Phone);

        }else {
            Common.list.clear();
        }

        sendNotificationorder(order_number);
        Common.sendMail(mUser.getEmail(), order_number, Common.Name, orderDate, order_total,
                Common.USER_Phone, "0", "Fox K. Leleih");

        order_placed.setVisibility(View.VISIBLE);
        home.setVisibility(View.VISIBLE);

    }

    private void sendNotificationorder(final String order_number) {

        final DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("serverToken").equalTo(true);

        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()) {

                    Token serverToken = postSnapshot.getValue(Token.class);

                    //Create Raw PayLoad To Send
                    Notification notification = new Notification("You Have New Order" + order_number, "Flo Gas Final");
                    Sender content = new Sender(serverToken.getToken(), notification);
                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200 ) {

                                        if (response.isSuccessful()) {
                                            Log.d("SendNotification Status","Success");
                                        } else {
                                            Log.d("SendNotification Status","Failed");
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.d("ERROR", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CANCELED",databaseError.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
//        Snackbar.make(parentLayout,"Back is not Allowed",Snackbar.LENGTH_LONG).show();
//        Common.intentCloseAnimation(OrderPlaceStatus.this);
        startActivity(new Intent(getApplicationContext(), DashboardUser.class));
        finish();
    }

    private void toast(String msg){
        Toast.makeText(OrderPlaceStatus.this,msg,Toast.LENGTH_SHORT).show();
    }
}