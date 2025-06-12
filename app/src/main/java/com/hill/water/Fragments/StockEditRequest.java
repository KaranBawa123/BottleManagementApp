package com.hill.water.Fragments;

public class StockEditRequest {
    private String requestId;
    private String bottleType;
    private int requestedStock;
    private int currentStock;
    private String workerName;
    private String workerPhone;
    private String status;

    // Constructor
    public StockEditRequest(String requestId, String bottleType, int requestedStock, int currentStock,
                            String workerName, String workerPhone, String status) {
        this.requestId = requestId;
        this.bottleType = bottleType;
        this.requestedStock = requestedStock;
        this.currentStock = currentStock;
        this.workerName = workerName;
        this.workerPhone = workerPhone;
        this.status = status;
    }

    public StockEditRequest() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBottleType() {
        return bottleType;
    }

    public void setBottleType(String bottleType) {
        this.bottleType = bottleType;
    }

    public int getRequestedStock() {
        return requestedStock;
    }

    public void setRequestedStock(int requestedStock) {
        this.requestedStock = requestedStock;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerPhone() {
        return workerPhone;
    }

    public void setWorkerPhone(String workerPhone) {
        this.workerPhone = workerPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
