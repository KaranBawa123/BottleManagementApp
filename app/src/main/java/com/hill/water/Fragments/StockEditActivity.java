package com.hill.water.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hill.water.R;
import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockEditActivity extends AppCompatActivity {

    private RecyclerView rvApprovalRequests;
    private RequestAdapter requestAdapter;
    private DatabaseReference editRequestRef, stockRef;
    private LottieAnimationView emptyAnimation;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stock_edit);

        rvApprovalRequests = findViewById(R.id.stock_edit_requests);
        emptyAnimation = findViewById(R.id.emptyAnimation);
        emptyTextView = findViewById(R.id.emptyStateText);

        rvApprovalRequests.setLayoutManager(new LinearLayoutManager(StockEditActivity.this));
        requestAdapter = new RequestAdapter();
        rvApprovalRequests.setAdapter(requestAdapter);

        editRequestRef = FirebaseDatabase.getInstance().getReference("stock_edit_requests");
        stockRef = FirebaseDatabase.getInstance().getReference("stock");

        fetchStockData();

        fetchRequests();
    }

    private void fetchStockData() {
        stockRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> balanceStockMap = new HashMap<>();
                for (DataSnapshot typeSnapshot : snapshot.getChildren()) {
                    String type = typeSnapshot.getKey();
                    Integer balance = typeSnapshot.getValue(Integer.class);
                    balanceStockMap.put(type, balance != null ? balance : 0);
                }
                requestAdapter.updateCurrentStockInRequests(balanceStockMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StockEditActivity.this, "Failed to load stock data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRequests() {
        editRequestRef.orderByChild("status")
                .equalTo("pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<StockEditRequest> requests = new ArrayList<>();
                        for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                            StockEditRequest request = requestSnapshot.getValue(StockEditRequest.class);
                            if (request != null) {
                                request.setRequestId(requestSnapshot.getKey());

                                int currentStock = request.getCurrentStock();
                                request.setCurrentStock(currentStock);
                                requests.add(request);
                            }
                        }
                        requestAdapter.setRequests(requests);
                        if (requests.isEmpty()) {
                            emptyAnimation.setVisibility(View.VISIBLE);
                            emptyTextView.setVisibility(View.VISIBLE);
                        } else {
                            emptyAnimation.setVisibility(View.GONE);
                            emptyTextView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StockEditActivity.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void approveRequest(StockEditRequest request) {
        String requestId = request.getRequestId();
        String bottleType = request.getBottleType();
        int requestedStock = request.getRequestedStock();

        stockRef.child(bottleType).setValue(requestedStock)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateRequestStatus(requestId, "approved");
                    } else {
                        Toast.makeText(StockEditActivity.this, "Failed to approve request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void rejectRequest(StockEditRequest request) {
        updateRequestStatus(request.getRequestId(), "rejected");
    }

    private void updateRequestStatus(String requestId, String status) {
        editRequestRef.child(requestId).child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        editRequestRef.child(requestId).removeValue().addOnCompleteListener(removeTask -> {
                            if (removeTask.isSuccessful()) {
                                Toast.makeText(StockEditActivity.this, "Request " + status + " and removed", Toast.LENGTH_SHORT).show();
                                fetchRequests();
                            } else {
                                Toast.makeText(StockEditActivity.this, "Failed to remove request", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(StockEditActivity.this, "Failed to update request status", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

        private List<StockEditRequest> requests = new ArrayList<>();
        private Map<String, Integer> balanceStockMap = new HashMap<>();

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stockedit_request, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
            StockEditRequest request = requests.get(position);
            holder.bind(request);
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        public void setRequests(List<StockEditRequest> requests) {
            this.requests = requests;
            notifyDataSetChanged();
        }

        public void updateCurrentStockInRequests(Map<String, Integer> balanceStockMap) {
            this.balanceStockMap = balanceStockMap;
            notifyDataSetChanged();
        }

        public class RequestViewHolder extends RecyclerView.ViewHolder {

            private TextView tvBottleType, tvRequestedStock, tvCurrentStock, tvWorkerName, tvWorkerPhone;
            private Button btnApprove, btnReject;

            public RequestViewHolder(@NonNull View itemView) {
                super(itemView);
                tvBottleType = itemView.findViewById(R.id.tv_bottle_type);
                tvRequestedStock = itemView.findViewById(R.id.tv_requested_stock);
                tvCurrentStock = itemView.findViewById(R.id.tv_current_stock);
                tvWorkerName = itemView.findViewById(R.id.tv_worker_name);
                tvWorkerPhone = itemView.findViewById(R.id.tv_worker_phone);
                btnApprove = itemView.findViewById(R.id.btn_approve);
                btnReject = itemView.findViewById(R.id.btn_reject);
            }

            public void bind(StockEditRequest request) {
                tvBottleType.setText(request.getBottleType());
                tvRequestedStock.setText(String.valueOf(request.getRequestedStock()));
                tvWorkerName.setText(request.getWorkerName());
                tvWorkerPhone.setText(request.getWorkerPhone());

                String bottleType = request.getBottleType();
                Integer currentStock = balanceStockMap.get(bottleType);
                tvCurrentStock.setText(currentStock != null ? String.valueOf(currentStock) : "Fetching...");

                btnApprove.setOnClickListener(v -> approveRequest(request));
                btnReject.setOnClickListener(v -> rejectRequest(request));
            }
        }
    }
}
