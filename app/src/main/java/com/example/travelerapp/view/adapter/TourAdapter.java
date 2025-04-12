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

public class TourAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_RECENTLY = 0;
    private static final int VIEW_TYPE_MAIN = 1;

    private Context context;
    private List<Tour> tourList;
    private OnTourClickListener listener;

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

    private void bindRecentlyTourViewHolder(RecentlyTourViewHolder holder, Tour tour, int position) {
        holder.nameTextView.setText(tour.getName());
        holder.locationTextView.setText(tour.getLocation());
        holder.ratingTextView.setText(tour.getRating() + " (" + tour.getReviewCount() + ")");
        holder.priceTextView.setText(tour.getPrice());
        holder.imageView.setImageResource(tour.getImageResourceId());

        if (tour.isBookmarked()) {
            holder.bookmarkButton.setImageResource(R.drawable.bookmarked);
        } else {
            holder.bookmarkButton.setImageResource(R.drawable.bookmark);
        }

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

    private void bindMainTourViewHolder(MainTourViewHolder holder, Tour tour, int position) {
        holder.nameTextView.setText(tour.getName());
        holder.locationTextView.setText(tour.getLocation());
        holder.ratingTextView.setText(tour.getRating());
        holder.priceTextView.setText(tour.getPrice());
        holder.imageView.setImageResource(tour.getImageResourceId());

        if (tour.isBookmarked()) {
            holder.bookmarkButton.setImageResource(R.drawable.bookmarked);
        } else {
            holder.bookmarkButton.setImageResource(R.drawable.bookmark);
        }

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
            // Fix view binding to match IDs in item_main_tour.xml
            imageView = itemView.findViewById(R.id.imgTour);
            nameTextView = itemView.findViewById(R.id.tvTourName);
            locationTextView = itemView.findViewById(R.id.tvLocation);
            ratingTextView = itemView.findViewById(R.id.tvRating);
            priceTextView = itemView.findViewById(R.id.tvPrice);
            bookmarkButton = itemView.findViewById(R.id.btnBookmark);
        }
    }
}