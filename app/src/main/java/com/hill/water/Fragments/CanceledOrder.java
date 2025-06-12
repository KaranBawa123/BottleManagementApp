package com.hill.water.Fragments;

public class CanceledOrder {
    private int invoiceNumber;
    private String itemName;
    private String bottleType;
    private int quantity;
    private double rate;
    private String date;
    private double total;
    private String location;
    private String name;
    private String phoneNumber;
    private String cancelReason;
    private long timestamp;

    // Default constructor
    public CanceledOrder() {
    }

    public CanceledOrder(int invoiceNumber, String itemName, String bottleType, int quantity,
                         double rate, String date, double total, String location, String name,
                         String phoneNumber, String cancelReason, long timestamp) {
        this.invoiceNumber = invoiceNumber;
        this.itemName = itemName;
        this.bottleType = bottleType;
        this.quantity = quantity;
        this.rate = rate;
        this.date = date;
        this.total = total;
        this.location = location;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.cancelReason = cancelReason;
        this.timestamp = timestamp;
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(int invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getBottleType() {
        return bottleType;
    }

    public void setBottleType(String bottleType) {
        this.bottleType = bottleType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
