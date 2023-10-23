package com.example.mabei_poa.Model;

import java.io.Serializable;
import java.util.Map;

public class ProductModel implements Serializable {

    private String id;
    private String name;
    private String image;
    private String category;
    private double purchasePrice;
    private double sellingPrice;
    private double quantity;
    private double lowStockAlert;
    private String units;
    private Map<String, Double> barcodes;

    public ProductModel() {
    }

    public ProductModel(String id, String name, String image, String category, double purchasePrice, double sellingPrice, double quantity, double lowStockAlert, String units, Map<String, Double> barcodes) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.lowStockAlert = lowStockAlert;
        this.units = units;
        this.barcodes = barcodes;
    }

    public double getLowStockAlert() {
        return lowStockAlert;
    }

    public void setLowStockAlert(double lowStockAlert) {
        this.lowStockAlert = lowStockAlert;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Map<String, Double> getBarcodes() {
        return barcodes;
    }

    public void setBarcodes(Map<String, Double> barcodes) {
        this.barcodes = barcodes;
    }
}
