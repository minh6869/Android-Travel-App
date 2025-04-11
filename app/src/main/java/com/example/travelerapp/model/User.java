package com.example.travelerapp.model;

import java.util.List;

/**
 * User model representing a user document in Firebase Firestore.
 * Fields match the structure in the 'users' collection:
 * - email: string
 * - name: string
 * - phone: string
 * - avatarUrl: string (URL from Firebase Storage)
 * - paymentMethods: array (e.g. ["momo", "credit_card"])
 * - bookings: array (list of booking IDs, references to bookings)
 */
public class User {
    private String id;          // Firestore Document ID
    private String email;       // User's email
    private String name;        // User's name
    private String phone;       // User's phone number
    private String avatarUrl;   // Profile picture URL from Firebase Storage
    private List<String> paymentMethods;  // List of payment methods
    private List<String> bookings;        // List of booking IDs

    // Default constructor required for Firestore
    public User() {
    }

    // Constructor with essential fields
    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }

    // Constructor with phone number
    public User(String email, String name, String phone) {
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    // Full constructor
    public User(String id, String email, String name, String phone, String avatarUrl,
                List<String> paymentMethods, List<String> bookings) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.paymentMethods = paymentMethods;
        this.bookings = bookings;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<String> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<String> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public List<String> getBookings() {
        return bookings;
    }

    public void setBookings(List<String> bookings) {
        this.bookings = bookings;
    }

    // Add a single payment method
    public void addPaymentMethod(String paymentMethod) {
        if (this.paymentMethods != null) {
            this.paymentMethods.add(paymentMethod);
        }
    }

    // Add a single booking
    public void addBooking(String bookingId) {
        if (this.bookings != null) {
            this.bookings.add(bookingId);
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", paymentMethods=" + paymentMethods +
                ", bookings=" + bookings +
                '}';
    }
}