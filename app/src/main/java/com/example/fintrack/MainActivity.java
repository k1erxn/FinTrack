package com.example.fintrack;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.fintrack.databinding.ActivityMainBinding;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;  // view binding for layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());  // inflate layout binding
        setContentView(binding.getRoot());  // set content view to binding root

        setSupportActionBar(binding.toolbar);  // use toolbar as action bar

        BottomNavigationView navView = binding.navView;  // bottom navigation view reference
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_history,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
        ).build();

        NavController navController = Navigation.findNavController(
                this,
                R.id.nav_host_fragment_activity_main  // nav host fragment id
        );

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);  // link action bar with nav controller
        NavigationUI.setupWithNavController(navView, navController);  // link nav view with nav controller
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(
                this,
                R.id.nav_host_fragment_activity_main  // get nav controller
        );
        return navController.navigateUp() || super.onSupportNavigateUp();  // handle navigation
    }
}
