package com.example.travelerapp.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelerapp.R;
import com.example.travelerapp.model.Tour;
import com.example.travelerapp.view.adapter.TourAdapter;
import com.example.travelerapp.viewmodel.TourViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements TourAdapter.OnTourClickListener {

    private RecyclerView recentlyTourRecyclerView;
    private RecyclerView mainTourRecyclerView;
    private TourAdapter recentlyTourAdapter;
    private TourAdapter mainTourAdapter;
    private TourViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TourViewModel.class);

        // Setup Recently Tours RecyclerView
        recentlyTourRecyclerView = view.findViewById(R.id.rcvRecentlyTour);
        LinearLayoutManager recentlyLayoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false);
        recentlyTourRecyclerView.setLayoutManager(recentlyLayoutManager);

        // Setup Main Tours RecyclerView
        mainTourRecyclerView = view.findViewById(R.id.rcvMainTour);
        LinearLayoutManager mainLayoutManager = new LinearLayoutManager(getContext());
        mainTourRecyclerView.setLayoutManager(mainLayoutManager);

        // Observe tours data and update UI
        viewModel.getAllTours().observe(getViewLifecycleOwner(), tours -> {
            updateRecentlyToursAdapter();
            updateMainToursAdapter();
        });

        return view;
    }

    private void updateRecentlyToursAdapter() {
        List<Tour> recentlyTours = viewModel.getRecentlyTours();
        if (recentlyTours != null) {
            recentlyTourAdapter = new TourAdapter(getContext(), recentlyTours, this);
            recentlyTourRecyclerView.setAdapter(recentlyTourAdapter);
        }
    }

    private void updateMainToursAdapter() {
        List<Tour> mainTours = viewModel.getMainTours();
        if (mainTours != null) {
            mainTourAdapter = new TourAdapter(getContext(), mainTours, this);
            mainTourRecyclerView.setAdapter(mainTourAdapter);
        }
    }

    @Override
    public void onTourClick(Tour tour) {
        // Handle when user clicks on a tour
        Toast.makeText(getContext(), "Selected: " + tour.getName(), Toast.LENGTH_SHORT).show();
        // Here you can open the tour detail screen
    }

    @Override
    public void onBookmarkClick(Tour tour, int position) {
        // Toggle bookmark status
        viewModel.toggleBookmark(tour);

        // Show notification toast
        if (tour.isBookmarked()) {
            Toast.makeText(getContext(), "Bookmarked tour: " + tour.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Removed bookmark from tour: " + tour.getName(), Toast.LENGTH_SHORT).show();
        }
    }
}