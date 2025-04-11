package com.example.travelerapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travelerapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private TextView tvUserName, tvUserEmail;
    private ShapeableImageView profileImage;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private View loggedInUI;
    private View loggedOutUI;
    private Button btnLogin, btnRegister;
    private LinearLayout editProfileOption, savedItemsOption, paymentHistoryOption, changePasswordOption, logoutOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        profileImage = findViewById(R.id.profileImage);
        loggedInUI = findViewById(R.id.loggedInUI);
        loggedOutUI = findViewById(R.id.loggedOutUI);

        // Find views for logged-in state
        editProfileOption = findViewById(R.id.editProfileOption);
        savedItemsOption = findViewById(R.id.savedItemsOption);
        paymentHistoryOption = findViewById(R.id.paymentHistoryOption);
        changePasswordOption = findViewById(R.id.changePasswordOption);
        logoutOption = findViewById(R.id.logoutOption);

        // Find views for logged-out state
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Set click listeners for logged-in state
        if (editProfileOption != null) {
            editProfileOption.setOnClickListener(v -> navigateToEditProfile());
        }

        if (savedItemsOption != null) {
            savedItemsOption.setOnClickListener(v -> {
                // Handle saved items click
                Log.d(TAG, "Saved items clicked");
            });
        }

        if (paymentHistoryOption != null) {
            paymentHistoryOption.setOnClickListener(v -> {
                // Handle payment history click
                Log.d(TAG, "Payment history clicked");
            });
        }

        if (changePasswordOption != null) {
            changePasswordOption.setOnClickListener(v -> navigateToChangePassword());
        }

        if (logoutOption != null) {
            logoutOption.setOnClickListener(v -> performLogout());
        }

        // Set click listeners for logged-out state
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> navigateToLogin());
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> navigateToRegister());
        }

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check authentication state and update UI
        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            // User is logged in
            if (loggedInUI != null) loggedInUI.setVisibility(View.VISIBLE);
            if (loggedOutUI != null) loggedOutUI.setVisibility(View.GONE);
            // Load user data
            loadUserData();
        } else {
            // User is logged out
            if (loggedInUI != null) loggedInUI.setVisibility(View.GONE);
            if (loggedOutUI != null) loggedOutUI.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && tvUserEmail != null && tvUserName != null) {
            // Set email from Firebase Auth
            tvUserEmail.setText(currentUser.getEmail());

            // Get additional user data from Firestore
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Set user name from Firestore
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvUserName.setText(name);
                        } else {
                            tvUserName.setText(currentUser.getDisplayName() != null ?
                                currentUser.getDisplayName() : "User");
                        }

                        // Load avatar if available
                        String avatarUrl = documentSnapshot.getString("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty() && profileImage != null) {
                            Picasso.get().load(avatarUrl)
                                .placeholder(R.drawable.user)
                                .error(R.drawable.user)
                                .into(profileImage);
                        }
                    } else {
                        // Fallback to display name from Firebase Auth
                        tvUserName.setText(currentUser.getDisplayName() != null ?
                            currentUser.getDisplayName() : "User");
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to display name from Firebase Auth
                    tvUserName.setText(currentUser.getDisplayName() != null ?
                        currentUser.getDisplayName() : "User");
                    Log.e(TAG, "Error loading user data", e);
                });
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.profileFragment);

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.homeFragment) {
                    startActivity(new Intent(this, DashboardActivityTest.class));
                    return true;
                }
                else if (itemId == R.id.profileFragment) {
                    return true; // Already in ProfileActivity
                }
                return false;
            });
        }
    }

    private void navigateToEditProfile() {
        Log.d(TAG, "Navigating to Edit Profile");
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    private void navigateToChangePassword() {
        Log.d(TAG, "Navigating to Change Password");
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    private void performLogout() {
        Log.d(TAG, "Performing Logout");
        // Sign out the user
        mAuth.signOut();
        // Update UI after logout
        updateUI(null);
        // Navigate to login screen
        Intent intent = new Intent(this, DashboardActivityTest.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}