package com.example.travelerapp.view;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelerapp.R;
import com.example.travelerapp.model.Tour;
import com.example.travelerapp.viewmodel.TourDetailViewModel;
import com.google.android.material.button.MaterialButton;

public class TourDetailActivity extends AppCompatActivity {

    private static final String TAG = "TourDetailActivity";
    public static final String EXTRA_TOUR_ID = "tour_id";

    private TourDetailViewModel viewModel;
    private ImageView tourImageView;
    private TextView titleTextView;
    private TextView ratingTextView;
    private TextView descriptionTextView;
    private TextView priceTextView;
    private MaterialButton bookmarkButton;
    private MaterialButton backButton;
    private MaterialButton continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Initialize views
        initViews();

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(TourDetailViewModel.class);

        // Get tour ID from intent
        String tourId = getIntent().getStringExtra(EXTRA_TOUR_ID);
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Tour information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load tour details
        viewModel.loadTourDetails(tourId);

        // Observe tour data
        viewModel.getTourLiveData().observe(this, this::updateUI);

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        tourImageView = findViewById(R.id.tour_image);
        titleTextView = findViewById(R.id.tour_title);
        ratingTextView = findViewById(R.id.tour_rating);
        descriptionTextView = findViewById(R.id.tour_description);
        priceTextView = findViewById(R.id.tour_price);
        bookmarkButton = findViewById(R.id.bookmark_button);
        backButton = findViewById(R.id.back_button);
        continueButton = findViewById(R.id.continue_button);
    }

    private void setClickListeners() {
        backButton.setOnClickListener(v -> finish());

        bookmarkButton.setOnClickListener(v -> {
            viewModel.toggleBookmark();
            updateBookmarkIcon();
        });

        continueButton.setOnClickListener(v -> {
            // Check if tour is loaded
            Tour tour = viewModel.getTourLiveData().getValue();
            if (tour == null) {
                Toast.makeText(this, "Tour information not available yet. Please wait.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navigate to booking/payment screen
            Intent intent = new Intent(this, BookingDateActivity.class);
            intent.putExtra(BookingDateActivity.EXTRA_TOUR_ID, tour.getId());
            startActivity(intent);
        });
    }

    private void updateUI(Tour tour) {
        if (tour == null) {
            Toast.makeText(this, "Tour not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set tour title
        titleTextView.setText(tour.getTitle());

        // Set rating
        String ratingText = tour.getRatingDisplay();
        ratingTextView.setText(ratingText);

        // Set description
        if (tour.getDescription() != null && !tour.getDescription().isEmpty()) {
            descriptionTextView.setText(tour.getDescription());
        } else {
            descriptionTextView.setText("No description available for this tour.");
        }

        // Set price
        priceTextView.setText(tour.getPrice());

        // Load image - using resource ID instead of URL
        if (tour.getImageResourceId() != 0) {
            tourImageView.setImageResource(tour.getImageResourceId());
        } else {
            // Set a default image if no image resource is available
            tourImageView.setImageResource(R.drawable.ic_launcher_background);
        }

        // Update bookmark icon
        updateBookmarkIcon();
    }

    private void updateBookmarkIcon() {
        Tour tour = viewModel.getTourLiveData().getValue();
        if (tour != null) {
            if (tour.isBookmarked()) {
                bookmarkButton.setIcon(getDrawable(R.drawable.bookmarked));
            } else {
                bookmarkButton.setIcon(getDrawable(R.drawable.save_instagram));
            }
        }
    }
}