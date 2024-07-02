package com.example.Cyber.Model;

public class ProductTransactionModel {

    private Long timeInMillis;
    private double productNum;
    private double productRevenue;
    private double productProfit;

    public ProductTransactionModel(Long timeInMillis, double productNum, double productRevenue, double productProfit) {
        this.timeInMillis = timeInMillis;
        this.productNum = productNum;
        this.productRevenue = productRevenue;
        this.productProfit = productProfit;
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
