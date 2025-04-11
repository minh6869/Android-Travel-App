package com.example.travelerapp.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelerapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivityTest extends AppCompatActivity {

    private static final String TAG = "DashboardActivityTest";

    private TextView tvWelcomeUser;
    private ConstraintLayout signInContainer;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            // Allow content to be displayed under status bar
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        try {
            setContentView(R.layout.activity_dashboard_test);

            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Get references to views
            tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
            signInContainer = findViewById(R.id.signInContainer);

            // Adjust padding for welcome section
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Adjust padding for welcomeSection to avoid being covered by status bar
                View welcomeSection = findViewById(R.id.welcomeSection);
                if (welcomeSection != null) {
                    welcomeSection.setPadding(
                            welcomeSection.getPaddingLeft(),
                            systemBars.top + 10, // Add padding on top
                            welcomeSection.getPaddingRight(),
                            welcomeSection.getPaddingBottom()
                    );
                }

                return insets;
            });

            Log.d(TAG, "Views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
        setupBottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUIForAuthState();
    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.homeFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.profileFragment) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
//            else if (itemId == R.id.nav_search) {
//                startActivity(new Intent(this, BookingsActivity.class));
//                return true;
//            }
//            else if (itemId == R.id.nav_notifications) {
//                startActivity(new Intent(this, SavedItemsActivity.class));
//                return true;
//            }
            else if (itemId == R.id.homeFragment) {
                return true; // Already in ProfileActivity
            }
            return false;
        });
    }

    private void updateUIForAuthState() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                // User is logged in
                String displayName = currentUser.getDisplayName();

                if (displayName != null && !displayName.isEmpty()) {
                    tvWelcomeUser.setText(displayName);
                } else {
                    String email = currentUser.getEmail();
                    if (email != null) {
                        String username = email.split("@")[0];
                        username = username.substring(0, 1).toUpperCase() + username.substring(1);
                        tvWelcomeUser.setText(username);
                    }
                }

                // Hide sign in container for logged in users
                if (signInContainer != null) {
                    signInContainer.setVisibility(View.GONE);
                }
            } else {
                // User is not logged in - use string resource
                tvWelcomeUser.setText(R.string.guest_name);

                // Show sign in container for guests
                if (signInContainer != null) {
                    signInContainer.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI for auth state", e);

            // Set default state in case of error
            if (tvWelcomeUser != null) {
                tvWelcomeUser.setText(R.string.welcome_default);
            }
        }
    }

    /**
     * Called when the Sign In button is clicked from XML
     */
    public void navigateToLogin(View view) {
        try {
            Log.d(TAG, "navigateToLogin method called");
            Intent intent = new Intent(DashboardActivityTest.this, LoginActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to login", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIForAuthState();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}