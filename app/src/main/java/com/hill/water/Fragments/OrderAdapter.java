package com.hill.water.Fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hill.water.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;
    private DatabaseReference database;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        database = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvName.setText(order.getName());
        holder.tvPhoneNumber.setText(order.getPhoneNumber());
        holder.tvShopName.setText(order.getShopName());
        holder.tvShopAddress.setText(order.getShopAddress());
        holder.tvItemName.setText(order.getItemName());
        holder.tvBottleType.setText(order.getBottleType());
        holder.tvQuantity.setText(String.valueOf(order.getQuantity()));
        holder.tvTotal.setText(String.valueOf(order.getTotalAmount()));
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvDispatchStatus.setText(order.getDispatchStatus());
        holder.tvOrderStatus.setText(order.getOrderStatus());

        int customGreen = Color.parseColor("#298b28");
        int customRed = Color.parseColor("#CE0000");

        if (order.getOrderStatus().equals("approved")) {
            holder.tvOrderStatus.setTextColor(customGreen);
        } else if (order.getOrderStatus().equals("pending")) {
            holder.tvOrderStatus.setTextColor(customRed);
        } else if (order.getOrderStatus().equals("rejected")) {
            holder.tvOrderStatus.setTextColor(customRed);
        }

        if (order.getDispatchStatus().equals("dispatched")) {
            holder.tvDispatchStatus.setTextColor(customGreen);
        } else if (order.getDispatchStatus().equals("pending")) {
            holder.tvDispatchStatus.setTextColor(customRed);
        }

        holder.btnApprove.setOnClickListener(v -> {
            String orderId = order.getOrderId();
            String userId = order.getUid();

            database.child("CustomerOrders").child(userId).child(orderId).child("orderStatus").setValue("approved").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    order.setOrderStatus("approved");
                    notifyItemChanged(position);
                }
            });
        });

        holder.btnReject.setOnClickListener(v -> {
            String orderId = order.getOrderId();
            String userId = order.getUid();

            database.child("CustomerOrders").child(userId).child(orderId).child("orderStatus").setValue("rejected").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    order.setOrderStatus("rejected");
                    order.setIsProcessed(true);

                    database.child("CustomerOrders").child(userId).child(orderId).child("isProcessed").setValue(true);

                    notifyItemChanged(position);
                }
            });
            orderList.remove(position);
            notifyItemRemoved(position);
        });

        holder.btnDispatch.setOnClickListener(v -> {
            String orderId = order.getOrderId();
            String userId = order.getUid();
            int quantity = (int) Math.round(order.getQuantity());
            String bottleType = order.getBottleType();

            updateStock(quantity, bottleType, isStockSufficient -> {
                if (!isStockSufficient) return;
                
                Map<String, Object> updates = new HashMap<>();
                updates.put("dispatchStatus", "dispatched");
                updates.put("orderStatus", "approved");
                updates.put("isProcessed", true);

                database.child("CustomerOrders").child(userId).child(orderId).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        order.setDispatchStatus("dispatched");
                        order.setOrderStatus("approved");
                        order.setIsProcessed(true);

                        notifyItemChanged(position);

                        database.child("invoiceNumberCounter").get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                int invoiceNumber = task1.getResult().getValue(Integer.class);

                                Map<String, Object> invoiceData = new HashMap<>();
                                invoiceData.put("name", order.getName());
                                invoiceData.put("bottleType", order.getBottleType());
                                invoiceData.put("date", order.getOrderDate());
                                invoiceData.put("total", order.getTotalAmount());
                                invoiceData.put("itemName", order.getItemName());
                                invoiceData.put("location", order.getShopAddress());
                                invoiceData.put("phoneNumber", order.getPhoneNumber());
                                invoiceData.put("quantity", order.getQuantity());
                                invoiceData.put("rate", order.getRate());
                                invoiceData.put("invoiceNumber", invoiceNumber);

                                database.child("invoices").child(String.valueOf(invoiceNumber)).setValue(invoiceData);

                                database.child("invoiceNumberCounter").setValue(invoiceNumber + 1);

                                database.child("totaldues").get().addOnCompleteListener(totalTask -> {
                                    if (totalTask.isSuccessful()) {
                                        Integer currentTotalDues = totalTask.getResult().getValue(Integer.class);
                                        if (currentTotalDues == null) currentTotalDues = 0;

                                        int newTotalDues = currentTotalDues + (int) order.getTotalAmount();
                                        database.child("totaldues").setValue(newTotalDues);
                                    }
                                });

                                sendWhatsAppMessage(order);
                                orderList.remove(position);
                                notifyItemRemoved(position);
                            }
                        });
                    }
                });
            });
        });
    }
    private void sendWhatsAppMessage(Order order) {
        String phoneNumber = order.getPhoneNumber();
        String message = "Your Order has been dispatched.\n\n" +
                "Order Details:\n" +
                "Name: " + order.getName() + "\n" +
                "Shop: " + order.getShopName() + "\n" +
                "Address: " + order.getShopAddress() + "\n" +
                "Item: " + order.getItemName() + "\n" +
                "Bottle Type: " + order.getBottleType() + "\n" +
                "Quantity: " + order.getQuantity() + "\n" +
                "Total Amount: " + order.getTotalAmount() + "\n" +
                "Order Date: " + order.getOrderDate();

        String formattedNumber = phoneNumber.startsWith("+") ? phoneNumber : "+91" + phoneNumber;

        String url = "https://api.whatsapp.com/send?phone=" + formattedNumber + "&text=" + Uri.encode(message);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    private void updateStock(int quantity, String bottleType, StockUpdateCallback callback) {
        DatabaseReference bottleStockRef;

        switch (bottleType) {
            case "1L(12piece)":
                bottleStockRef = database.child("stock/1L(12piece)");
                break;
            case "250ml(24piece)":
                bottleStockRef = database.child("stock/250ml(24piece)");
                break;
            case "500ml(24piece)":
                bottleStockRef = database.child("stock/500ml(24piece)");
                break;
            case "5L(1piece)":
                bottleStockRef = database.child("stock/5L(1piece)");
                break;
            default:
                Toast.makeText(context, "Invalid bottle type", Toast.LENGTH_SHORT).show();
                callback.onStockUpdate(false);
                return;
        }

        bottleStockRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int currentStock = dataSnapshot.exists() ? dataSnapshot.getValue(Integer.class) : 0;
                int updatedStock = currentStock - quantity;

                if (updatedStock < 0) {
                    Toast.makeText(context, "Insufficient stock for " + bottleType, Toast.LENGTH_SHORT).show();
                    callback.onStockUpdate(false);
                    return;
                }

                bottleStockRef.setValue(updatedStock).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Stock updated for " + bottleType, Toast.LENGTH_SHORT).show();
                        callback.onStockUpdate(true);
                    } else {
                        Toast.makeText(context, "Failed to update stock", Toast.LENGTH_SHORT).show();
                        callback.onStockUpdate(false);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error fetching stock: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                callback.onStockUpdate(false);
            }
        });
    }

    interface StockUpdateCallback {
        void onStockUpdate(boolean isStockSufficient);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhoneNumber, tvShopName, tvShopAddress, tvItemName, tvBottleType, tvQuantity, tvTotal, tvOrderDate, tvDispatchStatus, tvOrderStatus;
        Button btnApprove, btnReject, btnDispatch;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhoneNumber = itemView.findViewById(R.id.tv_phone_number);
            tvShopName = itemView.findViewById(R.id.tv_shopName);
            tvShopAddress = itemView.findViewById(R.id.tv_shopAdrress);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvBottleType = itemView.findViewById(R.id.tv_bottle_type);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvOrderDate = itemView.findViewById(R.id.tv_date);
            tvDispatchStatus = itemView.findViewById(R.id.tv_dispatchStatus);
            tvOrderStatus = itemView.findViewById(R.id.tv_orderStatus);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnDispatch = itemView.findViewById(R.id.btn_dispatch);
        }
    }
}
