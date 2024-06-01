package com.example.mabei_poa.Model;

import java.io.Serializable;
import java.util.Date;

public class CustomerModel implements Serializable {

    private String customerId;
    private String customerName;
    private String phoneNumber;
    private int currentDebt;
    private int maxDebt;
    private Date initialDate;
    private Date editDate;
    private String debtProgress;
    private int proposedAmount;
    private String proposedAmountType;

    public CustomerModel() {
    }

    public CustomerModel(String customerId, String customerName, String phoneNumber, int currentDebt, int maxDebt, Date initialDate, Date editDate, String debtProgress, int proposedAmount, String proposedAmountType) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.currentDebt = currentDebt;
        this.maxDebt = maxDebt;
        this.initialDate = initialDate;
        this.editDate = editDate;
        this.debtProgress = debtProgress;
        this.proposedAmount = proposedAmount;
        this.proposedAmountType = proposedAmountType;
    }

    public CustomerModel(CustomerModel customer){
        this.customerId = customer.getCustomerId();
        this.customerName = customer.getCustomerName();
        this.phoneNumber = customer.getPhoneNumber();
        this.currentDebt = customer.getCurrentDebt();
        this.maxDebt = customer.getMaxDebt();
        this.initialDate = customer.getInitialDate();
        this.editDate = customer.getEditDate();
        this.debtProgress = customer.getDebtProgress();
        this.proposedAmount = customer.getProposedAmount();
        this.proposedAmountType = customer.getProposedAmountType();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getCurrentDebt() {
        return currentDebt;
    }

    public void setCurrentDebt(int currentDebt) {
        this.currentDebt = currentDebt;
    }

    public int getMaxDebt() {
        return maxDebt;
    }

    public void setMaxDebt(int maxDebt) {
        this.maxDebt = maxDebt;
    }

    public Date getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(Date initialDate) {
        this.initialDate = initialDate;
    }

    public Date getEditDate() {
        return editDate;
    }

    public void setEditDate(Date editDate) {
        this.editDate = editDate;
    }

    public String getDebtProgress() {
        return debtProgress;
    }

    public void setDebtProgress(String debtProgress) {
        this.debtProgress = debtProgress;
    }

    public int getProposedAmount() {
        return proposedAmount;
    }

    public void setProposedAmount(int proposedAmount) {
        this.proposedAmount = proposedAmount;
    }

    public String getProposedAmountType() {
        return proposedAmountType;
    }

    public void setProposedAmountType(String proposedAmountType) {
        this.proposedAmountType = proposedAmountType;
    }
}
