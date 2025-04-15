package com.example.travelerapp.repository;

import android.util.Log;

import com.example.travelerapp.model.Booking;
import com.example.travelerapp.model.BookingDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookingDetailsRepository {
    private static final String TAG = "BookingDetailsRepository";

    private FirebaseFirestore db;
    private CollectionReference bookingsCollection;

    public interface BookingCallback {
        void onBookingCreated(boolean success, String bookingId);
    }

    public BookingDetailsRepository() {
        db = FirebaseFirestore.getInstance();
        bookingsCollection = db.collection("bookings");
    }

    public void createBooking(BookingDetailsModel bookingDetails, BookingCallback callback) {
        try {
            // Check if user is logged in
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.e(TAG, "Cannot create booking: User not logged in");
                callback.onBookingCreated(false, null);
                return;
            }

            // Create a new booking document reference with auto-generated ID
            DocumentReference newBookingRef = bookingsCollection.document();
            String bookingId = newBookingRef.getId();

            // Create a new booking object
            Booking booking = new Booking();
            booking.setId(bookingId); // Set the ID immediately
            booking.setUserId(currentUser.getUid());
            booking.setTourId(bookingDetails.getTourId());
            booking.setTourName(bookingDetails.getTourName()); // Add tour name for easier reference
            booking.setTourDateStart(bookingDetails.getBookingDate());
            booking.setNumberOfPerson(bookingDetails.getVisitorCount());
            booking.setTotalPrice(bookingDetails.getTotalPrice());
            booking.setParticipantName(bookingDetails.getContactName());
            booking.setParticipantEmail(bookingDetails.getContactEmail());
            booking.setParticipantPhoneNumber(bookingDetails.getContactPhone());
            booking.setPaymentStatus("pending");

            Log.d(TAG, "Creating booking document with ID: " + bookingId);

            // Add to Firestore
            newBookingRef.set(booking.toFirestore())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Booking document created successfully with ID: " + bookingId);
                        callback.onBookingCreated(true, bookingId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creating booking document", e);
                        callback.onBookingCreated(false, null);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error creating booking", e);
            callback.onBookingCreated(false, null);
        }
    }
}