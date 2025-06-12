package com.hill.water.Fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hill.water.R;

public class AdminApprovalFragment extends Fragment {

    private TextView stockedittv, userauthtv, orderRequeststv,allOrderstv;
    private TextView stockEditCount, userAuthCount, orderRequestCount;
    private DatabaseReference stockEditRef, userRegisterRef, customerOrdersRef;

    public AdminApprovalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_approval, container, false);

        stockedittv = view.findViewById(R.id.stockedittv);
        userauthtv = view.findViewById(R.id.userauthtv);
        orderRequeststv = view.findViewById(R.id.orderRequeststv);
        allOrderstv = view.findViewById(R.id.allOrderstv);

        stockEditCount = view.findViewById(R.id.stock_edit_count);
        userAuthCount = view.findViewById(R.id.user_auth_count);
        orderRequestCount = view.findViewById(R.id.order_request_count);

        stockEditRef = FirebaseDatabase.getInstance().getReference("stock_edit_requests");
        userRegisterRef = FirebaseDatabase.getInstance().getReference("user_register_requests");
        customerOrdersRef = FirebaseDatabase.getInstance().getReference("CustomerOrders");

        stockEditRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                stockEditCount.setText(String.valueOf(count));
                stockEditCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        userRegisterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                userAuthCount.setText(String.valueOf(count));
                userAuthCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        customerOrdersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                        Boolean isProcessed = orderSnapshot.child("isProcessed").getValue(Boolean.class);
                        if (isProcessed != null && !isProcessed) {
                            count++;
                        }
                    }
                }
                orderRequestCount.setText(String.valueOf(count));
                orderRequestCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to fetch order requests.", Toast.LENGTH_SHORT).show();
            }
        });

        stockedittv.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), StockEditActivity.class);
            startActivity(intent);
        });

        userauthtv.setOnClickListener(view12 -> {
            Intent intent = new Intent(getActivity(), UserRequestActivity.class);
            startActivity(intent);
        });

        orderRequeststv.setOnClickListener(view13 -> {
            Intent intent = new Intent(getActivity(), OrderRequests.class);
            startActivity(intent);
        });

        allOrderstv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AllOrders.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
