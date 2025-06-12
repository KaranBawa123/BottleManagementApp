package com.hill.water.Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hill.water.R;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    private List<DataItem> dataList;

    public DataAdapter(List<DataItem> dataList) {
        this.dataList = dataList;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_data, parent, false);
        return new DataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        DataItem dataItem = dataList.get(position);
        holder.dateTextView.setText(dataItem.getDate());
        holder.oneLitreTextView.setText("1 Litre: " + dataItem.getOneLitreCount());
        holder.twoFiftyMlTextView.setText("250ml: " + dataItem.getTwoFiftyMlCount());
        holder.fiveHundredMlTextView.setText("500ml: " + dataItem.getFiveHundredMlCount());
        holder.fiveLitreTextView.setText("5 Litre: " + dataItem.getFiveLitreCount());
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateData(List<DataItem> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }

    public class DataViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView, oneLitreTextView, twoFiftyMlTextView, fiveHundredMlTextView,fiveLitreTextView;

        public DataViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            oneLitreTextView = itemView.findViewById(R.id.oneLitreTextView);
            twoFiftyMlTextView = itemView.findViewById(R.id.twoFiftyMlTextView);
            fiveHundredMlTextView = itemView.findViewById(R.id.fiveHundredMlTextView);
            fiveLitreTextView = itemView.findViewById(R.id.fiveLitreTextView);
        }
    }

}
