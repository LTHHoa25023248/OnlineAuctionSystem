package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;

public abstract class Item {
    private String id, name, description;
    private double startingPrice, currentPrice;
    private LocalDateTime startTime, endTime;

    //Default Constructor
    public Item() {}

    //Constructor with full parameters
    public Item(String id, String name, String description, double startingPrice, double currentPrice, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    //Setters and Getters
    public double getCurrentPrice() {return currentPrice;}
    public String getName() {return name;}
    public String getId() {return id;}
    public LocalDateTime getEndTime() {return endTime;}

    public void setName(String newName) {this.name = newName;}
    public void setDescription(String description) {this.description = description;}
    public void setStartingPrice(double startingPrice) {
        //Throwing exception for negative value of starting price
        if (startingPrice < 0) {
            throw new IllegalArgumentException("Starting price must greater than 0!");
        }
        this.startingPrice = startingPrice;
    }

    
}
