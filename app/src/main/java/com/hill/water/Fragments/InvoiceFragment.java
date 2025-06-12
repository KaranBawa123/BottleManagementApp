package com.hill.water.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.hill.water.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class InvoiceFragment extends Fragment {

    private ViewPager2 viewPager;
    private ImageSliderAdapter adapter;
    private List<String> imageUrls;
    private DatabaseReference databaseReference;
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final long AUTO_SCROLL_DELAY = 5000;
    private TextView downloadInvoice;
    private DatabaseReference invoiceRef;
    private TextView invoiceCountTextView;

    public InvoiceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        downloadInvoice = view.findViewById(R.id.downloadInvoice);

        invoiceCountTextView = view.findViewById(R.id.invoice_count);

        invoiceRef = FirebaseDatabase.getInstance().getReference().child("invoice");

        imageUrls = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("plantImages");

        adapter = new ImageSliderAdapter(getContext(), imageUrls, imageUrl -> {
            FullScreenImageDialogFragment dialog = FullScreenImageDialogFragment.newInstance(imageUrl);
            dialog.showNow(getParentFragmentManager(), "FullScreenDialog");
        });

        viewPager.setAdapter(adapter);
        downloadInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),InvoiceActivity.class);
                startActivity(intent);
            }
        });

        invoiceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long invoiceCount = snapshot.getChildrenCount();
                if (invoiceCount > 0) {
                    invoiceCountTextView.setText(String.valueOf(invoiceCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InvoiceFragment", "Error fetching invoice count: " + error.getMessage());
            }
        });

        loadImagesFromFirebase();
        setupAutoScroll();

        return view;
    }

    private void loadImagesFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageUrls.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String imageUrl = dataSnapshot.getValue(String.class);
                    imageUrls.add(imageUrl);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAutoScroll() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    int currentItem = viewPager.getCurrentItem();
                    int nextItem = (currentItem + 1) % imageUrls.size();
                    viewPager.setCurrentItem(nextItem, true);
                }
                handler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        };
        handler.postDelayed(runnable, AUTO_SCROLL_DELAY);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
    }
}
