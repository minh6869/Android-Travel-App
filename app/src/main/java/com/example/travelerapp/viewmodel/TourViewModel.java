package com.example.travelerapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.travelerapp.model.Tour;
import com.example.travelerapp.repository.TourRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TourViewModel extends AndroidViewModel {
    private static final String TAG = "TourViewModel";
    private TourRepository repository;
    private LiveData<List<Tour>> allTours;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public TourViewModel(Application application) {
        super(application);
        Log.d(TAG, "TourViewModel initialized");
        repository = new TourRepository();
        allTours = repository.getTours();
    }

    public LiveData<List<Tour>> getAllTours() {
        return allTours;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public List<Tour> getRecentlyTours() {
        if (allTours.getValue() != null) {
            List<Tour> recentTours = allTours.getValue().stream()
                    .filter(Tour::isRecently)
                    .collect(Collectors.toList());
            Log.d(TAG, "Filtered " + recentTours.size() + " recent tours");
            return recentTours;
        }
        Log.d(TAG, "No tours available for filtering (recent)");
        return new ArrayList<>();
    }

    public List<Tour> getMainTours() {
        if (allTours.getValue() != null) {
            List<Tour> mainTours = allTours.getValue().stream()
                    .filter(tour -> !tour.isRecently())
                    .collect(Collectors.toList());
            Log.d(TAG, "Filtered " + mainTours.size() + " main tours");
            return mainTours;
        }
        Log.d(TAG, "No tours available for filtering (main)");
        return new ArrayList<>();
    }

    public void toggleBookmark(Tour tour) {
        Log.d(TAG, "Toggling bookmark for tour: " + tour.getId());
        tour.setBookmarked(!tour.isBookmarked());
        repository.updateTour(tour);
    }
}