package com.example.travelerapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelerapp.R;
import com.example.travelerapp.model.Tour;
import com.example.travelerapp.view.adapter.SearchTourAdapter;
import com.example.travelerapp.viewmodel.TourViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.graphics.drawable.GradientDrawable;


public class SearchActivity extends AppCompatActivity implements SearchTourAdapter.OnTourClickListener {
    private static final String TAG = "SearchActivity";
    public static final String EXTRA_CATEGORY = "extra_category";

    private EditText searchInput;
    private RecyclerView toursRecyclerView;
    private ProgressBar progressBar;
    private TextView noResultsText;
    private ImageButton backButton;
    private TextView categoryFilterText;

    private TourViewModel viewModel;
    private SearchTourAdapter adapter;
    private List<Tour> allTours = new ArrayList<>();
    private List<Tour> filteredTours = new ArrayList<>();
    private String currentCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        // Get category from intent if it exists
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_CATEGORY)) {
            currentCategory = intent.getStringExtra(EXTRA_CATEGORY);
            Log.d(TAG, "Received category filter: " + currentCategory);
        }

        // Initialize views
        initViews();

        // Set up the back button
        backButton.setOnClickListener(v -> finish());

        // Set up RecyclerView
        setupRecyclerView();

        // Initialize ViewModel properly using ViewModelProvider
        viewModel = new ViewModelProvider(this).get(TourViewModel.class);

        // Load data
        loadTours();

        // Set up search functionality
        setupSearch();
    }

    private void initViews() {
        searchInput = findViewById(R.id.searchInput);
        toursRecyclerView = findViewById(R.id.toursRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        noResultsText = findViewById(R.id.tvNoResults);
        backButton = findViewById(R.id.btnBack);

        // Initialize category filter text if it exists in layout
        categoryFilterText = findViewById(R.id.categoryFilterText);
        if (categoryFilterText == null) {
            // Create and add the category filter text view dynamically if it doesn't exist
            createCategoryFilterTextView();
        } else {
            updateCategoryFilterText();
        }
    }

    private void createCategoryFilterTextView() {
        // Only create if we have a category filter
        if (currentCategory == null || currentCategory.isEmpty()) {
            return;
        }

        // Find the parent view to add the category filter text to
        View searchContainer = findViewById(R.id.searchContainer);
        if (searchContainer != null && searchContainer.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) searchContainer.getParent();

            // Create the category filter text view
            categoryFilterText = new TextView(this);
            categoryFilterText.setId(View.generateViewId());

            // Set layout parameters
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, searchContainer.getId());
            params.setMargins(
                    dpToPx(16), // left
                    dpToPx(8),  // top
                    dpToPx(16), // right
                    dpToPx(8)   // bottom
            );
            categoryFilterText.setLayoutParams(params);

            // Set appearance
            categoryFilterText.setPadding(
                    dpToPx(12), // left
                    dpToPx(6),  // top
                    dpToPx(12), // right
                    dpToPx(6)   // bottom
            );
            categoryFilterText.setTextColor(getResources().getColor(android.R.color.white));

            // Try to set background from drawable
            try {
                int backgroundResId = getResources().getIdentifier(
                        "category_chip_background", "drawable", getPackageName());
                if (backgroundResId != 0) {
                    categoryFilterText.setBackgroundResource(backgroundResId);
                } else {
                    // Fallback to a colored background
                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.RECTANGLE);
                    shape.setCornerRadius(dpToPx(16));
                    shape.setColor(getResources().getColor(android.R.color.holo_blue_light));
                    categoryFilterText.setBackground(shape);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting background: " + e.getMessage());
            }

            // Add to parent
            parent.addView(categoryFilterText);

            // Update the text and click listener
            updateCategoryFilterText();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupRecyclerView() {
        // Set up RecyclerView with a grid layout (2 columns)
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        toursRecyclerView.setLayoutManager(layoutManager);

        // Initialize adapter with empty list
        filteredTours = new ArrayList<>();
        adapter = new SearchTourAdapter(this, filteredTours, this);
        toursRecyclerView.setAdapter(adapter);
    }

    private void updateCategoryFilterText() {
        if (categoryFilterText != null) {
            if (currentCategory != null && !currentCategory.isEmpty()) {
                categoryFilterText.setVisibility(View.VISIBLE);

                // Format category text nicely (capitalize first letter)
                String displayCategory = currentCategory.substring(0, 1).toUpperCase() +
                        currentCategory.substring(1);
                categoryFilterText.setText("Category: " + displayCategory + " ✕");

                // Add clear filter functionality
                categoryFilterText.setOnClickListener(v -> {
                    currentCategory = null;
                    updateCategoryFilterText();
                    searchInput.setHint("Search for tours...");
                    filterTours(searchInput.getText().toString());
                });
            } else {
                categoryFilterText.setVisibility(View.GONE);
            }
        }
    }

    private void loadTours() {
        progressBar.setVisibility(View.VISIBLE);
        toursRecyclerView.setVisibility(View.GONE);
        noResultsText.setVisibility(View.GONE);

        // Observe tours data from ViewModel
        viewModel.getAllTours().observe(this, tours -> {
            progressBar.setVisibility(View.GONE);

            if (tours != null && !tours.isEmpty()) {
                Log.d(TAG, "Received " + tours.size() + " tours from Firestore");
                allTours = new ArrayList<>(tours);

                // Apply current search filter and category filter
                filterTours(searchInput.getText().toString());
            } else {
                Log.d(TAG, "No tours received from Firestore");
                noResultsText.setVisibility(View.VISIBLE);

                if (currentCategory != null && !currentCategory.isEmpty()) {
                    noResultsText.setText("No tours available in category: " + currentCategory);
                } else {
                    noResultsText.setText("No tours available. Please try again later.");
                }
            }
        });
    }

    private void setupSearch() {
        // Add text change listener to search input
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter tours based on search query
                filterTours(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Set initial hint based on category
        if (currentCategory != null && !currentCategory.isEmpty()) {
            // Format category text nicely (capitalize first letter)
            String displayCategory = currentCategory.substring(0, 1).toUpperCase() +
                    currentCategory.substring(1);
            searchInput.setHint("Search in " + displayCategory + "...");
        } else {
            searchInput.setHint("Search for tours...");
        }
    }

    private void filterTours(String query) {
        if (allTours == null || allTours.isEmpty()) {
            Log.d(TAG, "Cannot filter: tour list is empty or null");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Clear previous results
        filteredTours.clear();

        // First filter by category if applicable
        List<Tour> categoryFilteredTours = new ArrayList<>();
        if (currentCategory != null && !currentCategory.isEmpty()) {
            for (Tour tour : allTours) {
                if (matchesCategory(tour, currentCategory)) {
                    categoryFilteredTours.add(tour);
                }
            }
            Log.d(TAG, "Category filter applied: " + currentCategory +
                    ", found " + categoryFilteredTours.size() + " tours");
        } else {
            categoryFilteredTours.addAll(allTours);
        }

        // Then filter by search query
        if (query.isEmpty()) {
            // Show all category-filtered tours if query is empty
            filteredTours.addAll(categoryFilteredTours);
            Log.d(TAG, "Empty query: showing all " + filteredTours.size() + " tours");
        } else {
            // Filter tours based on title, description, or location containing the query (case insensitive)
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());

            for (Tour tour : categoryFilteredTours) {
                // Check if tour matches search criteria
                boolean matchesTitle = tour.getTitle() != null &&
                        tour.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);

                boolean matchesLocation = false;
                if (tour.getLocation() != null) {
                    matchesLocation = tour.getLocation().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                } else if (tour.getAddress() != null) {
                    matchesLocation = tour.getAddress().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                }

                boolean matchesDescription = tour.getDescription() != null &&
                        tour.getDescription().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);

                if (matchesTitle || matchesLocation || matchesDescription) {
                    filteredTours.add(tour);
                }
            }

            Log.d(TAG, "Found " + filteredTours.size() + " tours matching query: " + query);
        }

        // Update UI
        progressBar.setVisibility(View.GONE);

        // Show/hide no results message
        if (filteredTours.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
            toursRecyclerView.setVisibility(View.GONE);

            if (currentCategory != null && !currentCategory.isEmpty()) {
                String displayCategory = currentCategory.substring(0, 1).toUpperCase() +
                        currentCategory.substring(1);

                if (query.isEmpty()) {
                    noResultsText.setText("No tours found in category: " + displayCategory);
                } else {
                    noResultsText.setText("No tours found in category \"" + displayCategory +
                            "\" matching \"" + query + "\"");
                }
            } else {
                if (query.isEmpty()) {
                    noResultsText.setText("No tours available");
                } else {
                    noResultsText.setText("No tours found matching \"" + query + "\"");
                }
            }
        } else {
            noResultsText.setVisibility(View.GONE);
            toursRecyclerView.setVisibility(View.VISIBLE);

            // Notify adapter of data change
            adapter.notifyDataSetChanged();
        }
    }

    private boolean matchesCategory(Tour tour, String category) {
        // Case-insensitive category matching
        if (tour.getCategory() != null) {
            return tour.getCategory().equalsIgnoreCase(category);
        }

        // Try to infer category from tour content if not explicitly set
        return inferCategory(tour).equalsIgnoreCase(category);
    }

    private String inferCategory(Tour tour) {
        String title = tour.getTitle() != null ? tour.getTitle().toLowerCase() : "";
        String desc = tour.getDescription() != null ? tour.getDescription().toLowerCase() : "";
        String location = tour.getLocation() != null ? tour.getLocation().toLowerCase() : "";
        String combined = title + " " + desc + " " + location;

        if (combined.contains("beach") || combined.contains("sea") ||
                combined.contains("ocean") || combined.contains("bay") ||
                combined.contains("island")) {
            return "beach";
        } else if (combined.contains("mountain") || combined.contains("hill") ||
                combined.contains("trek") || combined.contains("hiking") ||
                combined.contains("valley") || combined.contains("peak")) {
            return "mountain";
        } else if (combined.contains("city") || combined.contains("urban") ||
                combined.contains("downtown") || combined.contains("metropolis")) {
            return "city";
        } else {
            return "attraction";
        }
    }

    @Override
    public void onTourClick(Tour tour) {
        // Handle tour click
        Toast.makeText(this, "Selected: " + tour.getTitle(), Toast.LENGTH_SHORT).show();

        // You can open the tour detail activity here
        // Intent intent = new Intent(this, TourDetailActivity.class);
        // intent.putExtra("TOUR_ID", tour.getId());
        // startActivity(intent);
    }

    @Override
    public void onBookmarkClick(Tour tour, int position) {
        // Toggle bookmark status
        viewModel.toggleBookmark(tour);

        // Show notification toast
        if (tour.isBookmarked()) {
            Toast.makeText(this, "Bookmarked: " + tour.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Removed bookmark from: " + tour.getTitle(), Toast.LENGTH_SHORT).show();
        }

        // Update the specific item
        adapter.notifyItemChanged(position);
    }



}