package com.example.travelerapp.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelerapp.R;
import com.example.travelerapp.model.Tour;
import com.example.travelerapp.view.LoginActivity;
import com.example.travelerapp.view.SearchActivity;
import com.example.travelerapp.view.TourDetailActivity;
import com.example.travelerapp.view.adapter.TourAdapter;
import com.example.travelerapp.viewmodel.TourViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class HomeFragment extends Fragment implements TourAdapter.OnTourClickListener {

    private Button btnSignIn;
    private RecyclerView recentlyTourRecyclerView;
    private RecyclerView mainTourRecyclerView;
    private TourAdapter recentlyTourAdapter;
    private TourAdapter mainTourAdapter;
    private TourViewModel viewModel;

    // Category buttons
    private ImageButton btnBeach;
    private ImageButton btnMountain;
    private ImageButton btnCity;
    private ImageButton btnAttractions;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find the sign-in button
        btnSignIn = view.findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(v -> {
            // Navigate to login activity
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        // Update UI based on authentication status
        updateAuthUI();


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

        // Set up search bar click
        View searchBar = view.findViewById(R.id.searchBar);
        searchBar.setOnClickListener(v -> {
            // Launch the search activity without category filter
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        });

        // Initialize and set up category buttons
        initCategoryButtons(view);

        return view;
    }

    private void initCategoryButtons(View view) {
        // Find category buttons
        btnBeach = view.findViewById(R.id.btnBeach);
        btnMountain = view.findViewById(R.id.btnMountain);
        btnCity = view.findViewById(R.id.btnCity);
        btnAttractions = view.findViewById(R.id.btnAttractions);

        // Set click listeners for category buttons
        btnBeach.setOnClickListener(v -> launchSearchWithCategory("beach"));
        btnMountain.setOnClickListener(v -> launchSearchWithCategory("mountain"));
        btnCity.setOnClickListener(v -> launchSearchWithCategory("city"));
        btnAttractions.setOnClickListener(v -> launchSearchWithCategory("attraction"));
    }

    /**
     * Launch SearchActivity with the specified category filter
     */
    private void launchSearchWithCategory(String category) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_CATEGORY, category);
        startActivity(intent);

        // Optional: show a toast indicating the category selection
        Toast.makeText(getContext(),
                "Searching " + category + " tours",
                Toast.LENGTH_SHORT).show();
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
        // Check if user is logged in before allowing tour details or booking
        if (mAuth.getCurrentUser() == null) {
            // Prompt user to login for exclusive content
            Toast.makeText(getContext(), "Please sign in to view this tour", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
        } else {
            // Navigate to tour detail screen
            Intent intent = new Intent(getActivity(), TourDetailActivity.class);
            intent.putExtra(TourDetailActivity.EXTRA_TOUR_ID, tour.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onBookmarkClick(Tour tour, int position) {
        // Check if user is logged in before allowing bookmarks
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please sign in to bookmark tours", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            return;
        }

        // Toggle bookmark status
        viewModel.toggleBookmark(tour);

        // Show notification toast
        if (tour.isBookmarked()) {
            Toast.makeText(getContext(), "Bookmarked tour: " + tour.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Removed bookmark from tour: " + tour.getName(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // Update UI when returning to fragment
        updateAuthUI();
    }

    private void updateAuthUI() {
        if (mAuth.getCurrentUser() != null) {
            // User is signed in, hide the button
            btnSignIn.setVisibility(View.GONE);
        } else {
            // User is not signed in, show the button
            btnSignIn.setVisibility(View.VISIBLE);
        }
    }

}