package com.example.auctionmanagementsystem.model;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Item extends Entity {
  private String name, description;
  private double startingPrice;

  public Item() {}

  // Hàm khởi tạo với các tham số đầy đủ, tất cả các Item đều yêu cầu những tham số này
  // bất kể loại Item nào
  public Item(String name, String description, double startingPrice) {
    this.name = name;
    this.description = description;

    if (startingPrice < 0) {
      // Ném ngoại lệ khi giá khởi điểm âm
      throw new IllegalArgumentException("Starting price must greater than 0!");
    }
    this.startingPrice = startingPrice;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public double getStartingPrice() {
    return startingPrice;
  }

  public void setName(String newName) {
    this.name = newName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public abstract String getCategoryDetails();

  /**
   * Truyền attributes của các lớp con, được định nghĩa trong từng lớp con cụ thể
   * 
   * @param conn
   * @param itemId Id của bảng items
   * @throws SQLException
   */
  public abstract void insertSubData(Connection conn, int itemId) throws SQLException;

  /**
   * Cập nhật các thông tin của từng loại hàng, được định nghĩa trong từng lớp con cụ thể
   * 
   * @param conn
   */
  public abstract void updateSubData(Connection conn) throws SQLException;
}
