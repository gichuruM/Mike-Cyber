package com.example.mabei_poa.Model;

import java.io.Serializable;

public class CartModel implements Serializable {

    private ProductModel productModel;
    private double quantity;
    private double amount;
    private double productTotal;

    public CartModel() {
    }

    public CartModel(ProductModel productModel, double quantity, double amount, double productTotal) {
        this.productModel = productModel;
        this.quantity = quantity;
        this.amount = amount;
        this.productTotal = productTotal;
    }

    public double getProductTotal() {
        return productTotal;
    }
    public void setProductTotal(double productTotal) {
        this.productTotal = productTotal;
    }
    public ProductModel getProductModel() {
        return productModel;
    }
    public void setProductModel(ProductModel productModel) {
        this.productModel = productModel;
    }
    public double getQuantity() {
        return quantity;
    }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
