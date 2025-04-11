package com.example.travelerapp.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelerapp.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhoneNumber;
    private ShapeableImageView ivAvatar;
    private Button btnSave;
    private ImageView btnBack;
    private Uri avatarUri;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // Load current user data
        loadUserData();

        // Handle avatar click
        ivAvatar.setOnClickListener(v -> selectAvatar());

        // Handle save button click
        btnSave.setOnClickListener(v -> saveUserData());

        // Handle back button click
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            etEmail.setText(currentUser.getEmail());

            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Set full name
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            etFullName.setText(name);
                        }

                        // Set phone number if exists
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            etPhoneNumber.setText(phoneNumber);
                        }

                        // Load avatar if available
                        String avatarUrl = documentSnapshot.getString("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Picasso.get().load(avatarUrl)
                                .placeholder(R.drawable.user)
                                .error(R.drawable.user)
                                .into(ivAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void selectAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            avatarUri = data.getData();
            ivAvatar.setImageURI(avatarUri);
        }
    }

    private void saveUserData() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Saving...");
            progressDialog.show();

            // Update Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", fullName);
            updates.put("email", email);

            if (!phoneNumber.isEmpty()) {
                updates.put("phoneNumber", phoneNumber);
            }

            db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update avatar if selected
                    if (avatarUri != null) {
                        uploadAvatar(currentUser.getUid(), progressDialog);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void uploadAvatar(String userId, ProgressDialog progressDialog) {
        StorageReference avatarRef = storage.getReference().child("avatars/" + userId + ".jpg");

        avatarRef.putFile(avatarUri)
            .addOnSuccessListener(taskSnapshot ->
                avatarRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        db.collection("users").document(userId)
                            .update("avatarUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                    })
            )
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to upload avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}