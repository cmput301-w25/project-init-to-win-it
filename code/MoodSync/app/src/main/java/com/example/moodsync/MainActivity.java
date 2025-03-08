/**
 * MainActivity class for the MoodSync application.
 *
 * This class serves as the main entry point of the application and manages the app's navigation
 * and toolbar setup. It extends AppCompatActivity to provide compatibility support for modern Android features.
 */
package com.example.moodsync;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.moodsync.databinding.ActivityMainBinding;
import android.view.Menu;
import android.view.MenuItem;

/**
 * MainActivity manages the app's lifecycle, UI, and navigation.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Configuration object for managing top-level destinations in the navigation graph.
     */
    private AppBarConfiguration appBarConfiguration;

    /**
     * Binding object for accessing views in the activity_main.xml layout file.
     */
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the toolbar as the app bar for this activity
        setSupportActionBar(binding.toolbar);

        // Set up navigation controller and configure action bar with navigation support
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Handle action bar item clicks here
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
