package com.example.travelerapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travelerapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";
    private static final String EXTRA_BOOKING_ID = "extra_booking_id";

    // Payment time limit in milliseconds (2 hours)
    private static final long PAYMENT_TIME_LIMIT = 2 * 60 * 60 * 1000;

    // UI components
    private ImageView backIcon;
    private TextView paymentCountdown;
    private TextView tourName;
    private TextView tourDuration;
    private TextView tourDate;
    private TextView bookingId;
    private TextView numberOfTravelers;
    private TextView totalPayment;
    private TextView paymentDeadline;
    private Button confirmPaymentButton;

    private String bookingIdValue;
    private FirebaseFirestore db;
    private CountDownTimer countDownTimer;
    private long timeRemaining = PAYMENT_TIME_LIMIT;

    // Static method to start this activity
    public static void start(Context context, String bookingId) {
        if (context == null) {
            Log.e("PaymentActivity", "Cannot start activity with null context");
            return;
        }

        if (bookingId == null || bookingId.isEmpty()) {
            Log.e("PaymentActivity", "Cannot start activity with null or empty booking ID");
            return;
        }

        try {
            Log.d("PaymentActivity", "Starting PaymentActivity with booking ID: " + bookingId);
            Intent intent = new Intent(context, PaymentActivity.class);
            intent.putExtra(EXTRA_BOOKING_ID, bookingId);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("PaymentActivity", "Error starting PaymentActivity", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "PaymentActivity onCreate started");
        setContentView(R.layout.activity_pay);

        // Get booking ID from intent
        Intent intent = getIntent();
        if (intent == null) {
            Log.e(TAG, "Intent is null");
            Toast.makeText(this, "Error: Intent is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (intent.hasExtra(EXTRA_BOOKING_ID)) {
            bookingIdValue = intent.getStringExtra(EXTRA_BOOKING_ID);
            Log.d(TAG, "Received booking ID from intent: " + bookingIdValue);
        } else {
            Log.e(TAG, "No booking ID in intent extras");
            // Try to get from all extras
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Log.d(TAG, "Intent extra: " + key + " = " + extras.get(key));
                    if (extras.get(key) instanceof String && key.toLowerCase().contains("booking")) {
                        bookingIdValue = (String) extras.get(key);
                        Log.d(TAG, "Found potential booking ID: " + bookingIdValue);
                        break;
                    }
                }
            }

            if (bookingIdValue == null || bookingIdValue.isEmpty()) {
                Toast.makeText(this, "No booking information provided", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        try {
            // Initialize UI components
            initializeViews();

            // Setup UI elements
            setupUI();

            // Start countdown timer
            startCountdownTimer();

            // Load payment details
            loadPaymentDetails(bookingIdValue);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing payment activity", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            backIcon = findViewById(R.id.backIcon);
            paymentCountdown = findViewById(R.id.paymentCountdown);
            tourName = findViewById(R.id.tourName);
            tourDuration = findViewById(R.id.tourDuration);
            tourDate = findViewById(R.id.tourDate);
            bookingId = findViewById(R.id.bookingId);
            numberOfTravelers = findViewById(R.id.numberOfTravelers);
            totalPayment = findViewById(R.id.totalPayment);
            paymentDeadline = findViewById(R.id.paymentDeadline);
            confirmPaymentButton = findViewById(R.id.confirmPaymentButton);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error finding views", e);
            throw e;
        }
    }

    private void setupUI() {
        // Set click listeners
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Back button clicked");
                onBackPressed();
            }
        });

        confirmPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Confirm payment button clicked");
                confirmPayment();
            }
        });

        // Set default values
        bookingId.setText(bookingIdValue);

        // Set payment deadline (2 hours from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 2);
        Date deadline = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
        paymentDeadline.setText("Please pay before: " + sdf.format(deadline));
    }

    private void startCountdownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateCountdownDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                paymentCountdown.setText("Payment time expired!");
                confirmPaymentButton.setEnabled(false);
                confirmPaymentButton.setText("Payment Time Expired");

                // You might want to update the booking status in Firestore to "expired"
                Toast.makeText(PaymentActivity.this, "Payment time has expired", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void updateCountdownDisplay(long millisUntilFinished) {
        long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
        millisUntilFinished -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
        millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

        // Format the time as "HH : MM : SS"
        String timeFormatted = String.format(Locale.getDefault(),
                "Payment within %02d : %02d : %02d", hours, minutes, seconds);

        paymentCountdown.setText(timeFormatted);

        // Change text color to red when less than 15 minutes remain
        if (hours == 0 && minutes < 15) {
            paymentCountdown.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }

    private void loadPaymentDetails(String bookingId) {
        // Show loading state
        Log.d(TAG, "Loading payment details for booking: " + bookingId);
        confirmPaymentButton.setEnabled(false);

        // Load booking details from Firestore
        db.collection("bookings").document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Booking document retrieved successfully");
                    if (documentSnapshot.exists()) {
                        updateUI(documentSnapshot);
                    } else {
                        Log.e(TAG, "Booking document does not exist");
                        Toast.makeText(this, "Booking not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    confirmPaymentButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading booking details", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    confirmPaymentButton.setEnabled(true);
                });
    }

    private void updateUI(DocumentSnapshot document) {
        try {
            Log.d(TAG, "Updating UI with document data: " + document.getData());

            // Set booking ID
            bookingId.setText(document.getId());

            // Set tour details
            String tourId = document.getString("tourId");

            // Check if tour name is directly in the booking
            String tourNameValue = document.getString("tourName");
            if (tourNameValue != null && !tourNameValue.isEmpty()) {
                tourName.setText(tourNameValue);
            } else if (tourId != null) {
                // If not, load from tours collection
                loadTourDetails(tourId);
            } else {
                Log.w(TAG, "Tour ID is null in booking document");
                tourName.setText("Tour Details");
            }

            // Set number of travelers
            Long numberOfPersons = document.getLong("numberOfPerson");
            if (numberOfPersons != null) {
                numberOfTravelers.setText(String.valueOf(numberOfPersons));
            } else {
                Log.w(TAG, "Number of persons is null in booking document");
                numberOfTravelers.setText("1"); // Default value
            }

            // Set total payment
            Double totalPriceValue = document.getDouble("totalPrice");
            if (totalPriceValue != null) {
                totalPayment.setText(String.format("%,.0f VND", totalPriceValue));
            } else {
                Log.w(TAG, "Total price is null in booking document");
                totalPayment.setText("0 VND"); // Default value
            }

            // Set payment status
            String paymentStatus = document.getString("paymentStatus");
            if ("completed".equals(paymentStatus)) {
                confirmPaymentButton.setEnabled(false);
                confirmPaymentButton.setText("Payment Completed");
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                paymentCountdown.setText("Payment completed");

                // If payment is already completed, navigate to confirmation screen
                navigateToBookingConfirmation(bookingIdValue);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
        }
    }

    private void loadTourDetails(String tourId) {
        Log.d(TAG, "Loading tour details for tour ID: " + tourId);
        db.collection("tours").document(tourId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Tour document retrieved successfully");

                        // Try to get title from different fields
                        String title = documentSnapshot.getString("title");
                        if (title == null) {
                            title = documentSnapshot.getString("nameTour");
                        }
                        if (title != null) {
                            tourName.setText(title);
                        } else {
                            Log.w(TAG, "Tour title not found in document");
                            tourName.setText("Tour Details"); // Default value
                        }

                        // Set duration (default to 3 days if not found)
                        tourDuration.setText("Duration: 3 days");

                        // Set date range (default for demo)
                        tourDate.setText("Date: 18/03/2025 - 21/03/2025");
                    } else {
                        Log.w(TAG, "Tour document does not exist");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading tour details", e));
    }

    private void confirmPayment() {
        Log.d(TAG, "Confirming payment for booking: " + bookingIdValue);
        confirmPaymentButton.setEnabled(false);
        confirmPaymentButton.setText("Processing...");

        // Update payment status in Firestore
        db.collection("bookings").document(bookingIdValue)
                .update("paymentStatus", "completed", "paymentDate", new java.util.Date())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Payment confirmed successfully");
                    confirmPaymentButton.setText("Payment Completed");

                    // Stop the countdown timer
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    paymentCountdown.setText("Payment completed");

                    Toast.makeText(this, "Payment confirmed successfully", Toast.LENGTH_SHORT).show();

                    // Navigate to booking confirmation screen
                    navigateToBookingConfirmation(bookingIdValue);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error confirming payment", e);
                    confirmPaymentButton.setEnabled(true);
                    confirmPaymentButton.setText("Confirm Payment");
                    Toast.makeText(this, "Payment confirmation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigate to the BookingConfirmationActivity with the booking ID
     */
    private void navigateToBookingConfirmation(String bookingId) {
        // Add a small delay to show the success message
        new android.os.Handler().postDelayed(() -> {
            try {
                Log.d(TAG, "Navigating to BookingConfirmationActivity with booking ID: " + bookingId);
                Intent intent = new Intent(PaymentActivity.this, BookingConfirmationActivity.class);
                intent.putExtra("extra_booking_id", bookingId);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to BookingConfirmationActivity", e);
                Toast.makeText(this, "Error opening confirmation screen", Toast.LENGTH_SHORT).show();

                // Fallback to MainActivity if confirmation screen fails
                Intent mainIntent = new Intent(PaymentActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                finish();
            }
        }, 1500); // 1.5 second delay
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the timer when the activity is not visible
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the timer when the activity becomes visible again
        if (timeRemaining > 0) {
            startCountdownTimer();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "PaymentActivity onDestroy");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}