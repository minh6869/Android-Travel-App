package com.example.travelerapp.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PaymentModel {
    private String bookingId;
    private String tourId;
    private String tourName;
    private String tourDuration;
    private Date tourStartDate;
    private Date tourEndDate;
    private int numberOfTravelers;
    private double totalAmount;
    private String currency;
    private Date paymentDeadline;
    private long remainingTimeInSeconds;
    private String paymentStatus;

    public PaymentModel() {
        // Default constructor
        this.currency = "VND";
        this.paymentStatus = "pending";
    }

    // Constructor from Booking
    public PaymentModel(Booking booking) {
        this();
        this.bookingId = booking.getId();
        this.tourId = booking.getTourId();
        this.tourName = booking.getTourName();
        this.tourStartDate = booking.getTourDateStart();
        // Assuming a 3-day tour by default, can be modified
        this.tourEndDate = new Date(booking.getTourDateStart().getTime() + TimeUnit.DAYS.toMillis(3));
        this.numberOfTravelers = booking.getNumberOfPerson();
        this.totalAmount = booking.getTotalPrice();
        this.paymentStatus = booking.getPaymentStatus();

        // Set payment deadline to 24 hours from now
        this.paymentDeadline = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24));
        updateRemainingTime();
    }

    // Update the remaining time based on the current time and deadline
    public void updateRemainingTime() {
        if (paymentDeadline != null) {
            long currentTime = System.currentTimeMillis();
            long deadlineTime = paymentDeadline.getTime();

            if (deadlineTime > currentTime) {
                this.remainingTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(deadlineTime - currentTime);
            } else {
                this.remainingTimeInSeconds = 0;
            }
        }
    }

    // Format tour dates to a readable string
    public String getFormattedTourDates() {
        if (tourStartDate == null) {
            return "No date selected";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDateStr = dateFormat.format(tourStartDate);

        if (tourEndDate != null) {
            String endDateStr = dateFormat.format(tourEndDate);
            return startDateStr + " - " + endDateStr;
        }

        return startDateStr;
    }

    // Format payment deadline to a readable string
    public String getFormattedPaymentDeadline() {
        if (paymentDeadline == null) {
            return "No deadline set";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(paymentDeadline);
    }

    // Format remaining time to HH:MM:SS
    public String getFormattedRemainingTime() {
        long hours = remainingTimeInSeconds / 3600;
        long minutes = (remainingTimeInSeconds % 3600) / 60;
        long seconds = remainingTimeInSeconds % 60;

        return String.format(Locale.getDefault(), "%02d : %02d : %02d", hours, minutes, seconds);
    }

    // Format total amount with currency
    public String getFormattedTotalAmount() {
        return String.format(Locale.getDefault(), "%,.0f %s", totalAmount, currency);
    }

    // Getters and setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTourId() {
        return tourId;
    }

    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getTourDuration() {
        return tourDuration;
    }

    public void setTourDuration(String tourDuration) {
        this.tourDuration = tourDuration;
    }

    public Date getTourStartDate() {
        return tourStartDate;
    }

    public void setTourStartDate(Date tourStartDate) {
        this.tourStartDate = tourStartDate;
    }

    public Date getTourEndDate() {
        return tourEndDate;
    }

    public void setTourEndDate(Date tourEndDate) {
        this.tourEndDate = tourEndDate;
    }

    public int getNumberOfTravelers() {
        return numberOfTravelers;
    }

    public void setNumberOfTravelers(int numberOfTravelers) {
        this.numberOfTravelers = numberOfTravelers;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(Date paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
        updateRemainingTime();
    }

    public long getRemainingTimeInSeconds() {
        return remainingTimeInSeconds;
    }

    public void setRemainingTimeInSeconds(long remainingTimeInSeconds) {
        this.remainingTimeInSeconds = remainingTimeInSeconds;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    // Calculate duration in days
    public int getDurationDays() {
        if (tourStartDate == null || tourEndDate == null) {
            return 0;
        }

        long diffInMillies = Math.abs(tourEndDate.getTime() - tourStartDate.getTime());
        return (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
    }

    // Get formatted duration string
    public String getFormattedDuration() {
        int days = getDurationDays();
        return days + " day" + (days != 1 ? "s" : "");
    }
}