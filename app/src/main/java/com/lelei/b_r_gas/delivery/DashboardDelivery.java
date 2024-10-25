package com.lelei.b_r_gas.delivery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lelei.b_r_gas.R;

public class DashboardDelivery extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_delivery);

        BottomNavigationView navView = findViewById(R.id.nav_view_delivery);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_delivery);

        NavigationUI.setupWithNavController(navView, navController);
    }
}