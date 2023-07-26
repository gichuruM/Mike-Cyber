package com.example.mabei_poa.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class TransactionModel implements Serializable {

    private String transactionId;
    private Date time;
    private ArrayList<CartModel> cartModelArrayList;
    private double totalAmount;
    private double receivedAmount;
    private double changeAmount;
    private String paymentMethod;
    private String note;
    private double profit;

    public TransactionModel() {
    }

    public TransactionModel(String transactionId, Date time, ArrayList<CartModel> cartModelArrayList, double totalAmount, double receivedAmount, double changeAmount, String paymentMethod, String note, double profit) {
        this.transactionId = transactionId;
        this.time = time;
        this.cartModelArrayList = cartModelArrayList;
        this.totalAmount = totalAmount;
        this.receivedAmount = receivedAmount;
        this.changeAmount = changeAmount;
        this.paymentMethod = paymentMethod;
        this.note = note;
        this.profit = profit;
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

    public ArrayList<CartModel> getCartModelArrayList() {
        return cartModelArrayList;
    }

    public void setCartModelArrayList(ArrayList<CartModel> cartModelArrayList) {
        this.cartModelArrayList = cartModelArrayList;
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
}
