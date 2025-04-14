package com.example.travelerapp.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelerapp.R;
import com.example.travelerapp.model.BookingDateOption;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DateOptionAdapter extends RecyclerView.Adapter<DateOptionAdapter.DateViewHolder> {

    private List<BookingDateOption> dateOptions;
    private Context context;
    private OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(int position);
    }

    public DateOptionAdapter(Context context, List<BookingDateOption> dateOptions, OnDateSelectedListener listener) {
        this.context = context;
        this.dateOptions = dateOptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_date_option, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        BookingDateOption dateOption = dateOptions.get(position);

        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

        // Set day of week
        holder.dayOfWeekText.setText(dateOption.getDayName());

        // Set date
        holder.dateText.setText(dateFormat.format(dateOption.getDate()));

        // Update styling based on selection state
        if (dateOption.isSelected()) {
            holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.date_selected_border));
            holder.dayOfWeekText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.dateText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.date_option_border));
            holder.dayOfWeekText.setTextColor(ContextCompat.getColor(context, R.color.textPrimary));
            holder.dateText.setTextColor(ContextCompat.getColor(context, R.color.textPrimary));
        }

        // Set click listener
        final int pos = position; // Create final variable for use in lambda
        holder.container.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDateSelected(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateOptions != null ? dateOptions.size() : 0;
    }

    public void updateData(List<BookingDateOption> newDateOptions) {
        this.dateOptions = newDateOptions;
        notifyDataSetChanged();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView dayOfWeekText;
        TextView dateText;

        DateViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.date_container);
            dayOfWeekText = itemView.findViewById(R.id.day_of_week_text);
            dateText = itemView.findViewById(R.id.date_text);
        }
    }
}