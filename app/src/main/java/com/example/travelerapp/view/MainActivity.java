package com.example.travelerapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.travelerapp.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private ChipNavigationBar chipNavigationBar;
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        try {
            // Initialize Firebase if not done in Application class
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }

            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Set up views
            setupViews();

            // Set up navigation
            setupNavigation();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // No sign-in button in this layout, so we don't try to find it
    }

    private void setupNavigation() {
        try {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);

            if (navHostFragment != null) {
                navController = navHostFragment.getNavController();

                // Setup ChipNavigationBar
                chipNavigationBar = findViewById(R.id.bottom_navigation);
                if (chipNavigationBar != null) {
                    // Set the initially selected item
                    chipNavigationBar.setItemSelected(R.id.homeFragment, true);

                    // The menu resource is already set in the XML layout
                    // app:cnb_menuResource="@menu/bottom_nav_menu"

                    // Set the item selected listener
                    chipNavigationBar.setOnItemSelectedListener(id -> {
                        // Check if user is logged in for restricted destinations
                        if (isRestrictedDestination(id) && !isUserLoggedIn()) {
                            // Prompt for login
                            promptLogin();
                            return;
                        }

                        // Navigate to the selected destination
                        navController.navigate(id);
                    });
                } else {
                    Log.e(TAG, "Bottom navigation not found in layout");
                }
            } else {
                Log.e(TAG, "Nav host fragment not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation: " + e.getMessage(), e);
        }
    }

    private boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    private boolean isRestrictedDestination(int destinationId) {
        // Define which destinations require login
        return destinationId == R.id.profileFragment ||
                destinationId == R.id.bookingFragment ||
                destinationId == R.id.savedFragment;
    }

    private void promptLogin() {
        // Show login prompt
        Toast.makeText(this, "Please sign in to access this feature", Toast.LENGTH_SHORT).show();

        // Navigate to login screen
        startActivity(new Intent(MainActivity.this, LoginActivity.class));

        // For now, just reset the selection to home fragment
        chipNavigationBar.setItemSelected(R.id.homeFragment, true);
    }

    /**
     * Navigate to the profile tab
     * This method is called from HomeFragment when the profile button is clicked
     */
    public void navigateToProfile() {
        // Check if user is logged in before navigating to profile
        if (!isUserLoggedIn()) {
            promptLogin();
            return;
        }

        // Navigate to profile fragment using ChipNavigationBar
        if (chipNavigationBar != null) {
            chipNavigationBar.setItemSelected(R.id.profileFragment, true);
            navController.navigate(R.id.profileFragment);
        }
    }
}