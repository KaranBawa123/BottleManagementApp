package com.hill.water.Fragments;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.hill.water.R;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Orders> ordersList;

    public OrdersAdapter(List<Orders> ordersList) {
        this.ordersList = ordersList;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orders, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Orders order = ordersList.get(position);

        holder.orderDateTextView.setText(" " + order.getOrderDate());
        holder.quantityTextView.setText(" " + order.getQuantity());
        holder.nameTextView.setText(" " + order.getName());
        holder.shopNameTextView.setText(" " + order.getShopName());
        holder.shopAddressTextView.setText(" " + order.getShopAddress());
        holder.phoneNumberTextView.setText(" " + order.getPhoneNumber());
        holder.bottleTypeTextView.setText(" " + order.getBottleType());
        holder.itemNameTextView.setText(" " + order.getItemName());
        holder.totalAmountTextView.setText(" " + order.getTotalAmount());
        holder.rateTextView.setText(" " + order.getRate());
        holder.tv_dispatchStatus.setText(" "+order.getDispatchStatus());

        int customGreen = Color.parseColor("#298b28");
        int customRed = Color.parseColor("#CE0000");

        if (order.getDispatchStatus().equals("dispatched")) {
            holder.tv_dispatchStatus.setTextColor(customGreen);
        } else if (order.getDispatchStatus().equals("pending")) {
            holder.tv_dispatchStatus.setTextColor(customRed);
        }

    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDateTextView, quantityTextView, nameTextView, shopNameTextView, shopAddressTextView,
                phoneNumberTextView, bottleTypeTextView, itemNameTextView, totalAmountTextView, rateTextView,tv_dispatchStatus;

        public OrderViewHolder(View itemView) {
            super(itemView);
            orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            shopNameTextView = itemView.findViewById(R.id.shopNameTextView);
            shopAddressTextView = itemView.findViewById(R.id.shopAddressTextView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNumberTextView);
            bottleTypeTextView = itemView.findViewById(R.id.bottleTypeTextView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            totalAmountTextView = itemView.findViewById(R.id.totalAmountTextView);
            rateTextView = itemView.findViewById(R.id.rateTextView);
            tv_dispatchStatus = itemView.findViewById(R.id.tv_dispatchStatus);
        }
    }
}
