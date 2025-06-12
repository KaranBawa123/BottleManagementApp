package com.hill.water.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;  // Import TextView

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
import java.util.List;

public class UserRequestActivity extends AppCompatActivity {

    private RecyclerView rvUserRequests;
    private UserRequestAdapter userRequestAdapter;
    private DatabaseReference userRequestsRef;
    private LottieAnimationView emptyAnimation;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_request);

        rvUserRequests = findViewById(R.id.user_register_requests);
        emptyAnimation = findViewById(R.id.emptyAnimation);
        emptyStateText = findViewById(R.id.emptyStateText);

        rvUserRequests.setLayoutManager(new LinearLayoutManager(this));

        userRequestsRef = FirebaseDatabase.getInstance().getReference("user_register_requests");

        userRequestAdapter = new UserRequestAdapter(userRequestsRef);
        rvUserRequests.setAdapter(userRequestAdapter);

        fetchUserRequests();
    }

    private void fetchUserRequests() {
        userRequestsRef.orderByChild("status").equalTo("pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<UserRegistrationRequest> requests = new ArrayList<>();
                        for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                            UserRegistrationRequest request = requestSnapshot.getValue(UserRegistrationRequest.class);
                            if (request != null) {
                                request.setUid(requestSnapshot.getKey());
                                requests.add(request);
                            }
                        }
                        userRequestAdapter.setUserRequests(requests);

                        if (requests.isEmpty()) {
                            emptyAnimation.setVisibility(View.VISIBLE);
                            emptyStateText.setVisibility(View.VISIBLE);
                            rvUserRequests.setVisibility(View.GONE);
                        } else {
                            emptyAnimation.setVisibility(View.GONE);
                            emptyStateText.setVisibility(View.GONE);
                            rvUserRequests.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserRequestActivity.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
