package com.hill.water.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hill.water.R;

import java.util.ArrayList;
import java.util.List;

public class AgentLocationFragment extends Fragment {

    private RecyclerView recyclerView;
    private AgentLocationAdapter adapter;
    private List<AgentLocationSummary> locationList;
    private DatabaseReference agentLocationRef;
    private LottieAnimationView emptyAnimation;
    private TextView emptyStateText;

    public AgentLocationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_agent_location, container, false);

        emptyAnimation = rootView.findViewById(R.id.emptyAnimation);
        emptyStateText = rootView.findViewById(R.id.emptyStateText);

        recyclerView = rootView.findViewById(R.id.recyclerViewAgentLocations);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        locationList = new ArrayList<>();
        adapter = new AgentLocationAdapter(locationList, this::openMapActivity);
        recyclerView.setAdapter(adapter);

        agentLocationRef = FirebaseDatabase.getInstance().getReference("AgentLocations");

        fetchAllAgentsLocationData();

        return rootView;
    }

    private void fetchAllAgentsLocationData() {
        agentLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationList.clear();

                for (DataSnapshot agentSnapshot : dataSnapshot.getChildren()) {
                    String agentUID = agentSnapshot.getKey();

                    for (DataSnapshot dateSnapshot : agentSnapshot.getChildren()) {
                        String dateKey = dateSnapshot.getKey();
                        List<LocationData> locationsForDate = new ArrayList<>();
                        String agentName = "Unknown Name";
                        String agentPhone = "Unknown Phone";

                        for (DataSnapshot locationSnapshot : dateSnapshot.getChildren()) {
                            AgentLocation agentLocation = locationSnapshot.getValue(AgentLocation.class);
                            if (agentLocation != null) {
                                LocationData locationData = new LocationData(
                                        agentLocation.getAgentUid(),
                                        agentLocation.getLatitude(),
                                        agentLocation.getLongitude(),
                                        agentLocation.getDate(),
                                        agentLocation.getAgentName(),
                                        agentLocation.getAgentPhone(),
                                        agentLocation.getPlaceName()
                                );

                                locationsForDate.add(locationData);

                                if (agentName.equals("Unknown Name") && agentLocation.getAgentName() != null) {
                                    agentName = agentLocation.getAgentName();
                                }
                                if (agentPhone.equals("Unknown Phone") && agentLocation.getAgentPhone() != null) {
                                    agentPhone = agentLocation.getAgentPhone();
                                }
                            }
                        }

                        if (!locationsForDate.isEmpty()) {
                            AgentLocationSummary agentSummary = new AgentLocationSummary(
                                    agentUID, agentName, agentPhone, locationsForDate.size(), dateKey, locationsForDate
                            );
                            locationList.add(agentSummary);
                        }
                    }
                }

                if (locationList.isEmpty()) {
                    emptyAnimation.setVisibility(View.VISIBLE);
                    emptyStateText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyAnimation.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMapActivity(List<LocationData> locationsForAgent) {
        for (LocationData location : locationsForAgent) {
            Log.d("AgentLocationMapActivity", "Location: " + location.getPlaceName() +
                    " at LatLng: (" + location.getLatitude() + ", " + location.getLongitude() + ")");
        }

        Intent intent = new Intent(getContext(), AgentLocationMapActivity.class);
        intent.putParcelableArrayListExtra("locations", new ArrayList<>(locationsForAgent));
        Log.d("AgentLocationMapActivity", "Locations passed: " + locationsForAgent.size());
        startActivity(intent);
    }
}
