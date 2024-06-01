package com.example.mabei_poa.Model;

import java.io.Serializable;
import java.util.Date;

public class DebtTrackerSummaryModel implements Serializable {
    private String customerId;
    private String customerName;
    private Date debtTrackerDate;
    private String debtTrackerType;
    private int debtTrackerAmount;

    public DebtTrackerSummaryModel() {
    }

    public DebtTrackerSummaryModel(String customerId, String customerName, Date debtTrackerDate, String debtTrackerType, int debtTrackerAmount) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.debtTrackerDate = debtTrackerDate;
        this.debtTrackerType = debtTrackerType;
        this.debtTrackerAmount = debtTrackerAmount;
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

    public Date getDebtTrackerDate() {
        return debtTrackerDate;
    }

    public void setDebtTrackerDate(Date debtTrackerDate) {
        this.debtTrackerDate = debtTrackerDate;
    }

    public String getDebtTrackerType() {
        return debtTrackerType;
    }

    public void setDebtTrackerType(String debtTrackerType) {
        this.debtTrackerType = debtTrackerType;
    }

    public int getDebtTrackerAmount() {
        return debtTrackerAmount;
    }

    public void setDebtTrackerAmount(int debtTrackerAmount) {
        this.debtTrackerAmount = debtTrackerAmount;
    }
}
