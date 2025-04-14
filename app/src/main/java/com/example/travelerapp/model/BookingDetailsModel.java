package com.example.travelerapp.model;

import java.util.Date;

public class BookingDetailsModel {
    private String id;
    private String tourId;
    private String tourName;
    private String tourImageUrl;
    private int tourImageResourceId;
    private int visitorCount;
    private Date bookingDate;
    private double totalPrice;

    // Contact details
    private String contactName;
    private String contactEmail;
    private String contactPhone;

    // Location details
    private String pickupLocation;
    private String dropoffLocation;

    // Payment details
    private String paymentMethod;
    private boolean isContactFilled;
    private boolean isLocationFilled;

    public BookingDetailsModel() {
        // Default constructor
        this.visitorCount = 1;
        this.isContactFilled = false;
        this.isLocationFilled = false;
    }

    // Copy constructor to create from a Booking
    public BookingDetailsModel(Booking booking) {
        this.tourId = booking.getTourId();
        this.tourName = booking.getTourName();
        this.tourImageUrl = booking.getTourImageUrl();
        this.tourImageResourceId = booking.getTourImageResourceId();
        this.visitorCount = booking.getNumberOfPerson();
        this.bookingDate = booking.getTourDateStart();
        this.totalPrice = booking.getTotalPrice();
        this.isContactFilled = false;
        this.isLocationFilled = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getVisitorCount() {
        return visitorCount;
    }

    public void setVisitorCount(int visitorCount) {
        this.visitorCount = visitorCount;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isContactFilled() {
        return isContactFilled;
    }

    public void setContactFilled(boolean contactFilled) {
        isContactFilled = contactFilled;
    }

    public boolean isLocationFilled() {
        return isLocationFilled;
    }

    public void setLocationFilled(boolean locationFilled) {
        isLocationFilled = locationFilled;
    }

    public String getVisitorSummary() {
        return "Visitors: " + visitorCount;
    }

    public boolean isReadyToProceed() {
        return isContactFilled;
    }
}