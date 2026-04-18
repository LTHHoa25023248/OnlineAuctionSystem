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
        this.startTime = startTime;
        this.endTime = endTime;

        if (startingPrice < 0) {
            //Throwing exception for negative value of starting price
            throw new IllegalArgumentException("Starting price must greater than 0!");
        }
        //Valiadation when initialize a item. Current price is 
        //equal to starting price
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
    }

    //Setters and Getters
    public double getCurrentPrice() {return currentPrice;}
    public String getName() {return name;}
    public String getId() {return id;}
    public LocalDateTime getEndTime() {return endTime;}

    public void setName(String newName) {this.name = newName;}
    public void setDescription(String description) {this.description = description;}
    

    
}
