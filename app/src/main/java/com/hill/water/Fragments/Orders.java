package com.hill.water.Fragments;

public class Orders {
    private String orderDate;
    private double quantity;
    private String name;
    private String shopName;
    private String shopAddress;
    private String phoneNumber;
    private String bottleType;
    private String itemName;
    private double totalAmount;
    private double rate;
    private String dispatchStatus;

    public Orders() {

    }

    public Orders(String orderDate, double quantity, String name, String shopName,
                  String shopAddress, String phoneNumber, String bottleType, String itemName,
                  double totalAmount, double rate, String dispatchStatus) {
        this.orderDate = orderDate;
        this.quantity = quantity;
        this.name = name;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.phoneNumber = phoneNumber;
        this.bottleType = bottleType;
        this.itemName = itemName;
        this.totalAmount = totalAmount;
        this.rate = rate;
        this.dispatchStatus = dispatchStatus;
    }

    public String getOrderDate() { return orderDate; }
    public double getQuantity() { return quantity; }
    public String getName() { return name; }
    public String getShopName() { return shopName; }
    public String getShopAddress() { return shopAddress; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getBottleType() { return bottleType; }
    public String getItemName() { return itemName; }
    public double getTotalAmount() { return totalAmount; }
    public double getRate() { return rate; }
    public String getDispatchStatus() { return dispatchStatus; }
    public void setDispatchStatus(String dispatchStatus) { this.dispatchStatus = dispatchStatus; }
}
