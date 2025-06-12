package com.hill.water.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hill.water.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class StockFragment extends Fragment {

    private TextView tvBalanceStock1L, tvBalanceStock250ml, tvBalanceStock500ml;
    private EditText etNewStock;
    private Button btnSubmitStock;
    private Spinner spinnerBottleType;
    private ImageView ivEditStock1L, ivEditStock250ml, ivEditStock500ml;

    private Map<String, Integer> balanceStockMap = new HashMap<>();
    private DatabaseReference stockRef, editRequestRef, userRef;
    private String workerName, workerPhone;
    private boolean userInfoLoaded = false;
    private TextView tvBalanceStock5L;
    private ImageView ivEditStock5L;

    public StockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock, container, false);

        tvBalanceStock1L = view.findViewById(R.id.tv_balance_stock_1l);
        tvBalanceStock250ml = view.findViewById(R.id.tv_balance_stock_250ml);
        tvBalanceStock500ml = view.findViewById(R.id.tv_balance_stock_500ml);
        etNewStock = view.findViewById(R.id.et_new_stock);
        btnSubmitStock = view.findViewById(R.id.btn_submit_stock);
        spinnerBottleType = view.findViewById(R.id.spinner_bottle_type);

        ivEditStock1L = view.findViewById(R.id.iv_edit_stock_1l);
        ivEditStock250ml = view.findViewById(R.id.iv_edit_stock_250ml);
        ivEditStock500ml = view.findViewById(R.id.iv_edit_stock_500ml);
        tvBalanceStock5L = view.findViewById(R.id.tv_balance_stock_5l);
        ivEditStock5L = view.findViewById(R.id.iv_edit_stock_5l);

        stockRef = FirebaseDatabase.getInstance().getReference("stock");
        editRequestRef = FirebaseDatabase.getInstance().getReference("stock_edit_requests");
        userRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        userRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    workerName = snapshot.child("fullName").getValue(String.class);
                    workerPhone = snapshot.child("phoneNumber").getValue(String.class);
                    userInfoLoaded = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.bottle_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBottleType.setAdapter(adapter);

        stockRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                balanceStockMap.clear();
                for (DataSnapshot typeSnapshot : snapshot.getChildren()) {
                    String type = typeSnapshot.getKey();
                    Integer balance = typeSnapshot.getValue(Integer.class);
                    balanceStockMap.put(type, balance != null ? balance : 0);
                }
                updateBalanceStockText();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load stock data", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmitStock.setOnClickListener(v -> {
            String selectedBottleType = spinnerBottleType.getSelectedItem().toString();
            if (selectedBottleType.equals("Select Bottle Type")) {
                Toast.makeText(getActivity(), "Please select a bottle type", Toast.LENGTH_SHORT).show();
                return;
            }

            String newStockInput = etNewStock.getText().toString();
            if (!newStockInput.isEmpty()) {
                int newStock = Integer.parseInt(newStockInput);
                updateStock(selectedBottleType, newStock);
            } else {
                Toast.makeText(getActivity(), "Please enter new stock", Toast.LENGTH_SHORT).show();
            }
        });

        ivEditStock1L.setOnClickListener(v -> {
            if (userInfoLoaded) {
                sendEditRequest("1L(12piece)");
            } else {
                Toast.makeText(getActivity(), "User info not loaded, please wait", Toast.LENGTH_SHORT).show();
            }
        });
        ivEditStock250ml.setOnClickListener(v -> {
            if (userInfoLoaded) {
                sendEditRequest("250ml(24piece)");
            } else {
                Toast.makeText(getActivity(), "User info not loaded, please wait", Toast.LENGTH_SHORT).show();
            }
        });
        ivEditStock500ml.setOnClickListener(v -> {
            if (userInfoLoaded) {
                sendEditRequest("500ml(24piece)");
            } else {
                Toast.makeText(getActivity(), "User info not loaded, please wait", Toast.LENGTH_SHORT).show();
            }
        });
        ivEditStock5L.setOnClickListener(v -> {
            if (userInfoLoaded) {
                sendEditRequest("5L(1piece)");
            } else {
                Toast.makeText(getActivity(), "User info not loaded, please wait", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void updateStock(String type, int newStock) {
        int updatedStock = balanceStockMap.getOrDefault(type, 0) + newStock;
        balanceStockMap.put(type, updatedStock);
        updateBalanceStockText();

        etNewStock.setText("");
        spinnerBottleType.setSelection(0);

        stockRef.child(type).setValue(updatedStock)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Stock updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to update stock", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateBalanceStockText() {
        tvBalanceStock1L.setText("1 Litre: " + balanceStockMap.getOrDefault("1L(12piece)", 0));
        tvBalanceStock250ml.setText("250ml: " + balanceStockMap.getOrDefault("250ml(24piece)", 0));
        tvBalanceStock500ml.setText("500ml: " + balanceStockMap.getOrDefault("500ml(24piece)", 0));
        tvBalanceStock5L.setText("5 Litre: " + balanceStockMap.getOrDefault("5L(1piece)", 0));
    }

    private void sendEditRequest(String bottleType) {
        String newStockInput = etNewStock.getText().toString();
        if (newStockInput.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter new stock for edit request", Toast.LENGTH_SHORT).show();
            return;
        }

        int newStock = Integer.parseInt(newStockInput);
        int currentStock = balanceStockMap.getOrDefault(bottleType, 0);

        String requestId = editRequestRef.push().getKey();
        if (requestId != null) {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("bottleType", bottleType);
            requestMap.put("requestedStock", newStock);
            requestMap.put("currentStock", currentStock);
            requestMap.put("workerName", workerName);
            requestMap.put("workerPhone", workerPhone);
            requestMap.put("status", "pending");

            editRequestRef.child(requestId).setValue(requestMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            etNewStock.setText("");
                            Toast.makeText(getActivity(),"Request sent to Admin",Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to send edit request", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}