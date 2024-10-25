package com.lelei.b_r_gas.common;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lelei.b_r_gas.Model.Flags;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.lelei.b_r_gas.Database.Database;
import com.lelei.b_r_gas.Model.Item;
import com.lelei.b_r_gas.Model.ItemOrder;
import com.lelei.b_r_gas.Model.User;
import com.lelei.b_r_gas.R;
import com.lelei.b_r_gas.admin.navigation_drawer.home.UpdateItems;
import com.lelei.b_r_gas.user.PlaceOrder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class ItemsDetail extends AppCompatActivity {

    TextView item_name, item_price, item_description, outOfStock, discount;
    ImageView item_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    ElegantNumberButton numberButton;

    Button buyNow, edit_admin;

    AlertDialog.Builder builder;
    View view1;

    String latlng;

    String sweetId="", dis;
    String phone = Common.USER_Phone;
    String avaQuantity="";
    String appType="";
    String img_url="";

    FirebaseDatabase database;
    DatabaseReference sweet, users;

    Item currentItem;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        Paper.init(this);

        view1 = LayoutInflater.from(ItemsDetail.this).inflate(R.layout.address_places, null);

        //Firebase
        database = FirebaseDatabase.getInstance();
        sweet = database.getReference("Products");
        users = database.getReference("User");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser mUser = mAuth.getCurrentUser();

        Places.initialize(getApplicationContext(), "AIzaSyAhziR4XoP230Obef_f3WC_4b4Dyqf_ldY");
        final PlacesClient placesClient = Places.createClient(this);

        //Init View
        numberButton = findViewById(R.id.number_button_user);

        toolbar = findViewById(R.id.toolbar_sweets_user);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Get Category Id & Available Quantity Info from Intent Extra
        if (getIntent() != null) {
            sweetId = getIntent().getStringExtra("SweetId");
            avaQuantity = getIntent().getStringExtra("AvailableQuantity");
            appType = getIntent().getStringExtra("AppType");
        }

        item_description = findViewById(R.id.sweets_description_user);
        item_name = findViewById(R.id.sweets_name_user);
        item_price = findViewById(R.id.sweets_price_user);
        item_image = findViewById(R.id.img_sweets_user);
        discount = findViewById(R.id.discount);

        collapsingToolbarLayout = findViewById(R.id.collapsing_user);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsedAppBar);

        outOfStock = findViewById(R.id.txt_out_of_stock_user);

        buyNow = findViewById(R.id.bt_buyNow_user);
        btnCart = findViewById(R.id.btn_cart_user);
        edit_admin = findViewById(R.id.edit_admin);

        // Initialize Common.flags if null
        if (Common.flags == null) {
            Common.flags = new Flags(); // Initialize with default constructor or appropriate values
        }

        if (appType != null) {
            if (appType.equals("admin")) {
                numberButton.setVisibility(View.GONE);
                btnCart.hide();
                buyNow.setVisibility(View.GONE);
                edit_admin.setVisibility(View.VISIBLE);
            } else {
                numberButton.setVisibility(View.VISIBLE);
                buyNow.setVisibility(View.VISIBLE);
                edit_admin.setVisibility(View.GONE);
            }
        }

        // Set onClickListener for edit_admin button
        edit_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ItemsDetail.this, UpdateItems.class);
                intent.putExtra("Key", sweetId);
                intent.putExtra("Url", img_url);
                startActivity(intent);
                Common.intentOpenAnimation(ItemsDetail.this);
            }
        });

        numberButton.setRange(1,Integer.parseInt(avaQuantity));
        numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                if (String.valueOf(newValue).equals(avaQuantity)){
                    Toast.makeText(ItemsDetail.this,"You have Reached Maximum Available Quantity",Toast.LENGTH_LONG).show();
                }
            }
        });

        if (!Common.flags.isMakeOrders()){

            TextView textView = findViewById(R.id.make_no_order_reason_sweets);
            textView.setVisibility(View.VISIBLE);
            textView.setText(Common.flags.getMakeOrdersReason());
            textView.setSelected(true);

            numberButton.setVisibility(View.VISIBLE);
            buyNow.setVisibility(View.VISIBLE);
            btnCart.show();
        }

        if (avaQuantity.equals("0")){
            buyNow.setEnabled(false);
            btnCart.hide();
            outOfStock.setVisibility(View.VISIBLE);
        }

        if (avaQuantity.equals("1")){
            outOfStock.setVisibility(View.VISIBLE);
            outOfStock.setText("Last Item Left Hurry Up !");
        }

        builder = new AlertDialog.Builder(ItemsDetail.this);

        if (Common.isConnectedToInternet(this)){

                buyNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mUser != null) {

                            //Init Progress Dialog
                            final android.app.AlertDialog dlg = new SpotsDialog.Builder()
                                    .setContext(ItemsDetail.this)
                                    .setCancelable(false)
                                    .setMessage("Fetching Details...")
                                    .setTheme(R.style.DialogCustom)
                                    .build();

                            dlg.show();

                            users.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    User user = dataSnapshot.child(Common.USER_Phone).getValue(User.class);

                                    int count = Integer.parseInt(user.getBlacklistCount());

                                    if (user.getPendingPayment().equals("1")){

                                        AlertDialog.Builder warn = new AlertDialog.Builder(ItemsDetail.this);
                                        warn.setIcon(R.drawable.ic_warning_black_24dp);
                                        warn.setTitle("Error");
                                        warn.setMessage("You have't paid for your last orders, which you " +
                                                "declined to accept from our delivery boy. Pay those Arrears to continue.");

                                        warn.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });

                                        warn.setNegativeButton("Pay", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Toast.makeText(ItemsDetail.this,"Temporarily Disabled",
                                                        Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        });

                                        if (dlg.isShowing())
                                            dlg.dismiss();

                                        warn.create();
                                        warn.show();

                                    } else {

                                        Date dt = Calendar.getInstance().getTime();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                                        String current = dateFormat.format(dt);
                                        String d = user.getCancelDate();
                                        String da = d.replace(",", " ");

                                        if (count>=3){

                                            try {

                                                Date ds = dateFormat.parse(da);

                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTime(ds);
                                                calendar.add(Calendar.HOUR, 12);

                                                String b = dateFormat.format(calendar.getTime());

                                                Date doli = dateFormat.parse(b);
                                                Date holi = dateFormat.parse(current);

                                                if (holi.before(doli)){

                                                    AlertDialog.Builder warn = new AlertDialog.Builder(ItemsDetail.this);
                                                    warn.setIcon(R.drawable.ic_warning_black_24dp);
                                                    warn.setTitle("Error");
                                                    warn.setMessage("We detected that you have cancelled more than 3 orders in a day" +
                                                            ", so you cant order for next 12 hours");

                                                    warn.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });

                                                    if (dlg.isShowing())
                                                        dlg.dismiss();

                                                    warn.create();
                                                    warn.show();

                                                }else {

                                                    users.child(Common.USER_Phone).child("blacklistCount").setValue("0");
                                                    users.child(Common.USER_Phone).child("cancelDate").setValue("0");

                                                    if (dlg.isShowing())
                                                        dlg.dismiss();

                                                    initPurchase();
                                                }

                                                System.out.println("Time here "+doli + " | "+ holi);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else {

                                            if (dlg.isShowing())
                                                dlg.dismiss();
                                            initPurchase();
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            Toast.makeText(ItemsDetail.this, "You Need to be Logged In !", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
            Toast.makeText(ItemsDetail.this, "No Internet", Toast.LENGTH_SHORT).show();
        }

        if (Common.isConnectedToInternet(this)){
            btnCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addToCart();
                }
            });
        }else {
            Toast.makeText(ItemsDetail.this, "No Internet", Toast.LENGTH_SHORT).show();
        }

        if (!sweetId.isEmpty()){
            if (Common.isConnectedToInternet(getBaseContext())){
                getDetailSweet(sweetId);
            }else {
                Toast.makeText(ItemsDetail.this,"Please Check Your Internet Connection !", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initPurchase(){

        Date dt = Calendar.getInstance().getTime();
        String pattern = "HH:mm a";
        SimpleDateFormat timeFormat = new SimpleDateFormat(pattern);

        String orderTime = timeFormat.format(dt);

        String startTime = "8:00 am";

        try {
            Date date1 = timeFormat.parse(orderTime);
            Date date2 = timeFormat.parse(startTime);

            if (date1.before(date2)){

                AlertDialog.Builder warn = new AlertDialog.Builder(ItemsDetail.this);
                warn.setIcon(R.drawable.ic_warning_black_24dp);
                warn.setTitle("Warning");
                warn.setMessage("We wont deliver Orders between 12:00 am to 8:00 am, still you can place your Order. Your " +
                        "Order will be delivered between working time");

                warn.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        addrsDialog();
                    }
                });

                AlertDialog dialog = warn.create();
                dialog.show();

            }else {

                addrsDialog();

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void addrsDialog() {

        builder.setTitle("Find Places");
        builder.setCancelable(false);
        builder.setView(view1);
        builder.setIcon(R.drawable.ic_place_black_24dp);

        final String savedAddress = Paper.book().read(Common.USER_ADDRESS_SAVED);
        final String savedLatlng = Paper.book().read(Common.USER_SAVED_LATLNG);

        ItemOrder itemOrder1 = new ItemOrder(
                phone,
                sweetId,
                currentItem.getName(),
                numberButton.getNumber(),
                currentItem.getPrice(),
                currentItem.getDiscount(),
                currentItem.getImage());

        Common.list.add(itemOrder1);

        String product_price = currentItem.getPrice();
        String order_quan = numberButton.getNumber();
        String product_dis = currentItem.getDiscount();

        int final_price = Integer.parseInt(product_price) * Integer.parseInt(order_quan);

        final double dis = final_price * (Double.parseDouble(product_dis) / 100);
        final double dis_pirce = final_price - dis;

        if (savedAddress != null){

            if (!savedAddress.isEmpty()){

                final AlertDialog.Builder savedAddresBuilder = new AlertDialog.Builder(ItemsDetail.this);
                savedAddresBuilder.setTitle("Saved Address");

                final View view2 = LayoutInflater.from(ItemsDetail.this)
                        .inflate(R.layout.saved_address, null);

                savedAddresBuilder.setView(view2);

                TextView addrs = view2.findViewById(R.id.addrs);
                ImageView delete_addrs = view2.findViewById(R.id.delete_adrs);
                ImageView new_adrs = view2.findViewById(R.id.new_adrs);
                Button select_adrs = view2.findViewById(R.id.select_adrs);

                String a = savedAddress.replaceAll("\\s+","");
                String address = a.replace(",",",\n");
                addrs.setText(address);

                final AlertDialog alertDialog = savedAddresBuilder.create();

                alertDialog.show();

                delete_addrs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Paper.book().delete(Common.USER_ADDRESS_SAVED);
                        Paper.book().delete(Common.USER_SAVED_LATLNG);
                        alertDialog.dismiss();

                    }
                });

                new_adrs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (view1 != null){

                            ViewGroup parent = (ViewGroup) view1.getParent();
                            if (parent!= null){
                                parent.removeView(view1);
                            }

                        }

                        builder.setView(view1);
                        buy_now(String.valueOf(dis_pirce));

                    }
                });

                select_adrs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent placeOrder = new Intent(ItemsDetail.this, PlaceOrder.class);
                        placeOrder.putExtra("Price",String.valueOf(dis_pirce));
                        placeOrder.putExtra("Address",savedAddress);
                        placeOrder.putExtra("LatLng",savedLatlng);

                        alertDialog.dismiss();
                        startActivity(placeOrder);
                        Common.intentOpenAnimation(ItemsDetail.this);

                    }
                });

            }
        }
        else{

            if (view1 != null){

                ViewGroup parent = (ViewGroup) view1.getParent();
                if (parent!= null){
                    parent.removeView(view1);
                }

            }

            buy_now(String.valueOf(dis_pirce));

        }

    }

    private void buy_now(final String price) {

        final TextView enter_address = view1.findViewById(R.id.enter_address);
        final CheckBox checkBox = view1.findViewById(R.id.saveAddress);

        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME,
                Place.Field.ID, Place.Field.LAT_LNG);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                (getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment));

        autocompleteFragment.setPlaceFields(fields);
        autocompleteFragment.setCountry("KE");
        autocompleteFragment.setHint("Search Nearby Places");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                enter_address.setText(place.getAddress());
                latlng = place.getLatLng().toString();

            }

            @Override
            public void onError(@NonNull Status status) {

                Log.d("Error", status.toString());

            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog alertDialog = builder.create();

        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                builder.setView(null);

            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ad = enter_address.getText().toString();

                if (latlng != null && !latlng.isEmpty()){

                    if (!ad.isEmpty()){

                        Intent placeOrder = new Intent(ItemsDetail.this, PlaceOrder.class);
                        placeOrder.putExtra("Price",price);
                        placeOrder.putExtra("Address",enter_address.getText().toString());
                        placeOrder.putExtra("LatLng",latlng);

                        if (latlng!=null){

                            if (!latlng.isEmpty()){

                                if (checkBox.isChecked()){

                                    Paper.book().write(Common.USER_ADDRESS_SAVED,enter_address.getText().toString());
                                    Paper.book().write(Common.USER_SAVED_LATLNG,latlng);
                                }

                            }

                        }

                        alertDialog.dismiss();
                        startActivity(placeOrder);
                        Common.intentOpenAnimation(ItemsDetail.this);

                    }

                }else {

                    enter_address.setError("Enter Valid Address");

                }

            }
        });

    }

    private void addToCart(){

        boolean isExists = new Database(getBaseContext()).checkSweetExists(sweetId, Common.USER_Phone);

        if (isExists){
            new Database(getApplicationContext()).increaseCart(phone, sweetId, numberButton.getNumber());
            Toast.makeText(ItemsDetail.this, "Added To Cart", Toast.LENGTH_SHORT).show();

        } else {
            new Database(getApplicationContext()).addToCart(new ItemOrder(
                    phone,
                    sweetId,
                    currentItem.getName(),
                    numberButton.getNumber(),
                    currentItem.getPrice(),
                    currentItem.getDiscount(),
                    currentItem.getImage()
            ));
            Toast.makeText(ItemsDetail.this, "Added To Cart", Toast.LENGTH_SHORT).show();
        }

    }

    private void getDetailSweet(final String sweetId) {

        sweet.child(sweetId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentItem = dataSnapshot.getValue(Item.class);

                Picasso.get().load(currentItem.getImage())
                        .into(item_image);

                img_url = currentItem.getImage();

                item_price.setText(currentItem.getPrice());
                item_name.setText(currentItem.getName());
                item_description.setText(currentItem.getDescription());

                if (!currentItem.getDiscount().equals("0")){

                    discount.setVisibility(View.VISIBLE);
                    discount.setText(String.format("%s%% Discount Available", currentItem.getDiscount()));
                }else {
                    discount.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
