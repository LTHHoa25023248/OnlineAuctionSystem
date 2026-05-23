package com.example.auctionmanagementsystem.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.SQLException;

public class ItemTest {
  @Test
  public void testItemConstructor_ValidData_Success() {
    Item item = new Item("Laptop", "Core i7", 1000.0) {
      @Override
      public String getCategoryDetails() {
        return "";
      }

      @Override
      public void insertSubData(Connection conn, int itemId) throws SQLException {}

      @Override
      public void updateSubData(Connection conn) throws SQLException {}
    };

    assertEquals("Laptop", item.getName());
    assertEquals("Core i7", item.getDescription());
    assertEquals(1000.0, item.getStartingPrice());
  }

  @Test
  public void testItemConstructor_ZeroPrice_Success() {
    Item item = new Item("Móc khóa", "Đồ lưu niệm", 0.0) {
      @Override
      public String getCategoryDetails() {
        return "";
      }

      @Override
      public void insertSubData(Connection conn, int itemId) throws SQLException {}

      @Override
      public void updateSubData(Connection conn) throws SQLException {}
    };

    assertEquals(0.0, item.getStartingPrice());
  }

  // TC3: Khởi tạo với giá âm (BVA) - Ném ngoại lệ
  @Test
  public void testItemConstructor_NegativePrice_ThrowsException() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new Item("Điện thoại hỏng", "Không lên nguồn", -1.0) {
        @Override
        public String getCategoryDetails() {
          return "";
        }

        @Override
        public void insertSubData(Connection conn, int itemId) throws SQLException {}

        @Override
        public void updateSubData(Connection conn) throws SQLException {}
      };
    });

    assertEquals("Starting price must greater than 0!", exception.getMessage());
  }
}
