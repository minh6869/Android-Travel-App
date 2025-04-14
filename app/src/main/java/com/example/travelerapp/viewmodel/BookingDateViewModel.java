package com.example.travelerapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.travelerapp.model.Booking;
import com.example.travelerapp.model.BookingDateOption;
import com.example.travelerapp.model.Tour;
import com.example.travelerapp.repository.BookingRepository;
import com.example.travelerapp.repository.TourRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingDateViewModel extends AndroidViewModel {
    private static final String TAG = "BookingDateViewModel";

    private BookingRepository bookingRepository;
    private TourRepository tourRepository;

    private MutableLiveData<Tour> tourLiveData = new MutableLiveData<>();
    private MutableLiveData<Booking> bookingLiveData = new MutableLiveData<>();
    private MutableLiveData<List<BookingDateOption>> dateOptionsLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(true);
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> bookingCompleteLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isUserLoggedInLiveData = new MutableLiveData<>();

    public BookingDateViewModel(@NonNull Application application) {
        super(application);
        bookingRepository = new BookingRepository();
        tourRepository = new TourRepository();

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        isUserLoggedInLiveData.setValue(currentUser != null);

        // Listen for auth state changes
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            isUserLoggedInLiveData.setValue(user != null);
        });
    }

    public void initialize(String tourId) {
        isLoadingLiveData.setValue(true);

        // Create a new booking
        Booking booking = new Booking();
        booking.setTourId(tourId);
        bookingLiveData.setValue(booking);

        // Load tour details
        tourRepository.getTourById(tourId, tour -> {
            if (tour != null) {
                tourLiveData.setValue(tour);

                // Update booking with tour info
                Booking currentBooking = bookingLiveData.getValue();
                if (currentBooking != null) {
                    currentBooking.setTourName(tour.getTitle());
                    currentBooking.setTourImageUrl(tour.getTourImageUrl());
                    currentBooking.setTourImageResourceId(tour.getImageResourceId());
                    bookingLiveData.setValue(currentBooking);
                }

                // Load available dates
                loadAvailableDates(tourId);
            } else {
                errorMessageLiveData.setValue("Could not load tour details");
                isLoadingLiveData.setValue(false);
            }
        });
    }

    private void loadAvailableDates(String tourId) {
        bookingRepository.getAvailableDates(tourId, new BookingRepository.BookingCallback() {
            @Override
            public void onBookingDateOptionsLoaded(List<BookingDateOption> dateOptions) {
                dateOptionsLiveData.setValue(dateOptions);

                // Find the selected date and update the booking
                for (BookingDateOption option : dateOptions) {
                    if (option.isSelected()) {
                        Booking booking = bookingLiveData.getValue();
                        if (booking != null) {
                            booking.setSelectedDateOption(option);
                            booking.setTourDateStart(option.getDate());
                            calculatePrice(booking);
                        }
                        break;
                    }
                }

                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onBookingPriceCalculated(double price) {
                // This will be called from calculatePrice
            }

            @Override
            public void onBookingComplete(boolean success, String bookingId) {
                // This will be called from createBooking
            }
        });
    }

    public void selectDate(int position) {
        List<BookingDateOption> options = dateOptionsLiveData.getValue();
        if (options == null || position < 0 || position >= options.size()) {
            return;
        }

        // Update selection state
        for (int i = 0; i < options.size(); i++) {
            options.get(i).setSelected(i == position);
        }

        // Update booking with selected date
        BookingDateOption selectedOption = options.get(position);
        Booking booking = bookingLiveData.getValue();
        if (booking != null) {
            booking.setSelectedDateOption(selectedOption);
            booking.setTourDateStart(selectedOption.getDate());
            calculatePrice(booking);
        }

        dateOptionsLiveData.setValue(options);
    }

    public void selectDateByCalendar(Calendar selectedCalendar) {
        List<BookingDateOption> options = dateOptionsLiveData.getValue();
        if (options == null || options.isEmpty()) {
            return;
        }

        int selectedYear = selectedCalendar.get(Calendar.YEAR);
        int selectedMonth = selectedCalendar.get(Calendar.MONTH);
        int selectedDay = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        // Find matching date in our options
        for (int i = 0; i < options.size(); i++) {
            BookingDateOption option = options.get(i);
            Calendar optionCal = Calendar.getInstance();
            optionCal.setTime(option.getDate());

            if (optionCal.get(Calendar.YEAR) == selectedYear &&
                    optionCal.get(Calendar.MONTH) == selectedMonth &&
                    optionCal.get(Calendar.DAY_OF_MONTH) == selectedDay) {

                // Select this date
                selectDate(i);
                return;
            }
        }

        // If no match found, we could either:
        // 1. Do nothing and return false
        // 2. Create a new date option for this date
        // 3. Select the closest date

        // For simplicity, we'll just return false here
        errorMessageLiveData.setValue("Selected date is not available");
    }

    public void updateVisitorCount(int visitorCount) {
        if (visitorCount < 1) {
            return; // Don't allow less than 1 visitor
        }

        Booking booking = bookingLiveData.getValue();
        if (booking != null) {
            booking.setNumberOfPerson(visitorCount);
            calculatePrice(booking);
            bookingLiveData.setValue(booking);
        }
    }

    private void calculatePrice(Booking booking) {
        bookingRepository.calculatePrice(booking, new BookingRepository.BookingCallback() {
            @Override
            public void onBookingDateOptionsLoaded(List<BookingDateOption> dateOptions) {
                // Not used here
            }

            @Override
            public void onBookingPriceCalculated(double price) {
                booking.setTotalPrice(price);
                bookingLiveData.setValue(booking);
            }

            @Override
            public void onBookingComplete(boolean success, String bookingId) {
                // Not used here
            }
        });
    }

    public void createBooking() {
        // Check if user is logged in
        if (!checkUserLoggedIn()) {  // Changed method name here
            errorMessageLiveData.setValue("Please log in to book this tour");
            return;
        }

        Booking booking = bookingLiveData.getValue();
        if (booking != null) {
            isLoadingLiveData.setValue(true);

            bookingRepository.createBooking(booking, new BookingRepository.BookingCallback() {
                @Override
                public void onBookingDateOptionsLoaded(List<BookingDateOption> dateOptions) {
                    // Not used here
                }

                @Override
                public void onBookingPriceCalculated(double price) {
                    // Not used here
                }

                @Override
                public void onBookingComplete(boolean success, String bookingId) {
                    isLoadingLiveData.setValue(false);
                    if (success) {
                        Log.d(TAG, "Booking created successfully with ID: " + bookingId);
                        bookingCompleteLiveData.setValue(true);
                    } else {
                        errorMessageLiveData.setValue("Failed to create booking. Please try again.");
                    }
                }
            });
        }
    }

    // Renamed this method to avoid conflict
    public boolean checkUserLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    // LiveData getters
    public LiveData<Tour> getTour() {
        return tourLiveData;
    }

    public LiveData<Booking> getBooking() {
        return bookingLiveData;
    }

    public LiveData<List<BookingDateOption>> getDateOptions() {
        return dateOptionsLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public LiveData<Boolean> isBookingComplete() {
        return bookingCompleteLiveData;
    }

    // Renamed the method to avoid conflict
    public LiveData<Boolean> getUserLoggedInStatus() {
        return isUserLoggedInLiveData;
    }

    // Helper methods for formatting
    public String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(date);
    }

    public String formatDayOfWeek(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("EEE", Locale.getDefault()).format(date);
    }

    public String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0f VND", price);
    }
}