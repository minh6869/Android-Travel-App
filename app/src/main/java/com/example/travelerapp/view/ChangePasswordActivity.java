package com.example.travelerapp.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelerapp.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private TextView tvForgotPassword;
    private ImageView btnBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnBack = findViewById(R.id.btnBack);

        // Set click listeners
        btnBack.setOnClickListener(v -> finish());
        btnChangePassword.setOnClickListener(v -> validateAndChangePassword());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void validateAndChangePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Please enter current password");
            etCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Please enter new password");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password should be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your new password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Changing password...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Re-authenticate user
            AuthCredential credential = EmailAuthProvider.getCredential(
                    user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        // User re-authenticated, now update password
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(ChangePasswordActivity.this,
                                            "Password updated successfully",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(ChangePasswordActivity.this,
                                            "Failed to update password: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this,
                                "Current password is incorrect",
                                Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void showForgotPasswordDialog() {
        // Get the current user's email
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Error: User not logged in properly", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();

        // Create dialog to confirm password reset
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Send a password reset email to " + userEmail + "?");
        builder.setPositiveButton("Send", (dialog, which) -> {
            sendPasswordResetEmail(userEmail);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending reset email...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        // Email sent successfully
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Email Sent");
                        builder.setMessage("A password reset email has been sent to your email address. " +
                                "Please check your inbox and follow the instructions.");
                        builder.setPositiveButton("OK", (dialog, which) -> finish());
                        builder.setCancelable(false);
                        builder.show();
                    } else {
                        // Failed to send email
                        Toast.makeText(ChangePasswordActivity.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}