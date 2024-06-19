package com.example.Cyber.Model;

import java.io.Serializable;

public class CartModel implements Serializable {

    private String productId;
    private double quantity;
    private double productTotal;

    public CartModel() {
    }

    public CartModel(String productId, double quantity, double productTotal) {
        this.productId = productId;
        this.quantity = quantity;
        this.productTotal = productTotal;
    }

    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public double getProductTotal() {
        return productTotal;
    }
    public void setProductTotal(double productTotal) {
        this.productTotal = productTotal;
    }
    public double getQuantity() {
        return quantity;
    }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
