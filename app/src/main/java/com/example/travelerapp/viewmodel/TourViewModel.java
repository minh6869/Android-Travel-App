package com.example.travelerapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelerapp.model.Tour;
import com.example.travelerapp.repository.TourRepository;

import java.util.List;
import java.util.stream.Collectors;

public class TourViewModel extends ViewModel {
    private TourRepository repository;
    private LiveData<List<Tour>> allTours;

    public TourViewModel() {
        repository = new TourRepository();
        allTours = repository.getTours();
    }

    public LiveData<List<Tour>> getAllTours() {
        return allTours;
    }

    public List<Tour> getRecentlyTours() {
        if (allTours.getValue() != null) {
            return allTours.getValue().stream()
                    .filter(Tour::isRecently)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<Tour> getMainTours() {
        if (allTours.getValue() != null) {
            return allTours.getValue().stream()
                    .filter(tour -> !tour.isRecently())
                    .collect(Collectors.toList());
        }
        return null;
    }

    public void toggleBookmark(Tour tour) {
        tour.setBookmarked(!tour.isBookmarked());
        repository.updateTour(tour);
    }
}