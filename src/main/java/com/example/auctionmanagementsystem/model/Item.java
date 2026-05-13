package com.example.auctionmanagementsystem.model;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Item extends Entity{
    private String name, description;
    private double startingPrice;

    //Default Constructor
    public Item() {}

    //Constructor with full parameters
    public Item(String name, String description, double startingPrice) {
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

    public String getName() {return name;}
    public String getDescription() {return description;}
    public double getStartingPrice() {return startingPrice;}

    public void setName(String newName) {this.name = newName;}
    public void setDescription(String description) {this.description = description;}
    
    public abstract String getCategoryDetails();
    /**
     * Lớp con tự định nghĩa ở trong lớp cụ thể
     * @param conn
     * @param itemId Id của bảng items
     * @throws SQLException
     */
    public abstract void insertSubData(Connection conn, int itemId) throws SQLException;        
}
