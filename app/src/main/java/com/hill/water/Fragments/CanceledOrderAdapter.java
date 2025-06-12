package com.hill.water.Fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hill.water.R;
import android.util.Log;

import java.util.List;

public class CanceledOrderAdapter extends RecyclerView.Adapter<CanceledOrderAdapter.ViewHolder> {

    private List<CanceledOrder> canceledOrdersList;
    private Context context;

    public CanceledOrderAdapter(List<CanceledOrder> canceledOrdersList, Context context) {

        if (context == null) {
            Log.e("CanceledOrderAdapter", "Context is null");
        } else {
            Log.d("CanceledOrderAdapter", "Context is not null");
        }
        this.canceledOrdersList = canceledOrdersList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (context == null) {
            Log.e("CanceledOrderAdapter", "Context is null in onCreateViewHolder");
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.item_cancelled_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CanceledOrder order = canceledOrdersList.get(position);

        holder.invoiceNumber.setText(" "+String.valueOf(order.getInvoiceNumber()));
        holder.itemName.setText(" "+order.getItemName());
        holder.bottleType.setText(" "+order.getBottleType());
        holder.quantity.setText(" "+String.valueOf(order.getQuantity()));
        holder.rate.setText(" "+String.valueOf(order.getRate()));
        holder.total.setText(" "+String.valueOf(order.getTotal()));
        holder.cancelReason.setText(" "+order.getCancelReason());
        holder.location.setText(" "+order.getLocation());
        holder.name.setText(" "+order.getName());
        holder.phoneNumber.setText(" "+order.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return canceledOrdersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView invoiceNumber, itemName, bottleType, quantity, rate, total, cancelReason, location, name, phoneNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            invoiceNumber = itemView.findViewById(R.id.invoiceNumber);
            itemName = itemView.findViewById(R.id.itemName);
            bottleType = itemView.findViewById(R.id.bottleType);
            quantity = itemView.findViewById(R.id.quantity);
            rate = itemView.findViewById(R.id.rate);
            total = itemView.findViewById(R.id.total);
            cancelReason = itemView.findViewById(R.id.cancelReason);
            location = itemView.findViewById(R.id.location);
            name = itemView.findViewById(R.id.name);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
        }
    }
}
