package com.example.auctionmanagementsystem.model;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;
    
    public Electronics(String name, String description, double startingPrice, String brand, int warrantyMonths) {
        super(name, description, startingPrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public void setBrand(String newBrand) {this.brand = newBrand;}
    public void setWarrantyMonths(int newWarrantyMonths) {this.warrantyMonths = newWarrantyMonths;}

    public String getBrand() {return brand;}
    public int getWarrantyMonths() {return warrantyMonths;}

    @Override
    public String getCategoryDetails() {
        return String.format("Brand: %s | Warranty: %d", brand, warrantyMonths);
    }
}
