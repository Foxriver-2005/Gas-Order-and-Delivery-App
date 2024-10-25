package com.lelei.b_r_gas.user;

import static com.lelei.b_r_gas.Mpesa.Constants.BUSINESS_SHORT_CODE;
import static com.lelei.b_r_gas.Mpesa.Constants.CALLBACKURL;
import static com.lelei.b_r_gas.Mpesa.Constants.PARTYB;
import static com.lelei.b_r_gas.Mpesa.Constants.PASSKEY;
import static com.lelei.b_r_gas.Mpesa.Constants.TRANSACTION_TYPE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agik.AGIKSwipeButton.Controller.OnSwipeCompleteListener;
import com.agik.AGIKSwipeButton.View.Swipe_Button_View;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.lelei.b_r_gas.Database.Database;
import com.lelei.b_r_gas.Model.ItemOrder;
import com.lelei.b_r_gas.Mpesa.AccessToken;
import com.lelei.b_r_gas.Mpesa.DarajaApiClient;
import com.lelei.b_r_gas.Mpesa.STKPush;
import com.lelei.b_r_gas.Mpesa.Utils;
import com.lelei.b_r_gas.R;
import com.lelei.b_r_gas.common.Common;
import com.lelei.b_r_gas.user.navigation_drawer.order_user.OrderDetailAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceOrder extends AppCompatActivity {
    public boolean isAuthTokenReceived = false;
    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;

    String address, price, latlng;

    List<ItemOrder> cart = new ArrayList<>();

    FirebaseDatabase database;
    DatabaseReference requests, sweets;

    String avaQuantity, orderTime, orderDate, paymentMode;

    boolean checkCart;

    private TextView orderId, username, items_total, packaging_charge, delivery_charge, order_total;
    private Button changeAddress, back;
    private EditText mpesaNo;
    private RadioGroup paymentGroup;
    private RecyclerView listItems;
    private Swipe_Button_View swipeConfirm;

    private RelativeLayout parent_layout;

    private String order_number;
    private double orderT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        mProgressDialog = new ProgressDialog(this);
        mApiClient = new DarajaApiClient();
        mApiClient.setIsDebug(true); //Set True to enable logging, false to disable.

        database = FirebaseDatabase.getInstance();

        orderId = findViewById(R.id.orderId);
        username = findViewById(R.id.username);

        mpesaNo = findViewById(R.id.mpesaNo);

        changeAddress = findViewById(R.id.change_address);
        paymentGroup = findViewById(R.id.radio_group);
        back = findViewById(R.id.back);

        items_total = findViewById(R.id.items_total);
        packaging_charge = findViewById(R.id.packaging_charge);
        delivery_charge = findViewById(R.id.delivery_charge);
        order_total = findViewById(R.id.order_total);

        listItems = findViewById(R.id.list_sweets);
        listItems.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listItems.setLayoutManager(layoutManager);

        swipeConfirm = findViewById(R.id.swipeConfirm);

        parent_layout = findViewById(R.id.parent_layout);

        if (Common.list.size() != 0){
            cart = Common.list;
            checkCart = false;
        }else {
            cart = new Database(getApplicationContext()).getCarts(Common.USER_Phone);
            checkCart = true;
        }

        //Firebase
        sweets = database.getReference("Products");
        requests = database.getReference("Requests");

        if (getIntent() != null){

            address = getIntent().getStringExtra("Address");
            price = getIntent().getStringExtra("Price");
            latlng = getIntent().getStringExtra("LatLng");

        }

        generateOrder();

        makeOrder();

        int id = paymentGroup.getCheckedRadioButtonId();

        if (id == R.id.cod) {
            Date dt = Calendar.getInstance().getTime();
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            orderTime = timeFormat.format(dt);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            orderDate = dateFormat.format(dt);
            paymentMode = "Pay With Mpesa";
        }

        changeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        swipeConfirm.setOnSwipeCompleteListener_forward_reverse(new OnSwipeCompleteListener() {
            @Override
            public void onSwipe_Forward(Swipe_Button_View swipe_button_view) {
                validateAndProceed();

            }

            @Override
            public void onSwipe_Reverse(Swipe_Button_View swipe_button_view) {
                //InActive
            }
        });
    }

    private void generateOrder(){

        order_number = String.valueOf(System.currentTimeMillis());

        orderId.setText(order_number);
        username.setText(Common.Name);

        // Remove commas from price string
        String cleanPrice = price.replace(",", "");

        items_total.setText(String.format("%s Ksh", price));
        packaging_charge.setText("10.0 Ksh");
        delivery_charge.setText("40.0 Ksh");

        // Parse the cleaned price string to double
        orderT = Double.parseDouble(cleanPrice) + 40.0 + 10.0;
        order_total.setText(String.format("%s Ksh", String.valueOf(orderT)));

        OrderDetailAdapter orderDetailAdapter = new OrderDetailAdapter(cart);
        orderDetailAdapter.notifyDataSetChanged();
        listItems.setAdapter(orderDetailAdapter);
    }

    private void makeOrder(){

        for (int i=0; i<cart.size(); i++){

            final String id = cart.get(i).getProductId();
            final String orderQuantity = cart.get(i).getQuantity();

            sweets.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    avaQuantity = dataSnapshot.child("avaQuantity").getValue(String.class);

                    String sweetName = dataSnapshot.child("name").getValue(String.class);

                    if (Integer.parseInt(avaQuantity) < Integer.parseInt(orderQuantity)){

                        swipeConfirm.setVisibility(View.GONE);
                        back.setVisibility(View.VISIBLE);
                        Snackbar.make(parent_layout,sweetName+" is more than the Available Quantity",Snackbar.LENGTH_LONG).show();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {

                if (response.isSuccessful()) {
                    mApiClient.setAuthToken(response.body().accessToken);
                    // Set the flag to indicate that the authentication token has been received
                    isAuthTokenReceived = true;
                    // Now that the token is received, you can perform the STK push
                    performSTKPush(mpesaNo.getText().toString(), orderT);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                // Handle token retrieval failure
            }
        });
    }

    public void performSTKPush(String phone_number, double amount) {
        // Round the amount to the nearest whole number and convert it to a string
        int roundedAmount = (int) Math.round(amount);

        // Convert roundedAmount to a string
        String amountStr = String.valueOf(roundedAmount);

        mProgressDialog.setMessage("Processing Payment....");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        String timestamp = Utils.getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                Utils.getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                amountStr,  // Use the rounded amount here
                Utils.sanitizePhoneNumber(phone_number),
                PARTYB,
                Utils.sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "Flo Gas", // Account reference
                "Order with us"  // Transaction description
        );

        mApiClient.setGetAccessToken(false);

        // Sending the data to the Mpesa API
        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        try {
                            if (response.isSuccessful()) {
                                System.out.println("post submitted to API. %s " + response.body());
                                swipeConfirm.setEnabled(false);
                                swipeConfirm.setThumbImage(null);
                                swipeConfirm.setText("Order Confirmed");
                                Intent intent = new Intent(PlaceOrder.this, OrderPlaceStatus.class);
                                intent.putExtra("OrderId", order_number);
                                intent.putExtra("Address", address);
                                intent.putExtra("Price", String.valueOf(orderT));
                                intent.putExtra("PaymentMode", paymentMode);
                                intent.putExtra("LatLng", latlng);
                                Common.intentOpenAnimation(PlaceOrder.this);
                                startActivity(intent);
                                finish();
                            } else {
                                System.out.println("Response %s " + response.errorBody().string());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 5000);
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                mProgressDialog.dismiss();
            }
        });
    }
    private void validateAndProceed() {
        // Get the phone number from the EditText
        String phoneNumber = mpesaNo.getText().toString().trim();

        // Validate the phone number
        if (isValidPhoneNumber(phoneNumber)) {
            // Proceed to get access token if validation passes
            getAccessToken();
        } else {
            // Show an error message or handle invalid input
            Snackbar.make(parent_layout, "Please enter a valid phone number starting with 07 or 01 and exactly 10 digits.", Snackbar.LENGTH_LONG).show();
        }
    }
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Check if the phone number is not null and has exactly 10 characters
        if (phoneNumber == null || phoneNumber.length() != 10) {
            mpesaNo.setError("Valid Number is required");
            return false;
        }

        // Check if the phone number starts with 07 or 01
        return phoneNumber.startsWith("07") || phoneNumber.startsWith("01");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Common.list.clear(); //To Avoid Conflicts between Cart and Buy Option
        Common.intentCloseAnimation(PlaceOrder.this);
    }

}