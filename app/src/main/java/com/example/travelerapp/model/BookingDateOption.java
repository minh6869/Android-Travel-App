package com.example.travelerapp.model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class BookingDateOption {
    private String id;
    private Date date;
    private int dayOfWeek;
    private double price;
    private boolean isHoliday;
    private boolean isSelected;
    private boolean isAvailable;

    // Empty constructor for Firestore
    public BookingDateOption() {
    }

    public BookingDateOption(String id, Date date, int dayOfWeek, double price, boolean isHoliday) {
        this.id = id;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.price = price;
        this.isHoliday = isHoliday;
        this.isSelected = false;
        this.isAvailable = true;
    }

    // Convert from Firestore document
    public static BookingDateOption fromFirestore(String id, com.google.firebase.firestore.DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        BookingDateOption option = new BookingDateOption();
        option.id = id;

        // Get date from Timestamp
        Timestamp timestamp = document.getTimestamp("date");
        if (timestamp != null) {
            option.date = timestamp.toDate();
        }

        // Get day of week
        if (document.contains("dayOfWeek")) {
            Object dayOfWeekObj = document.get("dayOfWeek");
            if (dayOfWeekObj instanceof Long) {
                option.dayOfWeek = ((Long) dayOfWeekObj).intValue();
            } else if (dayOfWeekObj instanceof String) {
                // Convert string day to number if needed
                String dayString = (String) dayOfWeekObj;
                switch (dayString.toLowerCase()) {
                    case "sunday": option.dayOfWeek = 0; break;
                    case "monday": option.dayOfWeek = 1; break;
                    case "tuesday": option.dayOfWeek = 2; break;
                    case "wednesday": option.dayOfWeek = 3; break;
                    case "thursday": option.dayOfWeek = 4; break;
                    case "friday": option.dayOfWeek = 5; break;
                    case "saturday": option.dayOfWeek = 6; break;
                    default: option.dayOfWeek = -1;
                }
            }
        }

        // Get price
        if (document.contains("price")) {
            Object priceObj = document.get("price");
            if (priceObj instanceof Double) {
                option.price = (Double) priceObj;
            } else if (priceObj instanceof Long) {
                option.price = ((Long) priceObj).doubleValue();
            }
        }

        // Get isHoliday
        if (document.contains("isHoliday")) {
            option.isHoliday = document.getBoolean("isHoliday") != null &&
                    document.getBoolean("isHoliday");
        }

        option.isAvailable = true; // Default to available
        option.isSelected = false; // Default to not selected

        return option;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isHoliday() {
        return isHoliday;
    }

    public void setHoliday(boolean holiday) {
        isHoliday = holiday;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    // Helper method to get day name
    public String getDayName() {
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        if (dayOfWeek >= 0 && dayOfWeek < days.length) {
            return days[dayOfWeek];
        }
        return "";
    }
}