package com.example.travelerapp.model;

public class Tour {
    private String id;
    private String name;
    private String location;
    private String rating;
    private String reviewCount;
    private String price;
    private int imageResourceId;
    private boolean isBookmarked = false;
    private boolean isRecently = false;  // New property

    public Tour(String id, String name, String location, String rating,
                String reviewCount, String price, int imageResourceId, boolean isRecently) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.price = price;
        this.imageResourceId = imageResourceId;
        this.isRecently = isRecently;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getRating() { return rating; }
    public String getReviewCount() { return reviewCount; }
    public String getPrice() { return price; }
    public int getImageResourceId() { return imageResourceId; }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public boolean isRecently() {
        return isRecently;
    }

    public void setRecently(boolean recently) {
        isRecently = recently;
    }
}