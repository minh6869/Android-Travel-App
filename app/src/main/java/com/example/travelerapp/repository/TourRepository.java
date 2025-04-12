package com.example.travelerapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.travelerapp.R;
import com.example.travelerapp.model.Tour;

import java.util.ArrayList;
import java.util.List;

public class TourRepository {
    private MutableLiveData<List<Tour>> tourListLiveData = new MutableLiveData<>();

    public TourRepository() {
        loadTours();
    }

    private void loadTours() {
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

        tourListLiveData.setValue(tourList);
    }

    public LiveData<List<Tour>> getTours() {
        return tourListLiveData;
    }

    public void updateTour(Tour tour) {
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
    }
}