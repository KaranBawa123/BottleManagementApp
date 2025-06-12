package com.hill.water.Fragments;

public class Order {

    public String orderId;
    public String uid;
    public String orderDate;
    public double quantity;
    public String name;
    public String shopName;
    public String shopAddress;
    public String phoneNumber;
    public String bottleType;
    public String itemName;
    public double totalAmount;
    public String orderStatus;
    public String dispatchStatus;
    public double rate;
    public boolean isProcessed;

    // Default constructor
    public Order() {
        this.isProcessed = false;
    }

    public Order(String orderId, String uid, String orderDate, double quantity, String name, String shopName,
                 String shopAddress, String phoneNumber, String bottleType, String itemName,
                 double totalAmount, double rate, String orderStatus, String dispatchStatus, boolean isProcessed) {
        this.orderId = orderId;
        this.uid = uid;
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
        this.orderStatus = orderStatus;
        this.dispatchStatus = dispatchStatus;
        this.isProcessed = isProcessed;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBottleType() {
        return bottleType;
    }

    public void setBottleType(String bottleType) {
        this.bottleType = bottleType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDispatchStatus() {
        return dispatchStatus;
    }

    public void setDispatchStatus(String dispatchStatus) {
        this.dispatchStatus = dispatchStatus;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }
}
