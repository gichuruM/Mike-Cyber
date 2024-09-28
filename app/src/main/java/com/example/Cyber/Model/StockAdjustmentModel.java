package com.example.Cyber.Model;

public class StockAdjustmentModel {
    private String productId;
    private String productName;
    private double quantityOld;
    private double quantityAdded;
    private double newBuyingPrice;
    private double newSellingPrice;
    private long timestamp;

    public StockAdjustmentModel() {
        // Default constructor required for calls to DataSnapshot.getValue()
    }

    public StockAdjustmentModel(String productId, String productName, double quantityOld, double quantityAdded, double newBuyingPrice, double newSellingPrice, long timestamp) {
        this.productId = productId;
        this.productName = productName;
        this.quantityOld = quantityOld;
        this.quantityAdded = quantityAdded;
        this.newBuyingPrice = newBuyingPrice;
        this.newSellingPrice = newSellingPrice;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getQuantityOld() {
        return quantityOld;
    }

    public double getQuantityAdded() {
        return quantityAdded;
    }

    public double getNewBuyingPrice() {
        return newBuyingPrice;
    }

    public double getNewSellingPrice() {
        return newSellingPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }
}