package com.example.travelerapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.travelerapp.model.Tour;
import com.example.travelerapp.repository.TourRepository;

public class TourDetailViewModel extends AndroidViewModel {
    private static final String TAG = "TourDetailViewModel";

    private TourRepository repository;
    private MutableLiveData<Tour> tourLiveData = new MutableLiveData<>();

    public TourDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new TourRepository();
    }

    public void loadTourDetails(String tourId) {
        Log.d(TAG, "Loading tour details for ID: " + tourId);

        // Get tour details from repository
        repository.getTourById(tourId, tour -> {
            if (tour != null) {
                Log.d(TAG, "Tour loaded successfully: " + tour.getTitle());
                tourLiveData.setValue(tour);
            } else {
                Log.e(TAG, "Failed to load tour with ID: " + tourId);
                tourLiveData.setValue(null);
            }
        });
    }

    public LiveData<Tour> getTourLiveData() {
        return tourLiveData;
    }

    public void toggleBookmark() {
        Tour tour = tourLiveData.getValue();
        if (tour != null) {
            tour.setBookmarked(!tour.isBookmarked());
            repository.updateTour(tour);
            tourLiveData.setValue(tour);
        }
    }
}
