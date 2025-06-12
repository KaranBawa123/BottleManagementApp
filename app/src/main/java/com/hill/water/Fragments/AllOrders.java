package com.hill.water.Fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AllOrders extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private List<Orders> ordersList;
    private TextView orderSummaryTextView;
    private EditText searchPhoneNumberEditText;
    private ImageView downloadExcelimv;

    private static final long DELAY = 2500;
    private Handler handler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_orders);

        ordersRecyclerView = findViewById(R.id.recycler_view);
        ordersList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(ordersList);
        orderSummaryTextView = findViewById(R.id.orderSummaryTextView);
        downloadExcelimv = findViewById(R.id.downloadExcelimv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        ordersRecyclerView.setLayoutManager(layoutManager);
        ordersRecyclerView.setAdapter(ordersAdapter);

        searchPhoneNumberEditText = findViewById(R.id.searchPhoneNumberEditText);

        searchPhoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String phoneNumber = editable.toString();
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                if (!phoneNumber.isEmpty()) {

                    searchRunnable = new Runnable() {
                        @Override
                        public void run() {
                            downloadExcelimv.setVisibility(View.VISIBLE);
                            orderSummaryTextView.setVisibility(View.VISIBLE);
                            filterOrdersByPhoneNumber(phoneNumber);
                        }
                    };
                    handler.postDelayed(searchRunnable, DELAY);
                } else {

                    downloadExcelimv.setVisibility(View.GONE);
                    orderSummaryTextView.setVisibility(View.GONE);
                    fetchOrders();
                }
            }
        });

        fetchOrders();

        downloadExcelimv.setOnClickListener(v -> {
            if (!ordersList.isEmpty()) {
                generateAndExportExcel();
            } else {
                Toast.makeText(AllOrders.this, "No orders available to export", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchOrders() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("CustomerOrders");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        Orders order = orderSnapshot.getValue(Orders.class);
                        if (order != null) {
                            ordersList.add(order);
                        }
                    }
                }
                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AllOrders.this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrdersByPhoneNumber(String phoneNumber) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("CustomerOrders");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersList.clear();
                int orderCount = 0;
                double totalQuantity = 0;
                double totalAmount = 0;

                for (DataSnapshot uidSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot orderSnapshot : uidSnapshot.getChildren()) {
                        Orders order = orderSnapshot.getValue(Orders.class);
                        if (order != null && order.getPhoneNumber().equals(phoneNumber)) {
                            ordersList.add(order);
                            orderCount++;
                            totalQuantity += order.getQuantity();
                            totalAmount += order.getTotalAmount();
                        }
                    }
                }

                if (orderCount > 0) {

                    ordersAdapter.notifyDataSetChanged();

                    showOrderSummary(orderCount, totalQuantity, totalAmount);
                } else {

                    Toast.makeText(AllOrders.this, "No orders found for this phone number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AllOrders.this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOrderSummary(int orderCount, double totalQuantity, double totalAmount) {
        String summary = "Total Orders: " + orderCount + "\n" +
                "Total Quantity: " + totalQuantity + "\n" +
                "Total Amount: " + totalAmount;
        orderSummaryTextView.setText(summary);
    }

    private void generateAndExportExcel() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Filtered Orders");

        CellStyle headerCellStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle contentCellStyle = workbook.createCellStyle();
        contentCellStyle.setAlignment(HorizontalAlignment.CENTER);
        contentCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);

        String[] headers = {"Name", "Phone Number", "Shop Name", "Address", "Item Name",
                "Bottle Type", "Quantity", "Rate", "Total Amount", "Date"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        for (Orders order : ordersList) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(order.getName());
            row.createCell(1).setCellValue(order.getPhoneNumber());
            row.createCell(2).setCellValue(order.getShopName());
            row.createCell(3).setCellValue(order.getShopAddress());
            row.createCell(4).setCellValue(order.getItemName());
            row.createCell(5).setCellValue(order.getBottleType());
            row.createCell(6).setCellValue(order.getQuantity());
            row.createCell(7).setCellValue(order.getRate());
            row.createCell(8).setCellValue(order.getTotalAmount());
            row.createCell(9).setCellValue(order.getOrderDate());

            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(contentCellStyle);
            }
        }

        adjustColumnWidths(sheet);
        Uri fileUri = saveToExcel(workbook);

        if (fileUri != null) {
            openExcel(fileUri);
        }
    }

    private void adjustColumnWidths(Sheet sheet) {
        for (int colNum = 0; colNum < sheet.getRow(0).getPhysicalNumberOfCells(); colNum++) {
            int maxLength = 0;
            for (int rowNum = 0; rowNum < sheet.getPhysicalNumberOfRows(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row != null) {
                    Cell cell = row.getCell(colNum);
                    if (cell != null) {
                        String cellValue = cell.toString();
                        maxLength = Math.max(maxLength, cellValue.length());
                    }
                }
            }
            sheet.setColumnWidth(colNum, (maxLength + 2) * 256);
        }
    }
    private Uri saveToExcel(Workbook workbook) {
        String fileName = "FilteredOrders.xlsx";
        Uri fileUri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            fileUri = getContentResolver().insert(collection, values);

            if (fileUri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(fileUri)) {
                    workbook.write(out);
                    Toast.makeText(this, "Excel file saved", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Error generating Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            File directory = getExternalFilesDir(null);
            File file = new File(directory, fileName);
            try (OutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
                fileUri = Uri.fromFile(file);
                Toast.makeText(this, "Excel file saved to Downloads", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error generating Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileUri;
    }
    private void openExcel(Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(intent, "Open Excel File");
        try {
            startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open Excel file", Toast.LENGTH_SHORT).show();
        }
    }

}
