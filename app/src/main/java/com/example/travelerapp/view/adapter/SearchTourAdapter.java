package com.example.travelerapp.view.adapter;

import android.content.Context;
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

import java.util.List;

public class SearchTourAdapter extends RecyclerView.Adapter<SearchTourAdapter.TourViewHolder> {

    private Context context;
    private List<Tour> tourList;
    private OnTourClickListener listener;

    public interface OnTourClickListener {
        void onTourClick(Tour tour);
        void onBookmarkClick(Tour tour, int position);
    }

    public SearchTourAdapter(Context context, List<Tour> tourList, OnTourClickListener listener) {
        this.context = context;
        this.tourList = tourList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_tour, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tourList.get(position);

        holder.nameTextView.setText(tour.getName());
        holder.locationTextView.setText(tour.getLocation());

        // Format rating with review count
        String ratingText = tour.getRating() + " (" + tour.getReviewCount() + ")";
        holder.ratingTextView.setText(ratingText);

        holder.priceTextView.setText(tour.getPrice());
        holder.imageView.setImageResource(tour.getImageResourceId());

        // Set bookmark icon based on bookmark status
        if (tour.isBookmarked()) {
            holder.bookmarkButton.setImageResource(R.drawable.bookmarked);
        } else {
            holder.bookmarkButton.setImageResource(R.drawable.bookmark);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTourClick(tour);
            }
        });

        holder.bookmarkButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookmarkClick(tour, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView locationTextView;
        TextView ratingTextView;
        TextView priceTextView;
        ImageButton bookmarkButton;

        public TourViewHolder(@NonNull View itemView) {
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