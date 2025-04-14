package com.example.travelerapp.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.travelerapp.R;
import com.example.travelerapp.model.Tour;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TourRepository {
    private static final String TAG = "TourRepository";
    private MutableLiveData<List<Tour>> tourListLiveData = new MutableLiveData<>();
    private FirebaseFirestore db;
    private CollectionReference toursCollection;
    private CollectionReference reviewsCollection;
    private List<Tour> cachedTours = new ArrayList<>();
    private boolean useLocalData = false; // Set to false to use Firebase

    public TourRepository() {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Configure Firestore settings for better reliability
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        toursCollection = db.collection("tours");
        reviewsCollection = db.collection("reviews");

        // Load tours from Firestore
        if (!useLocalData) {
            loadFirestoreTours();
        } else {
            loadLocalTours();
        }
    }

    private void loadFirestoreTours() {
        Log.d(TAG, "📥 Loading tours from Firestore...");

        toursCollection
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "✅ Firestore query successful, found " +
                            queryDocumentSnapshots.size() + " documents");

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.w(TAG, "⚠️ No tours found in Firestore. Check your data or collection path.");
                        loadLocalTours(); // Fall back to local data
                        return;
                    }

                    List<Tour> tourList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d(TAG, "Processing document: " + document.getId());
                            Map<String, Object> data = document.getData();
                            Log.d(TAG, "Document data: " + data);

                            // Create Tour object
                            Tour tour = new Tour();

                            // Set document ID
                            tour.setId(document.getId());

                            // TITLE - Check all possible field names
                            if (data.containsKey("title")) {
                                tour.setTitle(getString(data, "title"));
                            } else if (data.containsKey("nameTour")) {
                                tour.setTitle(getString(data, "nameTour"));
                            } else if (data.containsKey("name")) {
                                tour.setTitle(getString(data, "name"));
                            }

                            // DESCRIPTION
                            if (data.containsKey("description")) {
                                tour.setDescription(getString(data, "description"));
                            }

                            // IMAGE URL - Check all possible field names
                            if (data.containsKey("tourImageUrl")) {
                                tour.setTourImageUrl(getString(data, "tourImageUrl"));
                            } else if (data.containsKey("imageUrl")) {
                                tour.setTourImageUrl(getString(data, "imageUrl"));
                            } else if (data.containsKey("image")) {
                                tour.setTourImageUrl(getString(data, "image"));
                            }

                            // STATUS
                            if (data.containsKey("status")) {
                                tour.setStatus(getString(data, "status"));
                            }

                            // RATING - Check all possible field names
                            if (data.containsKey("rating")) {
                                tour.setRating(getDouble(data, "rating"));
                            } else if (data.containsKey("review")) {
                                tour.setRating(getDouble(data, "review"));
                            }

                            // CATEGORY
                            if (data.containsKey("category")) {
                                tour.setCategory(getString(data, "category"));
                            }

                            // PROVIDER PHONE
                            if (data.containsKey("providerPhone")) {
                                tour.setProviderPhone(getString(data, "providerPhone"));
                            }

                            // PICKUP LOCATION
                            if (data.containsKey("pickupLoc")) {
                                tour.setPickupLoc(getString(data, "pickupLoc"));
                            }

                            // ADDRESS
                            if (data.containsKey("address")) {
                                tour.setAddress(getString(data, "address"));
                            }

                            // LOCATION
                            if (data.containsKey("location")) {
                                tour.setLocation(getString(data, "location"));
                            } else if (tour.getAddress() != null) {
                                tour.setLocation(tour.getAddress());
                            }

                            // PRICE - Check if price is directly in the document
                            if (data.containsKey("price")) {
                                tour.setPriceValue(data.get("price"));
                            } else {
                                // If not, load from availableDates subcollection
                                loadTourPrice(tour);
                            }

                            // Load review count
                            loadReviewCount(tour);

                            // Set recently flag based on some criteria (first 3 are recent)
                            tour.setRecently(tourList.size() < 3);

                            // Set default image if no image URL
                            if (tour.getTourImageUrl() == null || tour.getTourImageUrl().isEmpty()) {
                                tour.setImageResourceId(getDefaultImageForTour(tour.getId()));
                            }

                            // Make sure we have at least a title
                            if (tour.getTitle() == null || tour.getTitle().isEmpty()) {
                                tour.setTitle("Tour #" + (tourList.size() + 1));
                            }

                            tourList.add(tour);
                            Log.d(TAG, "✅ Added tour: " + tour);
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error processing tour document: " + e.getMessage(), e);
                        }
                    }

                    cachedTours = tourList;
                    tourListLiveData.setValue(tourList);
                    Log.d(TAG, "✅ Set LiveData with " + tourList.size() + " tours");

                    // Log all tours for debugging
                    for (Tour tour : tourList) {
                        Log.d(TAG, "Tour in list: " + tour);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading tours from Firestore: " + e.getMessage(), e);
                    loadLocalTours(); // Fall back to local data
                });
    }

    // Helper methods to safely extract values from data map
    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }

    private Double getDouble(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private void loadTourPrice(Tour tour) {
        Log.d(TAG, "Loading price for tour: " + tour.getId());

        // Get the lowest price from available dates
        toursCollection.document(tour.getId())
                .collection("availableDates")
                .orderBy("price")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot priceDoc = queryDocumentSnapshots.getDocuments().get(0);
                        if (priceDoc.contains("price")) {
                            Object priceObj = priceDoc.get("price");
                            Double priceValue = null;

                            if (priceObj instanceof Double) {
                                priceValue = (Double) priceObj;
                            } else if (priceObj instanceof Long) {
                                priceValue = ((Long) priceObj).doubleValue();
                            } else if (priceObj instanceof String) {
                                try {
                                    priceValue = Double.parseDouble((String) priceObj);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Could not parse price string: " + priceObj);
                                }
                            }

                            if (priceValue != null) {
                                tour.setNumericPrice(priceValue);
                                Log.d(TAG, "Set price for tour " + tour.getId() + ": " + tour.getPrice());

                                // Update the tour in the list
                                updateTourInList(tour);
                            }
                        } else {
                            Log.w(TAG, "Price field not found in document");
                            // Set a fixed price for testing
                            tour.setNumericPrice(1500000.0);
                            updateTourInList(tour);
                        }
                    } else {
                        Log.d(TAG, "No available dates found for tour: " + tour.getId());
                        // Set a fixed price for testing
                        tour.setNumericPrice(1500000.0);
                        updateTourInList(tour);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading price for tour " + tour.getId() + ": " + e.getMessage());
                    // Set a fixed price for testing
                    tour.setNumericPrice(1500000.0);
                    updateTourInList(tour);
                });
    }

    private void loadReviewCount(Tour tour) {
        reviewsCollection
                .whereEqualTo("tourId", tour.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tour.setReviewCount(String.valueOf(count));
                    updateTourInList(tour);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading review count for tour " + tour.getId() + ": " + e.getMessage());
                });
    }

    private void updateTourInList(Tour updatedTour) {
        List<Tour> currentList = tourListLiveData.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).getId().equals(updatedTour.getId())) {
                    currentList.set(i, updatedTour);
                    Log.d(TAG, "Updated tour in list: " + updatedTour);
                    break;
                }
            }
            tourListLiveData.setValue(currentList);
        }
    }

    private int getDefaultImageForTour(String tourId) {
        // Map tour IDs to resource IDs, or return a default
        switch (tourId) {
            case "tour1": return R.drawable.night_e1708573119375;
            case "tour2": return R.drawable.halongbay;
            case "tour3": return R.drawable._a258665_b982_4f6f_b857_dc886493b911_halong_bay_cruise_2_days_1_night_with_5_star_luxury;
            case "tour4": return R.drawable.japan_4k_qbc6mlnwowjbszld;
            case "tour5": return R.drawable.gyeongbokgung_palace_lit_up_at_sunset_s10jvst7e046kf75;
            case "9UBdAvgAx4hX6jsVqhNz": return R.drawable.halongbay; // Matching your actual Firestore ID
            default: return R.drawable.ic_launcher_background; // Default image
        }
    }

    private void loadLocalTours() {
        Log.d(TAG, "⚠️ Falling back to local tour data...");
        List<Tour> tourList = new ArrayList<>();

        // Recently Tours (isRecently = true)
        tourList.add(new Tour(
                "tour1",
                "Temple of Literature by Night",
                "Van Mieu Ward",
                "9.4/10",
                "16315",
                "199.000 VND",
                R.drawable.night_e1708573119375,
                true
        ));

        tourList.add(new Tour(
                "tour2",
                "Ha Long Bay with Cozy Bay Premium",
                "Ha Long City",
                "9.7/10",
                "89",
                "1.055.000 VND",
                R.drawable.halongbay,
                true
        ));

        tourList.add(new Tour(
                "tour3",
                "Ha Long Bay with Premium 5-star",
                "Ha Long City",
                "9.6/10",
                "1083",
                "1.799.000 VND",
                R.drawable._a258665_b982_4f6f_b857_dc886493b911_halong_bay_cruise_2_days_1_night_with_5_star_luxury,
                true
        ));

        // Main Tours (isRecently = false)
        tourList.add(new Tour(
                "tour4",
                "Japan Full Package Tour (Tokyo, Osaka, Mount Fuji and Kyoto) - 5D5N Tour",
                "Japan",
                "4.8/5",
                "2420",
                "34.890.000 VND",
                R.drawable.japan_4k_qbc6mlnwowjbszld,
                false
        ));

        tourList.add(new Tour(
                "tour5",
                "South Korea Full Package Tour (Seoul, Nami Island, Everland) - 5D4N Tour",
                "Seoul",
                "4.9/5",
                "1832",
                "16.990.000 VND",
                R.drawable.gyeongbokgung_palace_lit_up_at_sunset_s10jvst7e046kf75,
                false
        ));

        cachedTours = tourList;
        tourListLiveData.setValue(tourList);
        Log.d(TAG, "✅ Set LiveData with " + tourList.size() + " local tours");
    }

    public LiveData<List<Tour>> getTours() {
        // If we have no data yet, try loading it
        if (tourListLiveData.getValue() == null || tourListLiveData.getValue().isEmpty()) {
            if (!useLocalData) {
                loadFirestoreTours();
            } else {
                loadLocalTours();
            }
        }
        return tourListLiveData;
    }

    public void refreshTours() {
        // Force a refresh of tour data
        if (!useLocalData) {
            loadFirestoreTours();
        } else {
            loadLocalTours();
        }
    }

    public void updateTour(Tour tour) {
        // Update local cache
        List<Tour> currentList = tourListLiveData.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).getId().equals(tour.getId())) {
                    currentList.set(i, tour);
                    break;
                }
            }
            tourListLiveData.setValue(currentList);
        }

        // Update Firestore (if not using local data)
        if (!useLocalData) {
            // Only update the bookmark status in user's favorites collection
            // Don't update the main tour document
            updateUserFavorites(tour);
        }
    }

    private void updateUserFavorites(Tour tour) {
        // This would typically update a user's favorites collection
        // For now, we'll just log it
        Log.d(TAG, "Would update user favorites for tour: " + tour.getId() +
                ", bookmarked: " + tour.isBookmarked());
    }


    public interface TourCallback {
        void onTourLoaded(Tour tour);
    }

    // Replace the getTourById method in TourRepository class
    public void getTourById(String tourId, TourCallback callback) {
        Log.d(TAG, "Getting tour by ID: " + tourId);

        if (cachedTours != null && !cachedTours.isEmpty()) {
            // First check if tour is in cache
            for (Tour tour : cachedTours) {
                if (tour.getId().equals(tourId)) {
                    Log.d(TAG, "Found tour in cache: " + tour.getTitle());
                    callback.onTourLoaded(tour);
                    return;
                }
            }
        }

        // If not in cache, try to get from Firestore
        if (!useLocalData) {
            toursCollection.document(tourId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            try {
                                // Manually create the Tour object instead of using toObject()
                                Tour tour = new Tour();
                                Map<String, Object> data = documentSnapshot.getData();

                                if (data == null) {
                                    Log.e(TAG, "Document data is null for ID: " + tourId);
                                    callback.onTourLoaded(null);
                                    return;
                                }

                                // Set ID
                                tour.setId(documentSnapshot.getId());

                                // Set title - check different possible field names
                                if (data.containsKey("title")) {
                                    tour.setTitle(getString(data, "title"));
                                } else if (data.containsKey("nameTour")) {
                                    tour.setTitle(getString(data, "nameTour"));
                                } else if (data.containsKey("name")) {
                                    tour.setTitle(getString(data, "name"));
                                }

                                // Set description
                                if (data.containsKey("description")) {
                                    tour.setDescription(getString(data, "description"));
                                }

                                // Set image URL - check different possible field names
                                if (data.containsKey("tourImageUrl")) {
                                    tour.setTourImageUrl(getString(data, "tourImageUrl"));
                                } else if (data.containsKey("imageUrl")) {
                                    tour.setTourImageUrl(getString(data, "imageUrl"));
                                } else if (data.containsKey("image")) {
                                    tour.setTourImageUrl(getString(data, "image"));
                                }

                                // Set status
                                if (data.containsKey("status")) {
                                    tour.setStatus(getString(data, "status"));
                                }

                                // Set rating - check different possible field names
                                if (data.containsKey("rating")) {
                                    tour.setRating(getDouble(data, "rating"));
                                } else if (data.containsKey("review")) {
                                    tour.setRating(getDouble(data, "review"));
                                }

                                // Set category
                                if (data.containsKey("category")) {
                                    tour.setCategory(getString(data, "category"));
                                }

                                // Set provider phone
                                if (data.containsKey("providerPhone")) {
                                    tour.setProviderPhone(getString(data, "providerPhone"));
                                }

                                // Set pickup location
                                if (data.containsKey("pickupLoc")) {
                                    tour.setPickupLoc(getString(data, "pickupLoc"));
                                }

                                // Set address
                                if (data.containsKey("address")) {
                                    tour.setAddress(getString(data, "address"));
                                }

                                // Set location
                                if (data.containsKey("location")) {
                                    tour.setLocation(getString(data, "location"));
                                } else if (tour.getAddress() != null) {
                                    tour.setLocation(tour.getAddress());
                                }

                                // Set price
                                if (data.containsKey("price")) {
                                    tour.setPriceValue(data.get("price"));
                                } else {
                                    // If price is not directly in document, set a default
                                    tour.setNumericPrice(1500000.0);
                                }

                                // Set default image resource based on ID
                                tour.setImageResourceId(getDefaultImageForTour(tour.getId()));

                                // Load review count (this is async, but we'll return the tour immediately)
                                loadReviewCount(tour);

                                Log.d(TAG, "Successfully loaded tour: " + tour.getTitle());
                                callback.onTourLoaded(tour);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing tour document: " + e.getMessage(), e);
                                callback.onTourLoaded(null);
                            }
                        } else {
                            Log.e(TAG, "Tour document does not exist for ID: " + tourId);
                            callback.onTourLoaded(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading tour from Firestore: " + e.getMessage());
                        callback.onTourLoaded(null);
                    });
        } else {
            // If using local data and not found in cache, return null
            Log.e(TAG, "Tour not found in local data for ID: " + tourId);
            callback.onTourLoaded(null);
        }
    }

}
