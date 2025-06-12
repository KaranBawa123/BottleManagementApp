package com.hill.water.Fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hill.water.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataFragment extends Fragment {

    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private DataAdapter dataAdapter;
    private List<DataItem> dataItemList;
    private ImageView downloadIcon;
    private Button startDateButton, endDateButton;
    private String startDate, endDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("previouscounts");
        dataItemList = new ArrayList<>();
    }

    private void fetchDataFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataItemList.clear();
                int totalBottles = 0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    long oneLitreCount = 0, twoFiftyMlCount = 0, fiveHundredMlCount = 0, fiveLitreCount = 0;;

                    if (dateSnapshot.hasChild("1L(12piece)")) {
                        oneLitreCount = dateSnapshot.child("1L(12piece)").getValue(Long.class);
                    }
                    if (dateSnapshot.hasChild("250ml(24piece)")) {
                        twoFiftyMlCount = dateSnapshot.child("250ml(24piece)").getValue(Long.class);
                    }
                    if (dateSnapshot.hasChild("500ml(24piece)")) {
                        fiveHundredMlCount = dateSnapshot.child("500ml(24piece)").getValue(Long.class);
                    }
                    if (dateSnapshot.hasChild("5L(1piece)")) {
                        fiveLitreCount = dateSnapshot.child("5L(1piece)").getValue(Long.class);
                    }

                    dataItemList.add(new DataItem(date, oneLitreCount, twoFiftyMlCount, fiveHundredMlCount, fiveLitreCount));

                    totalBottles += oneLitreCount + twoFiftyMlCount + fiveHundredMlCount + fiveLitreCount;

                }

                if (dataAdapter != null) {
                    dataAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);

        downloadIcon = view.findViewById(R.id.downloadIcon);
        startDateButton = view.findViewById(R.id.startDateButton);
        endDateButton = view.findViewById(R.id.endDateButton);

        dataAdapter = new DataAdapter(dataItemList);
        recyclerView.setAdapter(dataAdapter);

        fetchDataFromFirebase();

        startDateButton.setOnClickListener(v -> showDatePickerDialog(true));
        endDateButton.setOnClickListener(v -> showDatePickerDialog(false));
        downloadIcon.setOnClickListener(v -> exportDataToCSV());

        return view;
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year1, monthOfYear, dayOfMonth1) -> {
            String selectedDate = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth1);

            if (isStartDate) {
                startDate = selectedDate;
            } else {
                endDate = selectedDate;
            }

            if (startDate != null && endDate != null) {
                filterDataByDateRange(startDate, endDate);
            }
        }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    private void filterDataByDateRange(String startDate, String endDate) {
        List<DataItem> filteredList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            for (DataItem item : dataItemList) {
                Date itemDate = sdf.parse(item.getDate());
                if (itemDate != null && !itemDate.before(start) && !itemDate.after(end)) {
                    filteredList.add(item);
                }
            }

            Collections.sort(filteredList, (o1, o2) -> {
                try {
                    Date date1 = sdf.parse(o1.getDate());
                    Date date2 = sdf.parse(o2.getDate());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            });

            dataAdapter.updateData(filteredList);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void exportDataToCSV() {
        StringBuilder data = new StringBuilder();
        data.append("Date,1 Litre,250ml,500ml,5 Litre\n");

        for (DataItem item : dataItemList) {
            data.append(item.getDate()).append(",")
                    .append(item.getOneLitreCount()).append(",")
                    .append(item.getTwoFiftyMlCount()).append(",")
                    .append(item.getFiveHundredMlCount()).append(",")
                    .append(item.getFiveLitreCount()).append("\n");

        }

        try {
            File file = new File(getActivity().getFilesDir(), "bottle_data.csv");
            FileWriter writer = new FileWriter(file);
            writer.write(data.toString());
            writer.close();

            Uri uri = FileProvider.getUriForFile(getActivity(), "com.example.hillwater.fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bottle Production Data");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Export Data"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
