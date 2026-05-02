package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;
    
    public Electronics(String id, String name, String description, double startingPrice, String brand, int warrantyMonths) {
        super(id, name, description, startingPrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getCategoryDetails() {
        return String.format("Brand: %s | Warranty: %d", brand, warrantyMonths);
    }
}
