package com.example.travelerapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Booking implements Parcelable {
    private String id;
    private String userId;
    private String participantName;
    private String participantEmail;
    private String participantPhoneNumber;
    private String tourId;
    private Date tourDateStart;
    private int numberOfPerson = 1;  // This represents total visitors
    private double totalPrice = 0.0;
    private String paymentStatus;

    // Local-only properties
    private String tourName;
    private String tourImageUrl;
    private int tourImageResourceId;
    private BookingDateOption selectedDateOption;

    public Booking() {
        // Default constructor required for Firestore
        this.numberOfPerson = 1; // Default to 1 visitor
        this.paymentStatus = "pending"; // Default status

        // Set user info from Firebase Auth if available
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.userId = currentUser.getUid();
            this.participantEmail = currentUser.getEmail();
            this.participantName = currentUser.getDisplayName();
        }
    }

    public Booking(String tourId, String tourName) {
        this();
        this.tourId = tourId;
        this.tourName = tourName;
    }

    // Parcelable implementation
    protected Booking(Parcel in) {
        id = in.readString();
        userId = in.readString();
        participantName = in.readString();
        participantEmail = in.readString();
        participantPhoneNumber = in.readString();
        tourId = in.readString();
        long tmpDate = in.readLong();
        tourDateStart = tmpDate != -1 ? new Date(tmpDate) : null;
        numberOfPerson = in.readInt();
        totalPrice = in.readDouble();
        paymentStatus = in.readString();
        tourName = in.readString();
        tourImageUrl = in.readString();
        tourImageResourceId = in.readInt();

        // Safely read the BookingDateOption, handling potential ClassNotFoundException
        try {
            selectedDateOption = in.readParcelable(BookingDateOption.class.getClassLoader());
        } catch (Exception e) {
            selectedDateOption = null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(participantName);
        dest.writeString(participantEmail);
        dest.writeString(participantPhoneNumber);
        dest.writeString(tourId);
        dest.writeLong(tourDateStart != null ? tourDateStart.getTime() : -1);
        dest.writeInt(numberOfPerson);
        dest.writeDouble(totalPrice);
        dest.writeString(paymentStatus);
        dest.writeString(tourName);
        dest.writeString(tourImageUrl);
        dest.writeInt(tourImageResourceId);
        dest.writeParcelable(selectedDateOption, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Booking> CREATOR = new Creator<Booking>() {
        @Override
        public Booking createFromParcel(Parcel in) {
            return new Booking(in);
        }

        @Override
        public Booking[] newArray(int size) {
            return new Booking[size];
        }
    };

    // Convert to Firestore document
    public Map<String, Object> toFirestore() {
        Map<String, Object> booking = new HashMap<>();
        booking.put("id", id); // Include ID in the document
        booking.put("userId", userId);
        booking.put("participantName", participantName);
        booking.put("participantEmail", participantEmail);
        booking.put("participantPhoneNumber", participantPhoneNumber);
        booking.put("tourId", tourId);

        // Convert Date to Timestamp
        if (tourDateStart != null) {
            booking.put("tourDateStart", new Timestamp(tourDateStart));
        }

        booking.put("numberOfPerson", numberOfPerson);
        booking.put("totalPrice", totalPrice);
        booking.put("paymentStatus", paymentStatus);
        booking.put("createdAt", new Timestamp(new Date())); // Add creation timestamp

        return booking;
    }

    // All getters and setters remain the same
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }

    public String getParticipantPhoneNumber() {
        return participantPhoneNumber;
    }

    public void setParticipantPhoneNumber(String participantPhoneNumber) {
        this.participantPhoneNumber = participantPhoneNumber;
    }

    public String getTourId() {
        return tourId;
    }

    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    public Date getTourDateStart() {
        return tourDateStart;
    }

    public void setTourDateStart(Date tourDateStart) {
        this.tourDateStart = tourDateStart;
    }

    public int getNumberOfPerson() {
        return numberOfPerson;
    }

    public void setNumberOfPerson(int numberOfPerson) {
        this.numberOfPerson = numberOfPerson;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getTourImageUrl() {
        return tourImageUrl;
    }

    public void setTourImageUrl(String tourImageUrl) {
        this.tourImageUrl = tourImageUrl;
    }

    public int getTourImageResourceId() {
        return tourImageResourceId;
    }

    public void setTourImageResourceId(int tourImageResourceId) {
        this.tourImageResourceId = tourImageResourceId;
    }

    public BookingDateOption getSelectedDateOption() {
        return selectedDateOption;
    }

    public void setSelectedDateOption(BookingDateOption selectedDateOption) {
        this.selectedDateOption = selectedDateOption;
        if (selectedDateOption != null) {
            this.tourDateStart = selectedDateOption.getDate();
        }
    }

    public String getVisitorSummary() {
        return "Visitors: " + numberOfPerson;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id='" + id + '\'' +
                ", tourId='" + tourId + '\'' +
                ", tourName='" + tourName + '\'' +
                ", participants=" + numberOfPerson +
                ", totalPrice=" + totalPrice +
                '}';
    }
}