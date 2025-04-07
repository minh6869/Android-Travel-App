package com.example.travelerapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelerapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    private EditText edtEmail, edtFullName, edtPassword, edtConfirmPassword;
    private ImageView ivPasswordToggle, ivConfirmPasswordToggle;
    private TextView tvLogIn;
    private Button btnSignUp;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        initializeComponents();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeComponents() {
        edtEmail = findViewById(R.id.edtEmail);
        edtFullName = findViewById(R.id.edtFullName);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        ivConfirmPasswordToggle = findViewById(R.id.ivConfirmPasswordToggle);
        tvLogIn = findViewById(R.id.tvLogIn);
        btnSignUp = findViewById(R.id.btnSignup);
    }

    private void setupClickListeners() {
        // Password visibility toggle
        ivPasswordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(edtPassword, ivPasswordToggle, isPasswordVisible);
                isPasswordVisible = !isPasswordVisible;
            }
        });

        // Confirm password visibility toggle
        ivConfirmPasswordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(edtConfirmPassword, ivConfirmPasswordToggle, isConfirmPasswordVisible);
                isConfirmPasswordVisible = !isConfirmPasswordVisible;
            }
        });

        // Login text click listener
        tvLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to login screen
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Sign Up button click listener
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String email = edtEmail.getText().toString().trim();
        final String fullName = edtFullName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Họ tên không được để trống");
            edtFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Mật khẩu không được để trống");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không khớp");
            edtConfirmPassword.requestFocus();
            return;
        }

        // Thay đổi text của nút để hiển thị trạng thái đang xử lý
        String originalBtnText = btnSignUp.getText().toString();
        btnSignUp.setText("Đang đăng ký...");

        // Disable đăng ký button để ngăn nhiều lần nhấn
        btnSignUp.setEnabled(false);

        // Disable các input fields trong khi đang xử lý
        setInputFieldsEnabled(false);

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Khôi phục text của nút
                        btnSignUp.setText(originalBtnText);

                        // Re-enable signup button
                        btnSignUp.setEnabled(true);

                        // Kích hoạt lại các input fields
                        setInputFieldsEnabled(true);

                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Cập nhật thông tin profile người dùng (tên) trong Firebase Auth
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignupActivity.this,
                                                        "Đăng ký thành công", Toast.LENGTH_SHORT).show();


                                            } else {
                                                Toast.makeText(SignupActivity.this,
                                                        "Không thể cập nhật thông tin người dùng",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // If sign up fails, display a message to the user
                            handleSignUpError(task.getException());
                        }
                    }
                });
    }

    private void setInputFieldsEnabled(boolean enabled) {
        edtEmail.setEnabled(enabled);
        edtFullName.setEnabled(enabled);
        edtPassword.setEnabled(enabled);
        edtConfirmPassword.setEnabled(enabled);
        ivPasswordToggle.setEnabled(enabled);
        ivConfirmPasswordToggle.setEnabled(enabled);
        tvLogIn.setEnabled(enabled);
    }

    private void handleSignUpError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            // Email đã tồn tại
            edtEmail.setError("Email này đã được sử dụng");
            edtEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            // Mật khẩu yếu
            edtPassword.setError("Mật khẩu quá yếu, vui lòng chọn mật khẩu mạnh hơn");
            edtPassword.requestFocus();
        } else {
            // Lỗi khác
            Toast.makeText(SignupActivity.this,
                    "Đăng ký thất bại: " + exception.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon, boolean isCurrentlyVisible) {
        if (isCurrentlyVisible) {
            // Hide password
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        } else {
            // Show password
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ic_visibility);
        }
        // Move cursor to the end of text
        editText.setSelection(editText.getText().length());
    }
}