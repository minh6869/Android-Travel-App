package com.example.travelerapp.model;

import android.util.Log;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

public class Tour {
    private static final String TAG = "Tour";

    @DocumentId
    private String id;

    // Primary fields from Firestore
    private String title;
    private String description;
    private String tourImageUrl;
    private String status;
    private Double rating;
    private String category;
    private String providerPhone;
    private String pickupLoc;
    private String address;
    private String location;

    // Price can be directly in the tour document
    private Object priceValue; // Can be Double, Long, or String

    // Local-only properties
    @Exclude private int imageResourceId;
    @Exclude private boolean isBookmarked = false;
    @Exclude private boolean isRecently = false;
    @Exclude private String reviewCount = "0";
    @Exclude private String formattedPrice = null;
    @Exclude private Double numericPrice = 0.0;

    // Empty constructor required for Firestore
    public Tour() {
        Log.d(TAG, "Creating empty Tour instance");
    }

    // Constructor for local data
    public Tour(String id, String name, String location, String rating,
                String reviewCount, String price, int imageResourceId, boolean isRecently) {
        this.id = id;
        this.title = name;
        this.location = location;
        try {
            this.rating = Double.parseDouble(rating.split("/")[0]);
        } catch (Exception e) {
            this.rating = 0.0;
        }
        this.reviewCount = reviewCount;
        this.formattedPrice = price;
        this.imageResourceId = imageResourceId;
        this.isRecently = isRecently;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        Log.d(TAG, "Setting title: " + title);
        this.title = title;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTourImageUrl() { return tourImageUrl; }
    public void setTourImageUrl(String tourImageUrl) {
        Log.d(TAG, "Setting tourImageUrl: " + tourImageUrl);
        this.tourImageUrl = tourImageUrl;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public Double getRating() { return rating != null ? rating : 0.0; }
    public void setRating(Double rating) {
        Log.d(TAG, "Setting rating: " + rating);
        this.rating = rating;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getProviderPhone() { return providerPhone; }
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }

    public String getPickupLoc() { return pickupLoc; }
    public void setPickupLoc(String pickupLoc) { this.pickupLoc = pickupLoc; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLocation() {
        return location != null ? location : (address != null ? address : "");
    }
    public void setLocation(String location) {
        Log.d(TAG, "Setting location: " + location);
        this.location = location;
    }

    // Direct Firestore price field
    public Object getPriceValue() { return priceValue; }
    public void setPriceValue(Object priceValue) {
        this.priceValue = priceValue;

        // Also update the formatted price and numeric price
        if (priceValue instanceof Double) {
            this.numericPrice = (Double) priceValue;
            this.formattedPrice = String.format("%,.0f VND", this.numericPrice);
        } else if (priceValue instanceof Long) {
            this.numericPrice = ((Long) priceValue).doubleValue();
            this.formattedPrice = String.format("%,.0f VND", this.numericPrice);
        } else if (priceValue instanceof String) {
            try {
                this.numericPrice = Double.parseDouble((String) priceValue);
                this.formattedPrice = String.format("%,.0f VND", this.numericPrice);
            } catch (NumberFormatException e) {
                this.formattedPrice = (String) priceValue;
            }
        }

        Log.d(TAG, "Set price value: " + priceValue + " -> " + this.formattedPrice);
    }

    // Price handling for UI
    @Exclude
    public String getPrice() {
        if (formattedPrice != null && !formattedPrice.isEmpty()) {
            return formattedPrice;
        }

        if (numericPrice > 0) {
            return String.format("%,.0f VND", numericPrice);
        }

        return "Contact for price";
    }

    @Exclude
    public void setPrice(String price) {
        Log.d(TAG, "Setting formatted price: " + price);
        this.formattedPrice = price;
    }

    @Exclude
    public Double getNumericPrice() { return numericPrice; }

    @Exclude
    public void setNumericPrice(Double price) {
        Log.d(TAG, "Setting numeric price: " + price);
        this.numericPrice = price;
        if (price > 0) {
            this.formattedPrice = String.format("%,.0f VND", price);
        }
    }

    // Local-only getters and setters
    @Exclude
    public int getImageResourceId() { return imageResourceId; }
    public void setImageResourceId(int imageResourceId) { this.imageResourceId = imageResourceId; }

    @Exclude
    public boolean isBookmarked() { return isBookmarked; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    @Exclude
    public boolean isRecently() { return isRecently; }
    public void setRecently(boolean recently) { isRecently = recently; }

    @Exclude
    public String getReviewCount() { return reviewCount; }
    public void setReviewCount(String reviewCount) {
        Log.d(TAG, "Setting reviewCount: " + reviewCount);
        this.reviewCount = reviewCount;
    }

    // Compatibility methods to maintain existing code functionality
    @Exclude
    public String getName() { return title; }

    @Exclude
    public String getRatingFormatted() {
        if (rating == null) return "0,0/10";
        return String.format("%.1f/10", rating).replace('.', ',');
    }

    @Exclude
    public String getRatingDisplay() {
        if (rating == null) return "0,0 (0)";
        return String.format("%.1f (%s)", rating, reviewCount).replace('.', ',');
    }

    // For debugging purposes
    @Override
    public String toString() {
        return "Tour{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", rating=" + rating +
                ", price='" + formattedPrice + '\'' +
                ", numericPrice=" + numericPrice +
                '}';
    }
}
