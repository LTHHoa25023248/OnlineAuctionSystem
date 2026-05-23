package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ItemDAO implements DAOInterface<Item> {

  
  // Implements DAOInterface. Dùng Connection từ bên ngoài truyền vào
 

  @Override
  public int insert(Item item, Connection connect) {
    String sqlItem = "INSERT INTO items(name, description, starting_price, item_type) VALUES(?,?,?,?)";
    try (PreparedStatement ps = connect.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, item.getName());
      ps.setString(2, item.getDescription());
      ps.setDouble(3, item.getStartingPrice());
      ps.setString(4, item.getClass().getSimpleName().toUpperCase());

      ps.executeUpdate();

      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          int itemId = rs.getInt(1);
          item.setId(itemId);
          item.insertSubData(connect, itemId);
          return itemId;
        }
      }
      throw new SQLException("Cannot get generated item ID");
    } catch (SQLException e) {
      throw new RuntimeException("Insert Item SQL failed", e);
    }
  }

  @Override
  public int update(Item item, Connection connect) {
    String sql = "UPDATE items SET name=?, description=? WHERE id=?";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setString(1, item.getName());
      ps.setString(2, item.getDescription());
      ps.setInt(3, item.getId());

      int affectedRows = ps.executeUpdate();
      item.updateSubData(connect);
      return affectedRows;
    } catch (SQLException e) {
      throw new RuntimeException("Update Item SQL failed", e);
    }
  }

  @Override
  public int delete(int id, Connection connect) {
    String sql = "DELETE FROM items WHERE id=?";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Delete Item SQL failed", e);
    }
  }

  @Override
  public Item selectById(int id, Connection connect) {
    String sql = "SELECT * FROM items WHERE id=?";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;

        String type = rs.getString("item_type");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double starting_price = rs.getDouble("starting_price");

        Map<String, String> attributes = new HashMap<>();
        String subTableSql = switch (type) {
          case "VEHICLE"     -> "SELECT * FROM vehicle_items WHERE item_id=?";
          case "ART"         -> "SELECT * FROM art_items WHERE item_id=?";
          case "ELECTRONICS" -> "SELECT * FROM electronics_items where item_id=?";
          default            -> null;
        };

        if (subTableSql != null) {
          try (PreparedStatement psSub = connect.prepareStatement(subTableSql)) {
            psSub.setInt(1, id);
            try (ResultSet rsSub = psSub.executeQuery()) {
              if (rsSub.next()) {
                if ("VEHICLE".equals(type)) {
                  attributes.put("year", String.valueOf(rsSub.getInt("year")));
                  attributes.put("mileage", String.valueOf(rsSub.getDouble("mileage")));
                } else if ("ART".equals(type)) {
                  attributes.put("artist", rsSub.getString("artist"));
                  attributes.put("theme", rsSub.getString("theme"));
                  attributes.put("material", rsSub.getString("material"));
                } else if ("ELECTRONICS".equals(type)) {
                  attributes.put("brand", rsSub.getString("brand"));
                  attributes.put("warranty", String.valueOf(rsSub.getInt("warranty_months")));
                }
              }
            }
          }
        }

        Item item = ItemFactory.createItem(type, name, description, starting_price, attributes);
        item.setId(id);
        return item;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Select Item by ID failed", e);
    }
  }

  @Override
  public List<Item> selectAll(Connection conn) {
    // Cài đặt logic lấy tất cả danh sách item tại đây nếu cần
    return new ArrayList<>();
  }

 
  //  HÀM TIỆN ÍCH-Tự động quản lý Connection 
  

  public int insert(Item item) {
    Connection connect = null;
    try {
      connect = new DatabaseConnection().getConnection();
      connect.setAutoCommit(false);

      int id = insert(item, connect); // Gọi hàm Interface ở trên

      connect.commit();
      return id;
    } catch (Exception e) {
      if (connect != null) {
        try { connect.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
      }
      throw new RuntimeException("Insert Item transaction failed", e);
    } finally {
      closeConnection(connect);
    }
  }

  public int update(Item item) {
    Connection connect = null;
    try {
      connect = new DatabaseConnection().getConnection();
      connect.setAutoCommit(false);

      int rows = update(item, connect); // Gọi hàm Interface ở trên

      connect.commit();
      return rows;
    } catch (Exception e) {
      if (connect != null) {
        try { connect.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
      }
      throw new RuntimeException("Update Item transaction failed", e);
    } finally {
      closeConnection(connect);
    }
  }

  public int delete(int id) {
    try (Connection connect = new DatabaseConnection().getConnection()) {
      return delete(id, connect); // Gọi hàm Interface ở trên
    } catch (SQLException e) {
      throw new RuntimeException("Delete Item failed", e);
    }
  }

  public Item selectById(int id) {
    try (Connection connect = new DatabaseConnection().getConnection()) {
      return selectById(id, connect); // Gọi hàm Interface ở trên
    } catch (SQLException e) {
      throw new RuntimeException("Select Item failed", e);
    }
  }

  // Hàm tiện ích để đóng kết nối
  private void closeConnection(Connection connect) {
    if (connect != null) {
      try {
        connect.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}




