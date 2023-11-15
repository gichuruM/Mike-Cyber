package com.example.mabei_poa.Model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class TransactionModel2 implements Serializable {

    private String transactionId;
    private Date time;
    private Map<String, Double> cartDetails;
    private double totalAmount;
    private double receivedAmount;
    private double changeAmount;
    private String paymentMethod;
    private String note;
    private double profit;
    private String transactionType;
    private double waterlessProfit;

    public TransactionModel2() {
    }

    public TransactionModel2(String transactionId, Date time, Map<String, Double> cartDetails, double totalAmount, double receivedAmount, double changeAmount, String paymentMethod, String note, double profit, String transactionType, double waterlessProfit) {
        this.transactionId = transactionId;
        this.time = time;
        this.cartDetails = cartDetails;
        this.totalAmount = totalAmount;
        this.receivedAmount = receivedAmount;
        this.changeAmount = changeAmount;
        this.paymentMethod = paymentMethod;
        this.note = note;
        this.profit = profit;
        this.transactionType = transactionType;
        this.waterlessProfit = waterlessProfit;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Map<String, Double> getCartDetails() {
        return cartDetails;
    }

    public void setCartDetails(Map<String, Double> cartDetails) {
        this.cartDetails = cartDetails;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(double receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(double changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public double getWaterlessProfit() {
        return waterlessProfit;
    }

    public void setWaterlessProfit(double waterlessProfit) {
        this.waterlessProfit = waterlessProfit;
    }
}
