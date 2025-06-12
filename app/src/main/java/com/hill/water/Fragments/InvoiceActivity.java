package com.hill.water.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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
import java.util.List;

public class InvoiceActivity extends AppCompatActivity {

    private RecyclerView invoiceRecyclerView;
    private InvoiceAdapter invoiceAdapter;
    private List<Invoice> invoiceList;
    private List<Invoice> allInvoices;
    private ImageView searchImageView,exportImageView;
    private double totalDues;

    private TextView tvTotalDues;

    private boolean isSearchActive = false;

    private Spinner invoiceFilterSpinner;
    private ArrayList<PaidInvoice> paidInvoicesList;
    private DatabaseReference paymentsRef;

    private List<Invoice> filteredSearchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        invoiceRecyclerView = findViewById(R.id.recyclerViewInvoices);
        searchImageView = findViewById(R.id.searchImageView);
        tvTotalDues = findViewById(R.id.tvTotalDues);
        exportImageView = findViewById(R.id.exportImageView);

        invoiceList = new ArrayList<>();
        allInvoices = new ArrayList<>();
        paidInvoicesList = new ArrayList<>();

        paymentsRef = FirebaseDatabase.getInstance().getReference("payments");
        paymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paidInvoicesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PaidInvoice invoice = dataSnapshot.getValue(PaidInvoice.class);
                    paidInvoicesList.add(invoice);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InvoiceActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });

        invoiceFilterSpinner = findViewById(R.id.invoiceFilterSpinner);
        LinearLayoutManager layoutManager = new LinearLayoutManager(InvoiceActivity.this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        invoiceRecyclerView.setLayoutManager(layoutManager);
        invoiceAdapter = new InvoiceAdapter(InvoiceActivity.this, invoiceList, new InvoiceAdapter.OnInvoiceClickListener() {
            @Override
            public void onInvoiceClick(Invoice invoice) {
                createPdf(invoice);
            }
        },totalDues);
        invoiceRecyclerView.setAdapter(invoiceAdapter);

        DatabaseReference stockRef = FirebaseDatabase.getInstance().getReference("totaldues");
        stockRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Double totalDues = dataSnapshot.getValue(Double.class);
                if (totalDues != null) {
                    tvTotalDues.setText("Total Dues: " + totalDues);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        DatabaseReference invoiceRef = FirebaseDatabase.getInstance().getReference("invoices");
        invoiceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allInvoices.clear();
                invoiceList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Invoice invoice = snapshot.getValue(Invoice.class);
                    if (invoice != null) {
                        allInvoices.add(invoice);
                        if (!invoice.isPaid()) {
                            invoiceList.add(invoice);
                        }
                    }
                }
                invoiceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(InvoiceActivity.this, "Failed to load invoices", Toast.LENGTH_SHORT).show();
            }
        });

        searchImageView.setOnClickListener(v -> {
            if (isSearchActive) {
                resetSearch();
            } else {
                showSearchDialog();
            }
        });
        exportImageView.setOnClickListener(v -> generateAndExportSearchResultsExcel());
        setupInvoiceFilterSpinner();
    }

    private void generateAndExportSearchResultsExcel() {
        if (filteredSearchResults.isEmpty()) {
            Toast.makeText(this, "No search results to export", Toast.LENGTH_SHORT).show();
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Filtered Invoices");

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

        String[] headers = {"Invoice No", "Name", "Phone Number", "Item Name", "Bottle Type", "Quantity", "Rate", "Amount", "Date", "Location"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        for (Invoice invoice : filteredSearchResults) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(invoice.getInvoiceNumber());
            row.createCell(1).setCellValue(invoice.getName());
            row.createCell(2).setCellValue(invoice.getPhoneNumber());
            row.createCell(3).setCellValue(invoice.getItemName());
            row.createCell(4).setCellValue(invoice.getBottleType());
            row.createCell(5).setCellValue(invoice.getQuantity());
            row.createCell(6).setCellValue(invoice.getRate());
            row.createCell(7).setCellValue(invoice.getTotal());
            row.createCell(8).setCellValue(invoice.getDate());
            row.createCell(9).setCellValue(invoice.getLocation());

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

    private void setupInvoiceFilterSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.invoice_filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        invoiceFilterSpinner.setAdapter(adapter);

        invoiceFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();

                if (selectedOption.equals("Select")) {
                }

                else if (selectedOption.equals("Unpaid Dispatches")) {
                    generateAndExportUnpaidExcel();
                }

                else if (selectedOption.equals("Paid Dispatches")) {
                    generateAndExportExcel2();
                }

                else if (selectedOption.equals("Cancelled Orders")) {

                    Intent intent = new Intent(InvoiceActivity.this,CancelledOrders.class);
                    startActivity(intent);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search by Phone Number");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String query = input.getText().toString();
            searchInvoices(query);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void searchInvoices(String query) {
        List<Invoice> filteredInvoices = new ArrayList<>();
        for (Invoice invoice : allInvoices) {
            if (!invoice.isPaid() && invoice.getPhoneNumber() != null &&
                    invoice.getPhoneNumber().contains(query)) {
                filteredInvoices.add(invoice);
            }
        }

        invoiceAdapter.updateInvoiceList(filteredInvoices);
        filteredSearchResults.clear();
        filteredSearchResults.addAll(filteredInvoices);

        if (!filteredInvoices.isEmpty()) {
            exportImageView.setVisibility(View.VISIBLE);
        } else {
            exportImageView.setVisibility(View.GONE);
        }

        isSearchActive = true;
    }

    private void resetSearch() {
        invoiceAdapter.updateInvoiceList(invoiceList);
        filteredSearchResults.clear();
        exportImageView.setVisibility(View.GONE);
        isSearchActive = false;
    }
    private void createPdf(Invoice invoice) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        int canvasWidth = canvas.getWidth();

        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hillwaterlogo);
        int logoMaxWidth = canvasWidth;
        int logoMaxHeight = 400;
        Bitmap scaledLogoBitmap = getScaledBitmap(logoBitmap, logoMaxWidth, logoMaxHeight);

        int logoX = (canvasWidth - scaledLogoBitmap.getWidth()) / 2;
        int logoY = 20;
        canvas.drawBitmap(scaledLogoBitmap, logoX, logoY, null);

        int y = logoY + scaledLogoBitmap.getHeight() + 20;

        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        String enterpriseText = "RJ Enterprises";
        float textWidth = paint.measureText(enterpriseText);
        canvas.drawText(enterpriseText, (canvasWidth - textWidth) / 2, y, paint);
        y += 20;

        paint.setTextSize(10);
        paint.setFakeBoldText(true);
        String addressText = "Address: PLOT NO.158,160 & 161,KHATA NO.10 AND 21 KHANDA BACHHELI KATUSYUN PAURI GARHWAL 246001, UTTARAKHAND";
        int addressMaxWidth = canvasWidth - 40;

        int textX = 20;
        y = wrapTextCentered(canvas, paint, addressText, textX, y, addressMaxWidth, canvasWidth);
        y += 15;

        paint.setTextSize(12);
        paint.setFakeBoldText(true);

        int x = 10;
        canvas.drawText("Dispatch Number:", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getInvoiceNumber() + "", x + 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Name:", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getName(), x + 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Phone Number:", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getPhoneNumber(), x + 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Dispatch Date:", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getDate(), x + 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Item Name:", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getItemName(), x + 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Quantity:", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getQuantity() + "", x + 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Rate:", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getRate() + "", x + 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Bottle Type:", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getBottleType(), x + 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Location:", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getLocation(), x + 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Total Cost: ", x, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(invoice.getTotal() + "", x + 120, y, paint);
        y += 10;

        Bitmap qrBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qr);
        int qrMaxWidth = canvasWidth - 40;
        int qrMaxHeight = 100;
        Bitmap scaledQrBitmap = getScaledBitmap(qrBitmap, qrMaxWidth, qrMaxHeight);

        int qrX = (canvasWidth - scaledQrBitmap.getWidth()) / 2;
        canvas.drawBitmap(scaledQrBitmap, qrX, y, null);
        y += scaledQrBitmap.getHeight() + 20;

        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        String titleText = "Scan and Pay here";
        float titleTextWidth = paint.measureText(titleText);
        canvas.drawText(titleText, (canvasWidth - titleTextWidth) / 2, y, paint);

        pdfDocument.finishPage(page);

        Uri fileUri = savePdfUsingMediaStore(pdfDocument, invoice);
        if (fileUri != null) {
            openPdf(fileUri);
        }
    }
    private int wrapTextCentered(Canvas canvas, Paint paint, String text, int x, int y, int maxWidth, int canvasWidth) {
        int lineHeight = (int) (paint.getTextSize() + 5);
        int textX;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (paint.measureText(line + word) > maxWidth) {
                textX = (int) ((canvasWidth - paint.measureText(line.toString().trim())) / 2);
                canvas.drawText(line.toString().trim(), textX, y, paint);
                y += lineHeight;
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }

        if (line.length() > 0) {
            textX = (int) ((canvasWidth - paint.measureText(line.toString().trim())) / 2);
            canvas.drawText(line.toString().trim(), textX, y, paint);
            y += lineHeight;
        }

        return y;
    }

    private Bitmap getScaledBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        float scaleWidth = ((float) maxWidth) / originalWidth;
        float scaleHeight = ((float) maxHeight) / originalHeight;

        float scaleFactor = Math.min(scaleWidth, scaleHeight);

        int newWidth = Math.round(originalWidth * scaleFactor);
        int newHeight = Math.round(originalHeight * scaleFactor);

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
    }

    private void generateAndExportUnpaidExcel() {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Unpaid Dispatches");

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
                "Bottle Type", "Quantity", "Rate", "Total Cost", "Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        headerRow.setHeightInPoints(20);
        int rowHeight = 20;

        int rowNum = 1;
        double totalCostSum = 0;
        for (Invoice invoice : invoiceList) {
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

            totalCostSum += invoice.getTotal();

            for (int i = 0; i <= 9; i++) {
                row.getCell(i).setCellStyle(contentCellStyle);
            }
        }

        Row totalRow = sheet.createRow(rowNum);
        totalRow.createCell(7).setCellValue("Total Cost Sum:");
        totalRow.createCell(8).setCellValue(totalCostSum);
        totalRow.getCell(7).setCellStyle(contentCellStyle);
        totalRow.getCell(8).setCellStyle(contentCellStyle);

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
        String fileName = "UnpaidDispatches.xlsx";
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

    private Uri savePdfUsingMediaStore(PdfDocument pdfDocument, Invoice invoice) {

        String fileName;
        if (invoice != null) {
            fileName = "Dispatch_" + invoice.getInvoiceNumber() + ".pdf";
        } else {
            fileName = "All_Dispatches.pdf";
        }

        Uri fileUri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");

            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            fileUri = getContentResolver().insert(collection, values);

            if (fileUri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(fileUri)) {
                    pdfDocument.writeTo(out);
                    Toast.makeText(this, "PDF saved: " + fileName, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(directory, fileName);
            try (OutputStream out = new FileOutputStream(file)) {
                pdfDocument.writeTo(out);
                fileUri = Uri.fromFile(file);
                Toast.makeText(this, "PDF saved to Downloads folder: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        pdfDocument.close();
        return fileUri;
    }

    private void openPdf(Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(intent, "Open Dispatch PDF");
        try {
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found to open PDF", Toast.LENGTH_SHORT).show();
        }
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

        double totalCostSum = 0.0;

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

            totalCostSum += invoice.getTotal();

            for (int i = 0; i <= 11; i++) {
                row.getCell(i).setCellStyle(contentCellStyle);
            }
        }

        Row sumRow = sheet.createRow(rowNum);
        sumRow.createCell(7).setCellValue("Total Cost Sum:");
        sumRow.createCell(8).setCellValue(totalCostSum);
        sumRow.getCell(7).setCellStyle(contentCellStyle);
        sumRow.getCell(8).setCellStyle(contentCellStyle);

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
