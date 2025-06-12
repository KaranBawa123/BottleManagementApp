package com.hill.water.Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hill.water.R;

import java.util.List;

public class PaidInvoicesAdapter extends RecyclerView.Adapter<PaidInvoicesAdapter.PaidInvoiceViewHolder> {

    private final List<PaidInvoice> paidInvoices;

    public PaidInvoicesAdapter(List<PaidInvoice> paidInvoices) {
        this.paidInvoices = paidInvoices;
    }

    @NonNull
    @Override
    public PaidInvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paid_invoice, parent, false);
        return new PaidInvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaidInvoiceViewHolder holder, int position) {
        PaidInvoice paidInvoice = paidInvoices.get(position);
        holder.bind(paidInvoice);
    }

    @Override
    public int getItemCount() {
        return paidInvoices.size();
    }

    static class PaidInvoiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewInvoiceNumber;
        private final TextView textViewDate;
        private final TextView textViewTotalAmount;
        private final TextView textViewBottleType;
        private final TextView textViewItemName;
        private final TextView textViewLocation;
        private final TextView textViewPaymentMethod;
        private final TextView textViewQuantity;
        private final TextView textViewRate;
        private final TextView tv_name;
        private final TextView tv_phone_number;
        private final TextView textViewPaymentDate;

        public PaidInvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewInvoiceNumber = itemView.findViewById(R.id.textViewInvoiceNumber);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTotalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            textViewBottleType = itemView.findViewById(R.id.textViewBottleType);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewPaymentMethod = itemView.findViewById(R.id.textViewPaymentMethod);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewRate = itemView.findViewById(R.id.textViewRate);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_phone_number = itemView.findViewById(R.id.tv_phone_number);
            textViewPaymentDate = itemView.findViewById(R.id.textViewPaymentDate);
        }

        public void bind(PaidInvoice paidInvoice) {
            textViewInvoiceNumber.setText("Dispatch No: " + paidInvoice.getInvoiceNumber());
            textViewDate.setText(" " + paidInvoice.getDate());
            textViewTotalAmount.setText(" " + paidInvoice.getTotal());
            textViewBottleType.setText(" " + paidInvoice.getBottleType());
            textViewItemName.setText(" " + paidInvoice.getItemName());
            textViewLocation.setText(" " + paidInvoice.getLocation());
            textViewPaymentMethod.setText(paidInvoice.getPaymentMethod());
            textViewQuantity.setText(" " + paidInvoice.getQuantity());
            textViewRate.setText(" " + paidInvoice.getRate());
            tv_name.setText(paidInvoice.getName());
            tv_phone_number.setText(paidInvoice.getPhoneNumber());
            textViewPaymentDate.setText(paidInvoice.getPaymentDate());
        }
    }
}
