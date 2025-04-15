package com.example.travelerapp.repository;

import androidx.annotation.NonNull;

import com.example.travelerapp.model.Booking;
import com.example.travelerapp.model.PaymentModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PaymentRepository {
    private static final String TAG = "PaymentRepository";

    private final FirebaseFirestore db;

    public PaymentRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface PaymentCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    // Get payment details from booking
    public void getPaymentDetails(String bookingId, PaymentCallback<PaymentModel> callback) {
        db.collection("bookings").document(bookingId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                try {
                                    Booking booking = document.toObject(Booking.class);
                                    if (booking != null) {
                                        booking.setId(document.getId());
                                        PaymentModel paymentModel = new PaymentModel(booking);

                                        // If tour ID exists, get tour details to set duration
                                        if (booking.getTourId() != null && !booking.getTourId().isEmpty()) {
                                            getTourDetails(booking.getTourId(), paymentModel, callback);
                                        } else {
                                            callback.onSuccess(paymentModel);
                                        }
                                    } else {
                                        callback.onError(new Exception("Failed to convert document to Booking"));
                                    }
                                } catch (Exception e) {
                                    callback.onError(e);
                                }
                            } else {
                                callback.onError(new Exception("Booking not found"));
                            }
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    // Get tour details to enhance the payment model
    private void getTourDetails(String tourId, PaymentModel paymentModel, PaymentCallback<PaymentModel> callback) {
        db.collection("tours").document(tourId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Set tour duration if available
                                String duration = document.getString("duration");
                                if (duration != null && !duration.isEmpty()) {
                                    paymentModel.setTourDuration(duration);
                                } else {
                                    paymentModel.setTourDuration(paymentModel.getFormattedDuration());
                                }
                            }
                            callback.onSuccess(paymentModel);
                        } else {
                            // Still return the payment model even if tour details are not found
                            paymentModel.setTourDuration(paymentModel.getFormattedDuration());
                            callback.onSuccess(paymentModel);
                        }
                    }
                });
    }

    // Confirm payment in Firestore
    public void confirmPayment(String bookingId, PaymentCallback<Boolean> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", "completed");
        updates.put("paymentDate", new java.util.Date());

        db.collection("bookings").document(bookingId)
                .update(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(true);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }
}