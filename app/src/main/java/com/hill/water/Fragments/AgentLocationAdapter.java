package com.hill.water.Fragments;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hill.water.R;

import java.util.ArrayList;
import java.util.List;

public class AgentLocationAdapter extends RecyclerView.Adapter<AgentLocationAdapter.ViewHolder> {
    private List<AgentLocationSummary> locationList;
    private Context context;
    private OnMapClickListener mapClickListener;

    public interface OnMapClickListener {
        void onMapClick(List<LocationData> locations);
    }

    public AgentLocationAdapter(List<AgentLocationSummary> locationList, OnMapClickListener listener) {
        this.locationList = locationList;
        this.mapClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AgentLocationSummary summary = locationList.get(position);

        holder.agentNameTextView.setText(" " + summary.getAgentName());
        holder.agentPhoneNumberTextView.setText(" " + summary.getAgentPhone());

        holder.locationSummaryTextView.setText(" " + summary.getTotalLocations() + " places");

        holder.dateTextView.setText(" " + summary.getLastDate());

        holder.googleMapImageView.setOnClickListener(view -> {
            if (mapClickListener != null) {
                Log.d("AgentLocationMapActivity", "Map clicked, sending locations...");
                mapClickListener.onMapClick(summary.getLocations());
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView agentNameTextView, agentPhoneNumberTextView, locationSummaryTextView, dateTextView;
        public ImageView googleMapImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            agentNameTextView = itemView.findViewById(R.id.agentNameTextView);
            agentPhoneNumberTextView = itemView.findViewById(R.id.agentPhoneNumberTextView);
            locationSummaryTextView = itemView.findViewById(R.id.locationSummaryTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            googleMapImageView = itemView.findViewById(R.id.googleMapImageView);
        }
    }
}
