package com.example.fintrack;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.fintrack.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inflate the activity main xml via view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // find the navhostfragment by its id
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);

        // get the navcontroller from it
        NavController navController = navHostFragment.getNavController();

        // configure top level destinations for the actionbar
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
        ).build();

        // hook up the actionbar with the navcontroller
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // hook up the bottomnavigationview with the navcontroller
        BottomNavigationView navView = binding.navView;
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // avoid memory leaks
        binding = null;
    }
}
