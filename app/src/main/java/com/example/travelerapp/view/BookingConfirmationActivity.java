package com.example.travelerapp.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.travelerapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "BookingConfirmation";
    public static final String EXTRA_BOOKING_ID = "extra_booking_id";

    private TextView bookingIdText;
    private TextView tourTitleText;
    private TextView tourDateText;
    private TextView travelersCountText;
    private TextView totalPaymentText;
    private ImageView tourImageView;
    private ImageView copyIcon;
    private Button returnHomeButton;
    private Button viewBookingsButton;
    private CardView tourDetailsCard;

    private String bookingId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipt);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Get booking ID from intent
        bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "No booking information available", Toast.LENGTH_SHORT).show();
            navigateToMain();
            return;
        }

        // Set booking ID text
        bookingIdText.setText(bookingId);

        // Load booking details
        loadBookingDetails(bookingId);

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        bookingIdText = findViewById(R.id.booking_id_text);
        tourTitleText = findViewById(R.id.tour_title_text);
        tourDateText = findViewById(R.id.tour_date_text);
        tourImageView = findViewById(R.id.tour_image);
        copyIcon = findViewById(R.id.copy_icon);
        returnHomeButton = findViewById(R.id.return_home_button);
        viewBookingsButton = findViewById(R.id.view_bookings_button);
        tourDetailsCard = findViewById(R.id.tour_details_card);
        travelersCountText = findViewById(R.id.travelers_count_text);
        totalPaymentText = findViewById(R.id.total_payment_text);
    }

    private void setupClickListeners() {
        // Copy booking ID to clipboard
        copyIcon.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Booking ID", bookingId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Booking ID copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // Return to home button
        returnHomeButton.setOnClickListener(v -> navigateToMain());

        // View bookings button (if visible)
        viewBookingsButton.setOnClickListener(v -> {
            // Navigate to bookings list activity
            // Intent intent = new Intent(BookingConfirmationActivity.this, BookingsListActivity.class);
            // startActivity(intent);
            // finish();

            // For now, just show a toast
            Toast.makeText(this, "View bookings feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBookingDetails(String bookingId) {
        db.collection("bookings").document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUI(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Booking not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading booking details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(DocumentSnapshot document) {
        // Get data from document
        String tourId = document.getString("tourId");
        Date tourDate = document.getDate("tourDateStart");
        Long numberOfPersons = document.getLong("numberOfPerson");
        Double totalPrice = document.getDouble("totalPrice");

        // Format date
        if (tourDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            tourDateText.setText(sdf.format(tourDate));
        }

        // Set number of travelers
        if (numberOfPersons != null) {
            travelersCountText.setText(String.valueOf(numberOfPersons));
            tourDetailsCard.setVisibility(View.VISIBLE);
        } else {
            tourDetailsCard.setVisibility(View.GONE);
        }

        // Set total payment
        if (totalPrice != null) {
            totalPaymentText.setText(String.format(Locale.getDefault(), "%,.0f VND", totalPrice));
        }

        // Load tour details
        if (tourId != null && !tourId.isEmpty()) {
            loadTourDetails(tourId);
        }
    }

    private void loadTourDetails(String tourId) {
        db.collection("tours").document(tourId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get tour name
                        String tourName = documentSnapshot.getString("title");
                        if (tourName == null) {
                            tourName = documentSnapshot.getString("nameTour");
                        }

                        if (tourName != null && !tourName.isEmpty()) {
                            tourTitleText.setText(tourName);
                        }

                        // Get tour image URL
                        String imageUrl = documentSnapshot.getString("tourImageUrl");
                        if (imageUrl == null) {
                            imageUrl = documentSnapshot.getString("imageUrl");
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            loadImageFromUrl(imageUrl);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Just log error, not critical
                    Log.e(TAG, "Error loading tour details", e);
                    tourTitleText.setText("Tour Details");
                });
    }

    private void loadImageFromUrl(String imageUrl) {
        new DownloadImageTask(tourImageView).execute(imageUrl);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // Static method to start this activity
    public static void start(Context context, String bookingId) {
        Intent intent = new Intent(context, BookingConfirmationActivity.class);
        intent.putExtra(EXTRA_BOOKING_ID, bookingId);
        context.startActivity(intent);
    }

    // AsyncTask to download images without using Glide
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            } catch (Exception e) {
                Log.e("DownloadImageTask", "Error downloading image", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }
}