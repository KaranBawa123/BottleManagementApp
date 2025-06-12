package com.hill.water.Fragments;

public class PaidInvoice {
    private int invoiceNumber;
    private String date;
    private String itemName;
    private int quantity;
    private double rate;
    private double total;
    private String bottleType;
    private String location;
    private boolean paid;
    private String paymentMethod;
    private String name;
    private String phoneNumber;
    private String paymentDate;

    // Default constructor
    public PaidInvoice() {}

    public PaidInvoice(int invoiceNumber, String date, String itemName, int quantity,
                       double rate, double total, String bottleType, String location,
                       boolean paid, String paymentMethod, String name, String phoneNumber,
                       String paymentDate) {
        this.invoiceNumber = invoiceNumber;
        this.date = date;
        this.itemName = itemName;
        this.quantity = quantity;
        this.rate = rate;
        this.total = total;
        this.bottleType = bottleType;
        this.location = location;
        this.paid = paid;
        this.paymentMethod = paymentMethod;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.paymentDate = paymentDate;
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(int invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getBottleType() {
        return bottleType;
    }

    public void setBottleType(String bottleType) {
        this.bottleType = bottleType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
}
