package com.example.travelerapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelerapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignIn;
    private Button btnSignInGoogle;
    private Button btnSignInFacebook;
    private TextView tvSignUp;
    private TextView tvForgotPassword;
    private ImageButton btnTogglePassword;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        initializeComponents();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeComponents() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle);
        btnSignInFacebook = findViewById(R.id.btnSignInFacebook);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
    }

    private void setupClickListeners() {
        // Sign In button click listener
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // Google Sign In button click listener
        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement Google Sign In functionality
                Toast.makeText(LoginActivity.this, "Google Sign In clicked", Toast.LENGTH_SHORT).show();
                // You would typically integrate with Google Sign In API here
            }
        });

        // Facebook Sign In button click listener
        btnSignInFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement Facebook Sign In functionality
                Toast.makeText(LoginActivity.this, "Facebook Sign In clicked", Toast.LENGTH_SHORT).show();
                // You would typically integrate with Facebook Login SDK here
            }
        });

        // Sign Up text click listener
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SignUp activity
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        // Forgot Password text click listener
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        // Toggle password visibility
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        } else {
            // Show password
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility);
        }
        isPasswordVisible = !isPasswordVisible;

        // Move cursor to end of text
        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Simple validation
        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password không được để trống");
            etPassword.requestFocus();
            return;
        }

        // Thay đổi text của nút để hiển thị trạng thái đang xử lý
        String originalBtnText = btnSignIn.getText().toString();
        btnSignIn.setText("Đang đăng nhập...");

        // Disable login button to prevent multiple attempts
        btnSignIn.setEnabled(false);

        // Disable các input fields trong khi đang xử lý
        setInputFieldsEnabled(false);

        // Thực hiện đăng nhập với Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Khôi phục text của nút
                        btnSignIn.setText(originalBtnText);

                        // Re-enable login button
                        btnSignIn.setEnabled(true);

                        // Kích hoạt lại các input fields
                        setInputFieldsEnabled(true);

                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công",
                                    Toast.LENGTH_SHORT).show();

//                            // Chuyển đến màn hình chính
//                            Intent intent = new Intent(LoginActivity.this, MainActivity.class); // chỗ này đổi mainactiviy thành dashboard nhé
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(intent);
//                            finish(); // Đóng activity đăng nhập
                        } else {
                            // Xử lý các loại lỗi đăng nhập khác nhau
                            handleLoginError(task.getException());
                        }
                    }
                });
    }

    private void setInputFieldsEnabled(boolean enabled) {
        etEmail.setEnabled(enabled);
        etPassword.setEnabled(enabled);
        btnSignInGoogle.setEnabled(enabled);
        btnSignInFacebook.setEnabled(enabled);
        tvForgotPassword.setEnabled(enabled);
        btnTogglePassword.setEnabled(enabled);
    }

    private void handleLoginError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            // Email không tồn tại
            etEmail.setError("Email không tồn tại trong hệ thống");
            etEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            // Sai mật khẩu
            etPassword.setError("Mật khẩu không đúng");
            etPassword.requestFocus();
        } else {
            // Lỗi khác
            Toast.makeText(LoginActivity.this,
                    "Đăng nhập thất bại: " + exception.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void forgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email để khôi phục mật khẩu");
            etEmail.requestFocus();
            return;
        }

        // Hiển thị trạng thái đang xử lý
        tvForgotPassword.setEnabled(false);
        Toast.makeText(this, "Đang gửi email khôi phục...", Toast.LENGTH_SHORT).show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        tvForgotPassword.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Email khôi phục mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Không thể gửi email khôi phục: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Kiểm tra nếu người dùng đã đăng nhập (không null)
        if (mAuth.getCurrentUser() != null) {
            // Người dùng đã đăng nhập, chuyển đến MainActivity
//            startActivity(new Intent(LoginActivity.this, Dashboard.class));
//            finish();
        }
    }
}
