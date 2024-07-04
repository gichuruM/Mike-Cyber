package com.example.Cyber.Model;

public class ProductTransactionModel {

    private String productId;
    private Long timeInMillis;
    private double productNum;
    private double productRevenue;
    private double productProfit;
    private double productTotalNum;
    private double productTotalRevenue;
    private double productTotalProfit;

    public ProductTransactionModel(String productId, Long timeInMillis, double productNum, double productRevenue, double productProfit, double productTotalNum, double productTotalRevenue, double productTotalProfit) {
        this.productId = productId;
        this.timeInMillis = timeInMillis;
        this.productNum = productNum;
        this.productRevenue = productRevenue;
        this.productProfit = productProfit;
        this.productTotalNum = productTotalNum;
        this.productTotalRevenue = productTotalRevenue;
        this.productTotalProfit = productTotalProfit;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getProductTotalNum() {
        return productTotalNum;
    }

    public void setProductTotalNum(double productTotalNum) {
        this.productTotalNum = productTotalNum;
    }

    public double getProductTotalRevenue() {
        return productTotalRevenue;
    }

    public void setProductTotalRevenue(double productTotalRevenue) {
        this.productTotalRevenue = productTotalRevenue;
    }

    public double getProductTotalProfit() {
        return productTotalProfit;
    }

    public void setProductTotalProfit(double productTotalProfit) {
        this.productTotalProfit = productTotalProfit;
    }

    public Long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(Long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public double getProductNum() {
        return productNum;
    }

    public void setProductNum(double productNum) {
        this.productNum = productNum;
    }

    public double getProductRevenue() {
        return productRevenue;
    }

    public void setProductRevenue(double productRevenue) {
        this.productRevenue = productRevenue;
    }

    public double getProductProfit() {
        return productProfit;
    }

    public void setProductProfit(double productProfit) {
        this.productProfit = productProfit;
    }
}
