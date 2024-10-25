package com.lelei.b_r_gas.common;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Objects;

import com.lelei.b_r_gas.Helper.FetchURL;
import com.lelei.b_r_gas.Helper.TaskLoadedCallback;
import com.lelei.b_r_gas.R;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback,
        TaskLoadedCallback {

    private GoogleMap mMap;

    private final static int LOCATION_PERMISSION_REQUEST = 1001;
    private static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

    };
    private static String[] PERMISSIONS_Q_ABOVE = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private MarkerOptions place1, place2;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(TrackingOrder.this);

        if (getIntent() != null) {

            double lat = Double.parseDouble(Objects.requireNonNull(getIntent().getStringExtra("Lat")));
            double lng = Double.parseDouble(Objects.requireNonNull(getIntent().getStringExtra("Lng")));

            LatLng o_Location = new LatLng(lat, lng);

            place2 = new MarkerOptions().title("Order Location")
                    .position(o_Location)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        }

    }

    private boolean arePermissionDenied() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            for (String permissions : PERMISSIONS_Q_ABOVE) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), permissions) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
            return false;
        } else {
            for (String permissions : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), permissions) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
            return false;
        }

    }

    private boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0) {
            if (arePermissionDenied()) {
                toast("Permission Denied");
                finish();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                mMap.clear();

                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                place1 = new MarkerOptions().position(latLng).title("My Location");

                mMap.addMarker(place1);
                mMap.addMarker(place2);

                new FetchURL(TrackingOrder.this).execute(getUrl(place1.getPosition(),
                        place2.getPosition()),"driving");
                showAllMarkers();

            }
        });

    }

    private void showAllMarkers(){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(place1.getPosition());
        builder.include(place2.getPosition());

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height,padding);
        mMap.animateCamera(cu);

    }

    private String getUrl(LatLng origin, LatLng dest){
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String str_mode = "mode="+ "1";
        String parameter = str_origin + "&" + str_dest + "&" + str_mode;
        String format = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + format + "?"
                + parameter + "&key=" + getString(R.string.gmaps_key);
        return url;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (arePermissionDenied()){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                requestPermissions(PERMISSIONS_Q_ABOVE, LOCATION_PERMISSION_REQUEST);
                Toast.makeText(this, "Please Grant Permissions", Toast.LENGTH_SHORT).show();
                return;
            }

            requestPermissions(PERMISSIONS, LOCATION_PERMISSION_REQUEST);
            Toast.makeText(this, "Please Grant Permissions", Toast.LENGTH_SHORT).show();
        }

        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        if (!checkPlayServices()){
            toast("No G-APPS INSTALLED");
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void toast(String msg){
        Toast.makeText(TrackingOrder.this,msg,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTaskDone(Object... values) {

        if (currentPolyline != null){
            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);

    }

}
