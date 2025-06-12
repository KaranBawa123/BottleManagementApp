package com.hill.water.Fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hill.water.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

public class PaidInvoicesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PaidInvoicesAdapter adapter;
    private ArrayList<PaidInvoice> paidInvoicesList;
    private DatabaseReference paymentsRef;
    private Button exportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paid_invoices);

        recyclerView = findViewById(R.id.recyclerViewPaidInvoices);
        exportButton = findViewById(R.id.btn_export_excel);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);

        paidInvoicesList = new ArrayList<>();
        adapter = new PaidInvoicesAdapter(paidInvoicesList);
        recyclerView.setAdapter(adapter);

        paymentsRef = FirebaseDatabase.getInstance().getReference("payments");

        fetchPaidInvoices();

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateAndExportExcel2();
            }
        });
    }

    private void fetchPaidInvoices() {
        paymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paidInvoicesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PaidInvoice invoice = dataSnapshot.getValue(PaidInvoice.class);
                    paidInvoicesList.add(invoice);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaidInvoicesActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateAndExportExcel2() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Paid Dispatches");

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

        String[] headers = {"Dispatch No", "Name", "Phone Number", "Location", "Item Name",
                "Bottle Type", "Quantity", "Rate", "Total Cost", "Date", "Payment Method", "Payment Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        headerRow.setHeightInPoints(20);
        int rowHeight = 20;

        int rowNum = 1;
        for (PaidInvoice invoice : paidInvoicesList) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(rowHeight);

            row.createCell(0).setCellValue(invoice.getInvoiceNumber());
            row.createCell(1).setCellValue(invoice.getName());
            row.createCell(2).setCellValue(invoice.getPhoneNumber());
            row.createCell(3).setCellValue(invoice.getLocation());
            row.createCell(4).setCellValue(invoice.getItemName());
            row.createCell(5).setCellValue(invoice.getBottleType());
            row.createCell(6).setCellValue(invoice.getQuantity());
            row.createCell(7).setCellValue(invoice.getRate());
            row.createCell(8).setCellValue(invoice.getTotal());
            row.createCell(9).setCellValue(invoice.getDate());
            row.createCell(10).setCellValue(invoice.getPaymentMethod());
            row.createCell(11).setCellValue(invoice.getPaymentDate());

            for (int i = 0; i <= 11; i++) {
                row.getCell(i).setCellStyle(contentCellStyle);
            }
        }

        adjustColumnWidths2(sheet);

        Uri fileUri = saveToExcel2(workbook);

        if (fileUri != null) {
            openExcel2(fileUri);
        }
    }

    private void adjustColumnWidths2(Sheet sheet) {
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

    private Uri saveToExcel2(Workbook workbook) {
        String fileName = "PaidInvoices.xlsx";
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

    private void openExcel2(Uri fileUri) {
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
