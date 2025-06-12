package com.hill.water.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hill.water.R;

import java.util.ArrayList;
import java.util.List;

public class OrderRequests extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseDatabase database;
    private DatabaseReference customerOrdersRef;
    private LottieAnimationView emptyAnimation;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_requests);

        recyclerView = findViewById(R.id.recycler_view);
        emptyAnimation = findViewById(R.id.emptyAnimation);
        emptyStateText = findViewById(R.id.emptyStateText);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(orderAdapter);

        database = FirebaseDatabase.getInstance();
        customerOrdersRef = database.getReference("CustomerOrders");

        fetchOrdersFromFirebase();
    }

    private void fetchOrdersFromFirebase() {
        customerOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot customerSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot orderSnapshot : customerSnapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null && !order.getIsProcessed()) {
                            orderList.add(order);
                        }
                    }
                }

                if (orderList.isEmpty()) {
                    emptyAnimation.setVisibility(View.VISIBLE);
                    emptyStateText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyAnimation.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderRequests.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
