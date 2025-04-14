package com.example.travelerapp.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelerapp.R;
import com.example.travelerapp.model.Tour;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TourAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "TourAdapter";
    private static final int VIEW_TYPE_RECENTLY = 0;
    private static final int VIEW_TYPE_MAIN = 1;

    private Context context;
    private List<Tour> tourList;
    private OnTourClickListener listener;

    // Simple image cache
    private Map<String, Bitmap> imageCache = new HashMap<>();

    public interface OnTourClickListener {
        void onTourClick(Tour tour);
        void onBookmarkClick(Tour tour, int position);
    }

    public TourAdapter(Context context, List<Tour> tourList, OnTourClickListener listener) {
        this.context = context;
        this.tourList = tourList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return tourList.get(position).isRecently() ? VIEW_TYPE_RECENTLY : VIEW_TYPE_MAIN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_RECENTLY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recently_tour, parent, false);
            return new RecentlyTourViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_main_tour, parent, false);
            return new MainTourViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Tour tour = tourList.get(position);

        if (holder instanceof RecentlyTourViewHolder) {
            bindRecentlyTourViewHolder((RecentlyTourViewHolder) holder, tour, position);
        } else if (holder instanceof MainTourViewHolder) {
            bindMainTourViewHolder((MainTourViewHolder) holder, tour, position);
        }
    }

    // In TourAdapter.java, update the bindRecentlyTourViewHolder method:

    private void bindRecentlyTourViewHolder(RecentlyTourViewHolder holder, Tour tour, int position) {
        // Set text data with null checks
        holder.nameTextView.setText(tour.getTitle() != null ? tour.getTitle() : "");
        holder.locationTextView.setText(tour.getLocation() != null ? tour.getLocation() : "");

        // Format rating with review count
        holder.ratingTextView.setText(tour.getRatingDisplay());

        // Set price
        holder.priceTextView.setText(tour.getPrice() != null ? tour.getPrice() : "Contact for price");

        // Load image
        loadTourImage(holder.imageView, tour);

        // Set bookmark status
        updateBookmarkIcon(holder.bookmarkButton, tour.isBookmarked());

        // Set click listeners
        setupClickListeners(holder.itemView, holder.bookmarkButton, tour, position);
    }

    private void bindMainTourViewHolder(MainTourViewHolder holder, Tour tour, int position) {
        // Set text data
        holder.nameTextView.setText(tour.getTitle() != null ? tour.getTitle() : "");
        holder.locationTextView.setText(tour.getLocation());

        // Format rating
        String ratingText = String.format(Locale.getDefault(), "%.1f/10", tour.getRating());
        holder.ratingTextView.setText(ratingText);

        // Set price
        holder.priceTextView.setText(tour.getPrice() != null ? tour.getPrice() : "");

        // Load image
        loadTourImage(holder.imageView, tour);

        // Set bookmark status
        updateBookmarkIcon(holder.bookmarkButton, tour.isBookmarked());

        // Set click listeners
        setupClickListeners(holder.itemView, holder.bookmarkButton, tour, position);
    }

    private void loadTourImage(ImageView imageView, Tour tour) {
        // First set a placeholder or default image
        imageView.setImageResource(R.drawable.ic_launcher_background); // Use a placeholder resource

        // Check if we have a URL first
        if (tour.getTourImageUrl() != null && !tour.getTourImageUrl().isEmpty()) {
            // Check if image is in cache
            if (imageCache.containsKey(tour.getTourImageUrl())) {
                imageView.setImageBitmap(imageCache.get(tour.getTourImageUrl()));
                return;
            }

            // Load image in background
            new ImageLoadTask(imageView, tour.getTourImageUrl()).execute();
        } else {
            // Fall back to resource ID if no URL
            int resourceId = tour.getImageResourceId();
            if (resourceId != 0) {
                imageView.setImageResource(resourceId);
            }
        }
    }

    // AsyncTask for loading images without using a library
    private class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
        private ImageView imageView;
        private String url;

        public ImageLoadTask(ImageView imageView, String url) {
            this.imageView = imageView;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                // Add to cache
                imageCache.put(url, result);
                imageView.setImageBitmap(result);
            }
        }
    }

    private void updateBookmarkIcon(ImageButton bookmarkButton, boolean isBookmarked) {
        bookmarkButton.setImageResource(isBookmarked ?
                R.drawable.bookmarked :
                R.drawable.bookmark);
    }

    private void setupClickListeners(View itemView, ImageButton bookmarkButton, Tour tour, int position) {
        // Item click listener
        itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTourClick(tour);
            }
        });

        // Bookmark click listener
        bookmarkButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookmarkClick(tour, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tourList != null ? tourList.size() : 0;
    }

    // Update data method for refreshing adapter data
    public void updateData(List<Tour> newTours) {
        this.tourList = newTours;
        notifyDataSetChanged();
    }

    public static class RecentlyTourViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView locationTextView;
        TextView ratingTextView;
        TextView priceTextView;
        ImageButton bookmarkButton;

        public RecentlyTourViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgTour);
            nameTextView = itemView.findViewById(R.id.tvTourName);
            locationTextView = itemView.findViewById(R.id.tvLocation);
            ratingTextView = itemView.findViewById(R.id.tvRating);
            priceTextView = itemView.findViewById(R.id.tvPrice);
            bookmarkButton = itemView.findViewById(R.id.btnBookmark);
        }
    }

    public static class MainTourViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView locationTextView;
        TextView ratingTextView;
        TextView priceTextView;
        ImageButton bookmarkButton;

        public MainTourViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgTour);
            nameTextView = itemView.findViewById(R.id.tvTourName);
            locationTextView = itemView.findViewById(R.id.tvLocation);
            ratingTextView = itemView.findViewById(R.id.tvRating);
            priceTextView = itemView.findViewById(R.id.tvPrice);
            bookmarkButton = itemView.findViewById(R.id.btnBookmark);
        }
    }
}