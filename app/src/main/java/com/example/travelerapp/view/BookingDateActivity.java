package com.example.travelerapp.view;

import android.app.DatePickerDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelerapp.R;
import com.example.travelerapp.model.Booking;
import com.example.travelerapp.model.BookingDateOption;
import com.example.travelerapp.view.adapter.DateOptionAdapter;
import com.example.travelerapp.viewmodel.BookingDateViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BookingDateActivity extends AppCompatActivity implements DateOptionAdapter.OnDateSelectedListener {

    public static final String EXTRA_TOUR_ID = "tour_id";

    private BookingDateViewModel viewModel;
    private DateOptionAdapter dateAdapter;

    // UI elements
    private RecyclerView dateRecyclerView;
    private ImageView tourImageView;
    private TextView tourNameText;
    private TextView visitorCountText;
    private TextView totalPriceText;
    private TextView visitorsCountText;
    private Button bookNowButton;
    private Button decreaseVisitorsButton;
    private Button increaseVisitorsButton;
    private ProgressBar loadingProgress;
    private View contentView;
    private View calendarButton;
    private View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        // Initialize views
        initViews();

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(BookingDateViewModel.class);

        // Set up date recycler view
        setupDateRecyclerView();

        // Get tour ID from intent
        String tourId = getIntent().getStringExtra(EXTRA_TOUR_ID);
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Tour information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize the view model with tour ID
        viewModel.initialize(tourId);

        // Observe data changes
        observeViewModel();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        tourImageView = findViewById(R.id.tour_image);
        tourNameText = findViewById(R.id.tour_name);
        visitorCountText = findViewById(R.id.visitor_count);
        totalPriceText = findViewById(R.id.total_price);
        visitorsCountText = findViewById(R.id.visitors_count_text);
        bookNowButton = findViewById(R.id.book_now_button);
        decreaseVisitorsButton = findViewById(R.id.decrease_visitors_button);
        increaseVisitorsButton = findViewById(R.id.increase_visitors_button);
        loadingProgress = findViewById(R.id.loading_progress);
        contentView = findViewById(R.id.content_container);
        dateRecyclerView = findViewById(R.id.date_recycler_view);
        calendarButton = findViewById(R.id.calendar_button);
        backButton = findViewById(R.id.back_button);
    }

    private void setupDateRecyclerView() {
        dateAdapter = new DateOptionAdapter(this, new ArrayList<>(), this);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateRecyclerView.setAdapter(dateAdapter);
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

        // Observe tour details
        viewModel.getTour().observe(this, tour -> {
            if (tour != null) {
                // Set tour image
                if (tour.getImageResourceId() != 0) {
                    tourImageView.setImageResource(tour.getImageResourceId());
                }

                // Set tour name
                tourNameText.setText(tour.getTitle());
            }
        });

        // Observe booking details
        viewModel.getBooking().observe(this, booking -> {
            if (booking != null) {
                // Update visitor count
                visitorCountText.setText(booking.getVisitorSummary());
                visitorsCountText.setText(String.valueOf(booking.getNumberOfPerson()));

                // Update total price
                totalPriceText.setText(viewModel.formatPrice(booking.getTotalPrice()));
            }
        });

        // Observe date options
        viewModel.getDateOptions().observe(this, dateOptions -> {
            if (dateOptions != null) {
                dateAdapter.updateData(dateOptions);
            }
        });

        // Observe booking completion
        viewModel.isBookingComplete().observe(this, isComplete -> {
            if (isComplete) {
                Toast.makeText(this, "Booking successful!", Toast.LENGTH_LONG).show();
                // Navigate to confirmation or home screen
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        // Observe login state
        viewModel.getUserLoggedInStatus().observe(this, isLoggedIn -> {
            // You could update UI based on login state if needed
        });
    }

    private void setClickListeners() {
        bookNowButton.setOnClickListener(v -> {
            if (!viewModel.checkUserLoggedIn()) {
                // If not logged in, redirect to login
                Toast.makeText(this, "Please log in to book this tour", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return;
            }

            // Navigate to booking details screen
            navigateToBookingDetails();
        });

        calendarButton.setOnClickListener(v -> {
            showDatePickerDialog();
        });

        backButton.setOnClickListener(v -> finish());

        decreaseVisitorsButton.setOnClickListener(v -> {
            int currentCount = Integer.parseInt(visitorsCountText.getText().toString());
            if (currentCount > 1) {
                viewModel.updateVisitorCount(currentCount - 1);
            }
        });

        increaseVisitorsButton.setOnClickListener(v -> {
            int currentCount = Integer.parseInt(visitorsCountText.getText().toString());
            viewModel.updateVisitorCount(currentCount + 1);
        });
    }

    private void showDatePickerDialog() {
        // Get current date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    // Handle the selected date
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth);

                    // Try to select this date in our view model
                    viewModel.selectDateByCalendar(selectedDate);
                }, year, month, day);

        // Set min date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        // Set max date to 3 months from now
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 3);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }


    private void navigateToBookingDetails() {
        Booking booking = viewModel.getBooking().getValue();
        if (booking != null) {
            Intent intent = new Intent(this, BookingDetailsActivity.class);
            intent.putExtra(BookingDetailsActivity.EXTRA_BOOKING, booking);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Booking information not available", Toast.LENGTH_SHORT).show();
        }
    }





    @Override
    public void onDateSelected(int position) {
        viewModel.selectDate(position);
    }
}