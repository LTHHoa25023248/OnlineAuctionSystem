package com.example.auctionmanagementsystem.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Vehicle extends Item {
  private int year;
  private double mileage;

  public Vehicle(String name, String description, double startingPrice, int year, double mileage) {
    super(name, description, startingPrice);
    this.year = year;
    this.mileage = mileage;
  }

  public void setYear(int newYear) {
    this.year = newYear;
  }

  public void setMileage(double newMileage) {
    this.mileage = newMileage;
  }

  public int getYear() {
    return year;
  }

  public double getMileage() {
    return mileage;
  }

  @Override
  public String getCategoryDetails() {
    return String.format("Manufacture Year: %d | Mileage: %.1f km", year, mileage);
  }

  @Override
  public void insertSubData(Connection conn, int itemId) throws SQLException {
    String sql = "INSERT INTO vehicle_items(item_id, year, mileage) VALUES (?,?,?)";
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setInt(1, itemId);
    ps.setInt(2, this.year);
    ps.setDouble(3, this.mileage);
    ps.executeUpdate();

  }

  @Override
  public void updateSubData(Connection conn) throws SQLException {
    String sql = "UPDATE items SET year=?, mileage =? WHERE id=?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, this.year);
      ps.setDouble(2, this.mileage);
      ps.executeUpdate();
    }
  }
}
