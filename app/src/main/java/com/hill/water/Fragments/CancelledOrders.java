package com.hill.water.Fragments;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hill.water.R;

import java.util.ArrayList;
import java.util.List;

public class CancelledOrders extends AppCompatActivity {

    private RecyclerView cancelledOrdersRV;
    private CanceledOrderAdapter adapter;
    private List<CanceledOrder> canceledOrdersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cancelled_orders);

        cancelledOrdersRV = findViewById(R.id.cancelledOrdersRV);
        canceledOrdersList = new ArrayList<>();
        adapter = new CanceledOrderAdapter(canceledOrdersList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        cancelledOrdersRV.setLayoutManager(layoutManager);
        cancelledOrdersRV.setAdapter(adapter);


        loadCancelledOrders();
    }

    private void loadCancelledOrders() {
        FirebaseDatabase.getInstance().getReference("CancelledOrders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        canceledOrdersList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            CanceledOrder canceledOrder = snapshot.getValue(CanceledOrder.class);
                            canceledOrdersList.add(canceledOrder);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("CancelledOrders", "Failed to load canceled orders", databaseError.toException());
                    }
                });
    }
}