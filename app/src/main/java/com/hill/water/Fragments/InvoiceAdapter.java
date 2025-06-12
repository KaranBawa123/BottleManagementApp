package com.hill.water.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hill.water.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {
    private Context context;
    private List<Invoice> invoiceList;
    private OnInvoiceClickListener onInvoiceClickListener;
    private double totaldues;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserRole;

    public interface OnInvoiceClickListener {
        void onInvoiceClick(Invoice invoice);
    }

    public InvoiceAdapter(Context context, List<Invoice> invoiceList, OnInvoiceClickListener listener, double totaldues) {
        this.context = context;
        this.invoiceList = invoiceList;
        this.onInvoiceClickListener = listener;
        this.totaldues = totaldues;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fetchUserRole();
    }

    private void fetchUserRole() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid()).child("role")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                currentUserRole = dataSnapshot.getValue(String.class);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(context, "Failed to fetch role", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoice invoice = invoiceList.get(position);
        holder.tvInvoiceNumber.setText("Dispatch No # : " + invoice.getInvoiceNumber());
        holder.tvItemName.setText(" " + invoice.getItemName());
        holder.tvBottleType.setText(" " + invoice.getBottleType());
        holder.tvQuantity.setText(" " + invoice.getQuantity());
        holder.tvRate.setText(String.format("%.2f", invoice.getRate()));
        holder.tvDate.setText(" " + invoice.getDate());
        holder.tvTotal.setText(String.format("%.2f", invoice.getTotal()));
        holder.tvLocation.setText(" " + invoice.getLocation());
        holder.tvName.setText(" " + invoice.getName());
        holder.tvPhonenumber.setText(" " + invoice.getPhoneNumber());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date invoiceDate = sdf.parse(invoice.getDate());
            Date currentDate = new Date();

            if (invoiceDate != null) {
                long diffInMillis = currentDate.getTime() - invoiceDate.getTime();
                long daysDue = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                if (daysDue == 0) {
                    holder.tv_payment_time.setText("Due Today");
                } else if (daysDue > 0) {
                    holder.tv_payment_time.setText(" "+daysDue + " days");
                    holder.tv_payment_time.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
                } else {
                    holder.tv_payment_time.setText("Due in " + Math.abs(daysDue) + " days");
                    holder.tv_payment_time.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            holder.tv_payment_time.setText("Date Error");
        }

        if (invoice.isPaid()) {
            holder.ivGooglePay.setVisibility(View.GONE);
            holder.ivCash.setVisibility(View.GONE);
            holder.tv_or.setVisibility(View.GONE);
            holder.tvPaymentStatus.setText("Payment Status: Paid");
            holder.tvPaymentStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
        } else {
            holder.ivGooglePay.setVisibility(View.VISIBLE);
            holder.ivCash.setVisibility(View.VISIBLE);
            holder.tvPaymentStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        }

        holder.ivGooglePay.setOnClickListener(v -> {
            if (currentUserRole != null && currentUserRole.equals("Admin")) {
                showDatePicker(invoice, "Google Pay", holder);
            } else {
                Toast.makeText(context, "Access Denied: Only Admin can process payments", Toast.LENGTH_SHORT).show();
            }
        });

        holder.ivCash.setOnClickListener(v -> {
            if (currentUserRole != null && currentUserRole.equals("Admin")) {
                showDatePicker(invoice, "Cash", holder);
            } else {
                Toast.makeText(context, "Access Denied: Only Admin can process payments", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(v -> onInvoiceClickListener.onInvoiceClick(invoice));

        holder.tvCancelOrder.setOnClickListener(v -> showCancelPopup(invoice));

        holder.tvEditOrder.setOnClickListener(v -> showEditOrderPopup(invoice, holder));

        holder.sendpaymentrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = invoice.getPhoneNumber().trim();

                if (!phoneNumber.startsWith("+91")) {
                    phoneNumber = "+91" + phoneNumber;
                }

                String message = "🔔 *Payment Reminder* 🔔\n\n" +
                        "🛒 *Order Details:*\n" +
                        "📦 *Dispatch No:* " + invoice.getInvoiceNumber() + "\n" +
                        "🍶 *Bottle Type:* " + invoice.getBottleType() + "\n" +
                        "🔢 *Quantity:* " + invoice.getQuantity() + "\n" +
                        "💰 *Total:* ₹" + String.format("%.2f", invoice.getTotal()) + "\n" +
                        "📅 *Order Date:* " + invoice.getDate() + "\n" +
                        "⏰ *Payment Due from:* " + holder.tv_payment_time.getText().toString() + "\n\n" +
                        "⚠️ Humble request to clear our dues as soon as possible. Thank you! Good-day 🙏"+"😊";

                sendWhatsApp(phoneNumber, message);
            }
        });
    }

    private void sendWhatsApp(String phoneNumber, String message) {
        try {

            String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error opening WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditOrderPopup(Invoice invoice, InvoiceViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_edit_order, null);

        EditText etNewRate = view.findViewById(R.id.etNewRate);
        EditText etNewTotal = view.findViewById(R.id.etNewTotal);
        Button btnOk = view.findViewById(R.id.btnOk);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnOk.setOnClickListener(v -> {
            String newRateStr = etNewRate.getText().toString().trim();
            String newTotalStr = etNewTotal.getText().toString().trim();

            if (newRateStr.isEmpty() || newTotalStr.isEmpty()) {
                Toast.makeText(context, "Rate and total cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            double newRate = Double.parseDouble(newRateStr);
            double newTotal = Double.parseDouble(newTotalStr);
            double oldTotal = invoice.getTotal();

            invoice.setRate(newRate);
            invoice.setTotal(newTotal);

            updateInvoiceInDatabase(invoice);

            adjustTotalDues(oldTotal, newTotal);

            holder.tvRate.setText(String.format("%.2f", newRate));
            holder.tvTotal.setText(String.format("%.2f", newTotal));

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void adjustTotalDues(double oldTotal, double newTotal) {
        double difference = newTotal - oldTotal;
        mDatabase.child("totaldues").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double currentDues = dataSnapshot.exists() ? dataSnapshot.getValue(Double.class) : 0;
                double updatedDues = currentDues + difference;
                mDatabase.child("totaldues").setValue(updatedDues);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to update total dues", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCancelPopup(Invoice invoice) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cancel Order");

        final EditText input = new EditText(context);
        input.setHint("Enter reason for cancellation");

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {

            String cancelReason = input.getText().toString();

            if (cancelReason.isEmpty()) {
                Toast.makeText(context, "Reason cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String bottleType = invoice.getBottleType();
            int quantity = invoice.getQuantity();
            updateBalanceStock(quantity, bottleType);
            updateTotalDues(invoice.getTotal(), true);

            int position = invoiceList.indexOf(invoice);
            if (position != -1) {
                invoiceList.remove(position);
                notifyItemRemoved(position);
            }

            saveCanceledOrderDetails(invoice, cancelReason);

            deleteInvoiceFromDatabase(invoice);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateTotalDues(double amount, boolean isCancel) {
        DatabaseReference totalDuesRef = FirebaseDatabase.getInstance().getReference("totaldues");

        totalDuesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double currentTotalDues = dataSnapshot.exists() ? dataSnapshot.getValue(Double.class) : 0;
                double newTotalDues = isCancel ? currentTotalDues - amount : currentTotalDues + amount;

                totalDuesRef.setValue(newTotalDues);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to fetch total dues", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCanceledOrderDetails(Invoice invoice, String cancelReason) {

        Map<String, Object> canceledOrder = new HashMap<>();
        canceledOrder.put("invoiceNumber", invoice.getInvoiceNumber());
        canceledOrder.put("itemName", invoice.getItemName());
        canceledOrder.put("bottleType", invoice.getBottleType());
        canceledOrder.put("quantity", invoice.getQuantity());
        canceledOrder.put("rate", invoice.getRate());
        canceledOrder.put("date", invoice.getDate());
        canceledOrder.put("total", invoice.getTotal());
        canceledOrder.put("location", invoice.getLocation());
        canceledOrder.put("name", invoice.getName());
        canceledOrder.put("phoneNumber", invoice.getPhoneNumber());
        canceledOrder.put("cancelReason", cancelReason);
        canceledOrder.put("timestamp", System.currentTimeMillis());

        String invoiceNumber = String.valueOf(invoice.getInvoiceNumber());

        FirebaseDatabase.getInstance().getReference("CancelledOrders")
                .child(invoiceNumber)
                .setValue(canceledOrder)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("InvoiceAdapter", "Order canceled and saved successfully");

                    } else {
                        Log.d("InvoiceAdapter", "Failed to save canceled order");
                    }
                });
    }

    private void deleteInvoiceFromDatabase(Invoice invoice) {

        DatabaseReference invoiceRef = FirebaseDatabase.getInstance().getReference("invoices");

        invoiceRef.child(String.valueOf(invoice.getInvoiceNumber())).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("InvoiceAdapter", "Invoice removed successfully from database.");
                    } else {
                        Log.d("InvoiceAdapter", "Failed to remove invoice from database.");
                    }
                });
    }

    private void updateBalanceStock(int quantity, String bottleType) {
        DatabaseReference bottleStockRef;

        switch (bottleType) {
            case "1L(12piece)":
                bottleStockRef = FirebaseDatabase.getInstance().getReference("stock/1L(12piece)");
                break;
            case "250ml(24piece)":
                bottleStockRef = FirebaseDatabase.getInstance().getReference("stock/250ml(24piece)");
                break;
            case "500ml(24piece)":
                bottleStockRef = FirebaseDatabase.getInstance().getReference("stock/500ml(24piece)");
                break;
            default:
                Toast.makeText(context, "Invalid bottle type", Toast.LENGTH_SHORT).show();
                return;
        }

        bottleStockRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int currentStock = dataSnapshot.exists() ? dataSnapshot.getValue(Integer.class) : 0;
                int updatedStock = currentStock + quantity;

                bottleStockRef.setValue(updatedStock);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to update stock", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker(Invoice invoice, String paymentMethod, InvoiceViewHolder holder) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
            String paymentDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDayOfMonth;
            invoice.setPaymentMethod(paymentMethod);
            invoice.setPaymentDate(paymentDate);

            showDiscountPopup(invoice);

        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void showDiscountPopup(Invoice invoice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Apply Discount")
                .setMessage("Do you want to give a discount on this dispatch?")
                .setPositiveButton("Yes", (dialog, which) -> showEnterDiscountPopup(invoice))
                .setNegativeButton("No", (dialog, which) -> {

                    invoice.setPaid(true);
                    updateInvoiceInDatabase(invoice);
                    onPaymentMade(invoice.getTotal());
                    sendWhatsAppMessage(invoice, false, 0);
                    removeInvoiceFromList(invoice);
                })
                .show();
    }

    private void showEnterDiscountPopup(Invoice invoice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Discount Amount");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter discount amount");

        builder.setView(input);

        builder.setPositiveButton("Apply", (dialog, which) -> {
            String discountStr = input.getText().toString().trim();
            if (!discountStr.isEmpty()) {
                double discount = Double.parseDouble(discountStr);
                if (discount >= 0 && discount <= invoice.getTotal()) {
                    double originalTotal = invoice.getTotal();
                    double newTotal = originalTotal - discount;

                    onPaymentMade(originalTotal);

                    invoice.setTotal(newTotal);
                    invoice.setPaid(true);
                    updateInvoiceInDatabase(invoice);

                    sendWhatsAppMessage(invoice, true, discount);
                    removeInvoiceFromList(invoice);
                } else {
                    Toast.makeText(context, "Invalid discount amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void removeInvoiceFromList(Invoice invoice) {
        int position = invoiceList.indexOf(invoice);
        if (position != -1) {
            invoiceList.remove(position);
            notifyItemRemoved(position);
        }
    }

    private void sendWhatsAppMessage(Invoice invoice, boolean isDiscounted, double discount) {
        String phoneNumber = invoice.getPhoneNumber();
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+91" + phoneNumber;
        }

        String message = "✨ *Payment Received* ✨\n\n" +
                "Dear Customer, \n\n" +
                "We have successfully received your payment for the following order:\n\n" +
                "📦 *Dispatch No:* " + invoice.getInvoiceNumber() + "\n" +
                (isDiscounted ? "💰 *Original Amount:* ₹" + String.format("%.2f", invoice.getTotal() + discount) + "\n" +
                        "🔻 *Discount Given:* ₹" + String.format("%.2f", discount) + "\n" : "") +
                "✅ *Final Amount Paid:* ₹" + String.format("%.2f", invoice.getTotal()) + "\n\n" +
                "Thank you for your timely payment! 😊\n" +
                "We appreciate your business. If you have any more orders, feel free to reach us via WhatsApp or Call. 🙇\n\n" +
                "Best regards,\n" +
                "The HillWater Team 🏞️";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message)));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    public void updateInvoiceList(List<Invoice> newInvoiceList) {
        invoiceList.clear();
        invoiceList.addAll(newInvoiceList);
        notifyDataSetChanged();
    }

    private void updateInvoiceInDatabase(Invoice invoice) {
        DatabaseReference invoiceRef = FirebaseDatabase.getInstance().getReference("invoices").child(String.valueOf(invoice.getInvoiceNumber()));
        invoiceRef.setValue(invoice);

        DatabaseReference paymentsRef = FirebaseDatabase.getInstance().getReference("payments").child(String.valueOf(invoice.getInvoiceNumber()));
        paymentsRef.setValue(invoice);
    }

    private void updateTotalDuesInDatabase(double newTotalDues) {
        DatabaseReference totalDuesRef = FirebaseDatabase.getInstance().getReference("totaldues");
        totalDuesRef.setValue(newTotalDues);
    }

    public void onPaymentMade(double amount) {
        DatabaseReference totalDuesRef = FirebaseDatabase.getInstance().getReference("totaldues");
        totalDuesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double currentTotalDues = dataSnapshot.getValue(Double.class);
                    double newTotalDues = currentTotalDues - amount;

                    updateTotalDuesInDatabase(newTotalDues);
                } else {
                    updateTotalDuesInDatabase(amount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to fetch total dues", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNumber, tvItemName, tvBottleType, tvQuantity, tvRate, tvDate, tvTotal, tvLocation, tvPaymentStatus, tv_or, tvName, tvPhonenumber,tv_payment_time,sendpaymentrequest;
        ImageView ivGooglePay, ivCash;
        Button tvEditOrder,tvCancelOrder;

        public InvoiceViewHolder(View itemView) {
            super(itemView);
            tvInvoiceNumber = itemView.findViewById(R.id.tv_invoice_number);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvBottleType = itemView.findViewById(R.id.tv_bottle_type);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvRate = itemView.findViewById(R.id.tv_rate);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
            tv_or = itemView.findViewById(R.id.tv_or);
            ivGooglePay = itemView.findViewById(R.id.iv_google_pay);
            ivCash = itemView.findViewById(R.id.iv_cash);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhonenumber = itemView.findViewById(R.id.tv_phone_number);
            tvCancelOrder = itemView.findViewById(R.id.tv_cancel_order);
            tvEditOrder = itemView.findViewById(R.id.tv_edit_order);
            tv_payment_time = itemView.findViewById(R.id.tv_payment_time);
            sendpaymentrequest = itemView.findViewById(R.id.sendpaymentrequest);
        }
    }
}
