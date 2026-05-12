package com.example.auctionmanagementsystem.model;

public abstract class Item {
    private String id, name, description;
    private double startingPrice;

    //Default Constructor
    public Item() {}

    //Constructor with full parameters
    public Item(String id, String name, String description, double startingPrice) {
        this.id = id;
        this.name = name;
        this.description = description;

        if (startingPrice < 0) {
            //Throwing com.example.auctionmanagementsystem.exception for negative value of starting price
            throw new IllegalArgumentException("Starting price must greater than 0!");
        }
        //Valiadation when initialize a item. Current price is 
        //equal to starting price
        this.startingPrice = startingPrice;
    }

    //Setters and Getters
    public String getName() {return name;}
    public String getId() {return id;}
    public String getDescription() {return description;}
    public double getStartingPrice() {return startingPrice;}

    public void setName(String newName) {this.name = newName;}
    public void setDescription(String description) {this.description = description;}
    
    //Abstract method
    public abstract String getCategoryDetails();          
}
