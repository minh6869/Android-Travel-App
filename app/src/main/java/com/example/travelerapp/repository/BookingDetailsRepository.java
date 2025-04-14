package com.example.travelerapp.repository;

import android.util.Log;

import com.example.travelerapp.model.Booking;
import com.example.travelerapp.model.BookingDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
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
        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot create booking: User not logged in");
            callback.onBookingCreated(false, null);
            return;
        }

        // Create a new booking document
        Booking booking = new Booking();
        booking.setTourId(bookingDetails.getTourId());
        booking.setTourDateStart(bookingDetails.getBookingDate());
        booking.setNumberOfPerson(bookingDetails.getVisitorCount());
        booking.setTotalPrice(bookingDetails.getTotalPrice());
        booking.setParticipantName(bookingDetails.getContactName());
        booking.setParticipantEmail(bookingDetails.getContactEmail());
        booking.setParticipantPhoneNumber(bookingDetails.getContactPhone());

        // Add to Firestore
        bookingsCollection.add(booking.toFirestore())
                .addOnSuccessListener(documentReference -> {
                    String bookingId = documentReference.getId();
                    Log.d(TAG, "Booking created with ID: " + bookingId);

                    // Update the document with its ID
                    documentReference.update("id", bookingId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Booking document updated with its ID");
                                callback.onBookingCreated(true, bookingId);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Error updating booking with ID", e);
                                // Still consider it successful even if this update fails
                                callback.onBookingCreated(true, bookingId);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating booking", e);
                    callback.onBookingCreated(false, null);
                });
    }
}