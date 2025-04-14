package com.example.travelerapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.travelerapp.model.Booking;
import com.example.travelerapp.model.BookingDetailsModel;
import com.example.travelerapp.repository.BookingDetailsRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingDetailsViewModel extends AndroidViewModel {
    private static final String TAG = "BookingDetailsViewModel";

    private BookingDetailsRepository repository;
    private MutableLiveData<BookingDetailsModel> bookingDetailsLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> bookingCreatedLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isUserLoggedInLiveData = new MutableLiveData<>();

    public BookingDetailsViewModel(@NonNull Application application) {
        super(application);
        repository = new BookingDetailsRepository();

        // Check if user is logged in
        isUserLoggedInLiveData.setValue(FirebaseAuth.getInstance().getCurrentUser() != null);

        // Listen for auth state changes
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            isUserLoggedInLiveData.setValue(firebaseAuth.getCurrentUser() != null);
        });
    }

    public void initialize(Booking booking) {
        if (booking == null) {
            errorMessageLiveData.setValue("Invalid booking data");
            return;
        }

        BookingDetailsModel bookingDetails = new BookingDetailsModel(booking);
        bookingDetailsLiveData.setValue(bookingDetails);
    }

    public void setContactDetails(String name, String email, String phone) {
        BookingDetailsModel bookingDetails = bookingDetailsLiveData.getValue();
        if (bookingDetails != null) {
            bookingDetails.setContactName(name);
            bookingDetails.setContactEmail(email);
            bookingDetails.setContactPhone(phone);
            bookingDetails.setContactFilled(true);
            bookingDetailsLiveData.setValue(bookingDetails);
        }
    }

    public void setLocationDetails(String pickup, String dropoff) {
        BookingDetailsModel bookingDetails = bookingDetailsLiveData.getValue();
        if (bookingDetails != null) {
            bookingDetails.setPickupLocation(pickup);
            bookingDetails.setDropoffLocation(dropoff);
            bookingDetails.setLocationFilled(true);
            bookingDetailsLiveData.setValue(bookingDetails);
        }
    }

    public void createBooking() {
        BookingDetailsModel bookingDetails = bookingDetailsLiveData.getValue();
        if (bookingDetails == null) {
            errorMessageLiveData.setValue("Booking details not available");
            return;
        }

        if (!bookingDetails.isContactFilled()) {
            errorMessageLiveData.setValue("Please fill in contact details");
            return;
        }

        isLoadingLiveData.setValue(true);

        repository.createBooking(bookingDetails, new BookingDetailsRepository.BookingCallback() {
            @Override
            public void onBookingCreated(boolean success, String bookingId) {
                isLoadingLiveData.setValue(false);

                if (success) {
                    bookingCreatedLiveData.setValue(true);
                } else {
                    errorMessageLiveData.setValue("Failed to create booking. Please try again.");
                }
            }
        });
    }

    // LiveData getters
    public LiveData<BookingDetailsModel> getBookingDetails() {
        return bookingDetailsLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public LiveData<Boolean> isBookingCreated() {
        return bookingCreatedLiveData;
    }

    public LiveData<Boolean> isUserLoggedIn() {
        return isUserLoggedInLiveData;
    }

    // Helper methods
    public String formatDate(Date date) {
        if (date == null) return "";
        return "Valid on " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
    }

    public String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0f VND", price);
    }

    public boolean checkUserLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
}