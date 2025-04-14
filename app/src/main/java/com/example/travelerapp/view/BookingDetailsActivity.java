package com.example.travelerapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelerapp.R;
import com.example.travelerapp.model.Booking;
import com.example.travelerapp.viewmodel.BookingDetailsViewModel;

public class BookingDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING = "booking";

    private BookingDetailsViewModel viewModel;

    // UI elements
    private ImageView tourImageView;
    private TextView tourNameText;
    private TextView visitorCountText;
    private TextView validDateText;
    private TextView contactDetailsText;
    private TextView locationDetailsText;
    private TextView totalPriceText;
    private Button continueButton;
    private ProgressBar loadingProgress;
    private View contentView;
    private View contactDetailsCard;
    private View locationDetailsButton;
    private View priceDetailsButton;
    private View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        // Initialize views
        initViews();

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(BookingDetailsViewModel.class);

        // Get booking from intent
        Booking booking = getIntent().getParcelableExtra(EXTRA_BOOKING);
        if (booking == null) {
            Toast.makeText(this, "Booking information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize the view model with booking
        viewModel.initialize(booking);

        // Observe data changes
        observeViewModel();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        tourImageView = findViewById(R.id.tour_image);
        tourNameText = findViewById(R.id.tour_name);
        visitorCountText = findViewById(R.id.visitor_count);
        validDateText = findViewById(R.id.valid_date_text);
        contactDetailsText = findViewById(R.id.contact_details_text);
        locationDetailsText = findViewById(R.id.location_details_text);
        totalPriceText = findViewById(R.id.total_price_text);
        continueButton = findViewById(R.id.continue_button);
        loadingProgress = findViewById(R.id.loading_progress);
        contentView = findViewById(R.id.content_container);
        contactDetailsCard = findViewById(R.id.contact_details_card);
        locationDetailsButton = findViewById(R.id.location_details_button);
        priceDetailsButton = findViewById(R.id.price_details_button);
        backButton = findViewById(R.id.back_button);
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.isLoading().observe(this, isLoading -> {
            loadingProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            contentView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        // Observe booking details
        viewModel.getBookingDetails().observe(this, bookingDetails -> {
            if (bookingDetails != null) {
                // Set tour image
                if (bookingDetails.getTourImageResourceId() != 0) {
                    tourImageView.setImageResource(bookingDetails.getTourImageResourceId());
                }

                // Set tour name
                tourNameText.setText(bookingDetails.getTourName());

                // Set visitor count
                visitorCountText.setText(bookingDetails.getVisitorSummary());

                // Set valid date
                validDateText.setText(viewModel.formatDate(bookingDetails.getBookingDate()));

                // Set total price
                totalPriceText.setText(viewModel.formatPrice(bookingDetails.getTotalPrice()));

                // Update contact details status
                if (bookingDetails.isContactFilled()) {
                    contactDetailsText.setText(bookingDetails.getContactName());
                } else {
                    contactDetailsText.setText("Fill in Contact Details");
                }

                // Update location details status
                if (bookingDetails.isLocationFilled()) {
                    locationDetailsText.setText(bookingDetails.getPickupLocation());
                } else {
                    locationDetailsText.setText("Location Details");
                }

                // Update continue button state
                continueButton.setEnabled(bookingDetails.isReadyToProceed());
            }
        });

        // Observe booking creation
        viewModel.isBookingCreated().observe(this, isCreated -> {
            if (isCreated) {
                Toast.makeText(this, "Booking created successfully!", Toast.LENGTH_LONG).show();
                // Navigate to payment or confirmation screen
                navigateToPayment();
            }
        });
    }

    private void setClickListeners() {
        backButton.setOnClickListener(v -> finish());

        contactDetailsCard.setOnClickListener(v -> {
            showContactDetailsDialog();
        });

        locationDetailsButton.setOnClickListener(v -> {
            showLocationDetailsDialog();
        });

        priceDetailsButton.setOnClickListener(v -> {
            showPriceDetailsDialog();
        });

        continueButton.setOnClickListener(v -> {
            if (!viewModel.checkUserLoggedIn()) {
                // If not logged in, redirect to login
                Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return;
            }

            // Create the booking
            viewModel.createBooking();
        });
    }

    private void showContactDetailsDialog() {
        // In a real app, you would show a dialog or navigate to a screen to collect contact details
        // For this example, we'll just set some dummy data
        viewModel.setContactDetails(
                "John Doe",
                "john.doe@example.com",
                "+84 123 456 789"
        );

        Toast.makeText(this, "Contact details updated", Toast.LENGTH_SHORT).show();
    }

    private void showLocationDetailsDialog() {
        // In a real app, you would show a dialog or navigate to a screen to collect location details
        // For this example, we'll just set some dummy data
        viewModel.setLocationDetails(
                "Hotel ABC, 123 Main St, Da Nang",
                "Hotel ABC, 123 Main St, Da Nang"
        );

        Toast.makeText(this, "Location details updated", Toast.LENGTH_SHORT).show();
    }

    private void showPriceDetailsDialog() {
        // In a real app, you would show a dialog or screen with price breakdown
        // For this example, we'll just show a toast
        Toast.makeText(this, "Price includes all taxes and fees", Toast.LENGTH_SHORT).show();
    }

    private void navigateToPayment() {
        // In a real app, you would navigate to a payment screen
        // For this example, we'll just go back to the main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}