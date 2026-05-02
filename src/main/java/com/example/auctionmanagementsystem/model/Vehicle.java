package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private int year;
    private double mileage;

    public Vehicle(String id, String name, String description, double startingPrice, int year, double mileage) {
        super(id, name, description, startingPrice);
        this.year = year;
        this.mileage = mileage;
    }

    @Override 
    public String getCategoryDetails() {
        return String.format("Manufacture Year: %d | Mileage: %.1f km", year, mileage);
    }
}