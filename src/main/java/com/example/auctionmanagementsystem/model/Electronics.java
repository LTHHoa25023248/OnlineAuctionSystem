package com.example.auctionmanagementsystem.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    @Override
    public void insertSubData(Connection conn, int itemId) throws SQLException {
        String sql = "INSERT INTO electronics_items(item_id, year, mileage) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setString(2, this.brand);
            ps.setInt(3, this.warrantyMonths);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateSubData(Connection conn) throws SQLException {
        String sql = "UPDATE items SET brand=?, warrantyMonths =? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.brand);
            ps.setInt(2, this.warrantyMonths);
            ps.executeUpdate();
        }
    }
}

