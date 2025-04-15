package com.example.travelerapp.viewmodel;

import android.os.CountDownTimer;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelerapp.model.PaymentModel;
import com.example.travelerapp.repository.PaymentRepository;

public class PaymentViewModel extends ViewModel {
    private static final String TAG = "PaymentViewModel";

    private final PaymentRepository repository;
    private CountDownTimer countDownTimer;

    // LiveData objects
    private final MutableLiveData<PaymentModel> paymentData = new MutableLiveData<>();
    private final MutableLiveData<String> remainingTime = new MutableLiveData<>();
    private final MutableLiveData<PaymentStatus> paymentStatus = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PaymentViewModel(PaymentRepository repository) {
        this.repository = repository;
    }

    // Getters for LiveData
    public LiveData<PaymentModel> getPaymentData() {
        return paymentData;
    }

    public LiveData<String> getRemainingTime() {
        return remainingTime;
    }

    public LiveData<PaymentStatus> getPaymentStatus() {
        return paymentStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Load payment details
    public void loadPaymentDetails(String bookingId) {
        repository.getPaymentDetails(bookingId, new PaymentRepository.PaymentCallback<PaymentModel>() {
            @Override
            public void onSuccess(PaymentModel result) {
                paymentData.postValue(result);
                startCountdownTimer(result.getRemainingTimeInSeconds());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading payment details", e);
                errorMessage.postValue("Failed to load payment details: " + e.getMessage());
            }
        });
    }

    // Start countdown timer
    private void startCountdownTimer(long secondsRemaining) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(secondsRemaining * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;

                String timeString = String.format("%02d : %02d : %02d", hours, minutes, secs);
                remainingTime.postValue(timeString);
            }

            @Override
            public void onFinish() {
                remainingTime.postValue("00 : 00 : 00");
                errorMessage.postValue("Payment time has expired");
            }
        };

        countDownTimer.start();
    }

    // Confirm payment
    public void confirmPayment() {
        PaymentModel payment = paymentData.getValue();
        if (payment == null || payment.getBookingId() == null) {
            errorMessage.postValue("No booking information available");
            return;
        }

        paymentStatus.postValue(PaymentStatus.LOADING);

        repository.confirmPayment(payment.getBookingId(), new PaymentRepository.PaymentCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    // Update local payment model
                    payment.setPaymentStatus("completed");
                    paymentData.postValue(payment);
                    paymentStatus.postValue(PaymentStatus.SUCCESS);
                } else {
                    paymentStatus.postValue(PaymentStatus.ERROR);
                    errorMessage.postValue("Payment confirmation failed");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error confirming payment", e);
                paymentStatus.postValue(PaymentStatus.ERROR);
                errorMessage.postValue("Payment confirmation failed: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    // Payment status enum
    public enum PaymentStatus {
        LOADING,
        SUCCESS,
        ERROR
    }
}