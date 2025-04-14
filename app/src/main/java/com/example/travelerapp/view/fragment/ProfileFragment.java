package com.example.travelerapp.view.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.travelerapp.R;
import com.example.travelerapp.view.ChangePasswordActivity;
import com.example.travelerapp.view.EditProfileActivity;
import com.example.travelerapp.view.LoginActivity;
import com.example.travelerapp.view.MainActivity;
import com.example.travelerapp.view.SignupActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // UI components
    private ScrollView scrollView;
    private RelativeLayout loggedOutUI;
    private Button btnLogin, btnRegister;
    private TextView tvUserName, tvUserEmail;
    private ImageView profileImage;
    private LinearLayout editProfileOption, savedItemsOption, paymentHistoryOption;
    private LinearLayout changePasswordOption, logoutOption;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        initializeViews(view);
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update UI based on authentication state when fragment resumes
        updateUI(mAuth.getCurrentUser());
    }

    private void initializeViews(View view) {
        // Main containers
        scrollView = view.findViewById(R.id.scrollView);
        loggedOutUI = view.findViewById(R.id.loggedOutUI);

        // Logged out UI buttons
        btnLogin = view.findViewById(R.id.btnLogin);
        btnRegister = view.findViewById(R.id.btnRegister);

        // User info views
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        profileImage = view.findViewById(R.id.profileImage);

        // Profile options
        editProfileOption = view.findViewById(R.id.editProfileOption);
        savedItemsOption = view.findViewById(R.id.savedItemsOption);
        paymentHistoryOption = view.findViewById(R.id.paymentHistoryOption);

        // Account settings
        changePasswordOption = view.findViewById(R.id.changePasswordOption);
        logoutOption = view.findViewById(R.id.logoutOption);
    }

    private void setupClickListeners() {
        // Login and Register buttons (shown when logged out)
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SignupActivity.class));
        });

        // Profile options (shown when logged in)
        editProfileOption.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        savedItemsOption.setOnClickListener(v -> {
            // Navigate to saved items
            Toast.makeText(getContext(), "Saved Items clicked", Toast.LENGTH_SHORT).show();
        });

        paymentHistoryOption.setOnClickListener(v -> {
            // Navigate to payment history
            Toast.makeText(getContext(), "Payment History clicked", Toast.LENGTH_SHORT).show();
        });

        // Account settings
        changePasswordOption.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
        });

        logoutOption.setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in, show profile UI
            scrollView.setVisibility(View.VISIBLE);
            loggedOutUI.setVisibility(View.GONE);

            // Update profile information
            loadUserProfile(user);
        } else {
            // User is signed out, show login UI
            scrollView.setVisibility(View.GONE);
            loggedOutUI.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserProfile(FirebaseUser user) {
        // Set email from Firebase Auth
        tvUserEmail.setText(user.getEmail());

        // Set display name (if available)
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            tvUserName.setText(user.getDisplayName());
        } else {
            tvUserName.setText("User");
        }

        // Load profile image (if available)
        if (user.getPhotoUrl() != null) {
            loadImageFromUrl(user.getPhotoUrl().toString());
        }

        // Get additional user data from Firestore
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUserProfileFromFirestore(documentSnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserProfileFromFirestore(DocumentSnapshot document) {
        // Update user name if available in Firestore
        String name = document.getString("name");
        if (name != null && !name.isEmpty()) {
            tvUserName.setText(name);
        }

        // Update profile image if available in Firestore
        String avatarUrl = document.getString("avatarUrl");
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            loadImageFromUrl(avatarUrl);
        }
    }

    private void loadImageFromUrl(String imageUrl) {
        // Use ExecutorService to load image from URL (modern approach)
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Bitmap bitmap = null;

            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);

                Bitmap finalBitmap = bitmap;
                handler.post(() -> {
                    if (finalBitmap != null && profileImage != null) {
                        profileImage.setImageBitmap(finalBitmap);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                handler.post(() -> {
                    // Set default image on error
                    profileImage.setImageResource(R.drawable.user__1_);
                });
            }
        });
    }

    private void logoutUser() {
        // Create and customize alert dialog for better visibility
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom);
        builder.setTitle("Log Out");
        builder.setMessage("Are you sure you want to log out?");

        // Customize positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Sign out from Firebase
            mAuth.signOut();

            // Update UI
            updateUI(null);

            // Show success message
            // Restart MainActivity
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish(); // Close the current activity
            }
        });

        // Customize negative button
        builder.setNegativeButton("No", null);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Make buttons more visible by changing their color
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(Color.BLUE);
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(Color.RED);
        }
    }



}
