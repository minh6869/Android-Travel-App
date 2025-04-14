package com.example.travelerapp.repository;

import android.util.Log;

import com.example.travelerapp.model.Booking;
import com.example.travelerapp.model.BookingDateOption;
import com.example.travelerapp.model.Tour;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingRepository {
    private static final String TAG = "BookingRepository";

    private FirebaseFirestore db;
    private CollectionReference toursCollection;
    private CollectionReference bookingsCollection;
    private TourRepository tourRepository;

    public BookingRepository() {
        db = FirebaseFirestore.getInstance();
        toursCollection = db.collection("tours");
        bookingsCollection = db.collection("bookings");
        tourRepository = new TourRepository();
    }

    public interface BookingCallback {
        void onBookingDateOptionsLoaded(List<BookingDateOption> dateOptions);
        void onBookingPriceCalculated(double price);
        void onBookingComplete(boolean success, String bookingId);
    }

    public void getAvailableDates(String tourId, BookingCallback callback) {
        Log.d(TAG, "Getting available dates for tour: " + tourId);

        // Get the current date
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        // Query the availableDates subcollection for dates after today
        toursCollection.document(tourId)
                .collection("availableDates")
                .whereGreaterThanOrEqualTo("date", today)
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(14) // Get next 14 days
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<BookingDateOption> dateOptions = new ArrayList<>();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No available dates found in Firestore, generating default dates");
                        // If no dates in Firestore, generate some default ones
                        dateOptions = generateDefaultDates(tourId);
                    } else {
                        Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " available dates in Firestore");
                        // Convert Firestore documents to BookingDateOption objects
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            BookingDateOption option = BookingDateOption.fromFirestore(
                                    document.getId(), document);
                            if (option != null) {
                                dateOptions.add(option);
                            }
                        }

                        // Select the first date by default
                        if (!dateOptions.isEmpty()) {
                            dateOptions.get(0).setSelected(true);
                        }
                    }

                    callback.onBookingDateOptionsLoaded(dateOptions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting available dates: " + e.getMessage());
                    // If there's an error, generate some default dates
                    List<BookingDateOption> defaultDates = generateDefaultDates(tourId);
                    callback.onBookingDateOptionsLoaded(defaultDates);
                });
    }

    private List<BookingDateOption> generateDefaultDates(String tourId) {
        List<BookingDateOption> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // Start from today
        calendar.setTime(new Date());

        // Generate 7 days
        for (int i = 0; i < 7; i++) {
            Date date = calendar.getTime();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Convert to 0-based (0 = Sunday)

            // Set price based on day (weekend is more expensive)
            double basePrice = 775000; // Base price
            boolean isWeekend = (dayOfWeek == 0 || dayOfWeek == 6); // Sunday or Saturday
            boolean isHoliday = false; // Default to not a holiday
            double price = isWeekend ? basePrice * 1.2 : basePrice;

            // Create the date option
            String dateId = String.format(Locale.US, "%s_%tF", tourId, date); // tourId_YYYY-MM-DD
            BookingDateOption option = new BookingDateOption(dateId, date, dayOfWeek, price, isHoliday);

            // If it's the first day, select it by default
            if (i == 0) {
                option.setSelected(true);
            }

            dates.add(option);

            // Move to next day
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    public void calculatePrice(Booking booking, BookingCallback callback) {
        if (booking == null || booking.getSelectedDateOption() == null) {
            callback.onBookingPriceCalculated(0);
            return;
        }

        // Get the base price from the selected date
        double basePrice = booking.getSelectedDateOption().getPrice();

        // Calculate total based on number of persons
        double totalPrice = basePrice * booking.getNumberOfPerson();

        booking.setTotalPrice(totalPrice);
        callback.onBookingPriceCalculated(totalPrice);
    }

    public void createBooking(Booking booking, BookingCallback callback) {
        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot create booking: User not logged in");
            callback.onBookingComplete(false, null);
            return;
        }

        // Ensure we have all required data
        if (booking.getTourId() == null || booking.getTourDateStart() == null) {
            Log.e(TAG, "Cannot create booking: Missing tour ID or date");
            callback.onBookingComplete(false, null);
            return;
        }

        Log.d(TAG, "Creating booking for tour: " + booking.getTourId());

        // Create the booking document in Firestore
        bookingsCollection.add(booking.toFirestore())
                .addOnSuccessListener(documentReference -> {
                    String bookingId = documentReference.getId();
                    Log.d(TAG, "Booking created with ID: " + bookingId);

                    // Update the booking with the ID
                    booking.setId(bookingId);

                    // Update the document with its ID
                    documentReference.update("id", bookingId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Booking document updated with its ID");
                                callback.onBookingComplete(true, bookingId);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Error updating booking with ID", e);
                                // Still consider it successful even if this update fails
                                callback.onBookingComplete(true, bookingId);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating booking", e);
                    callback.onBookingComplete(false, null);
                });
    }
}