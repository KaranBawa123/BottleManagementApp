package com.hill.water.Fragments;

public class Invoice2 {
    public int invoiceNumber;
    public String date;
    public String itemName;
    public int quantity;
    public double rate;
    public double total;
    public String bottleType;
    public String location;
    public String phoneNumber;
    public String name;

    public Invoice2() {}

    public Invoice2(int invoiceNumber, String date, String itemName, int quantity, double rate, double total, String bottleType, String location, String phoneNumber, String name) {
        this.invoiceNumber = invoiceNumber;
        this.date = date;
        this.itemName = itemName;
        this.quantity = quantity;
        this.rate = rate;
        this.total = total;
        this.bottleType = bottleType;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.name = name;
    }
}
