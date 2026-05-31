package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.model.*;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ItemDAO implements DAOInterface<Item> {

  // Them item vao bang items, sau do goi insertSubData de them du lieu phu vao cac bang rieng
  @Override
  public int insert(Item item, Connection connect) {
    String sqlItem = "INSERT INTO items(name, description, starting_price, item_type, image_path) VALUES(?,?,?,?,?)";
    try (PreparedStatement ps = connect.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, item.getName());
      ps.setString(2, item.getDescription());
      ps.setDouble(3, item.getStartingPrice());
      // item_type lay tu ten class san pham chuyen thanh chu hoa
      ps.setString(4, item.getClass().getSimpleName().toUpperCase());
      ps.setString(5, item.getImagePath());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          int itemId = rs.getInt(1);
          item.setId(itemId);
          // Moi loai item tu xu ly them du lieu phu vao bang rieng cua no
          item.insertSubData(connect, itemId);
          return itemId;
        }
      }
      throw new SQLException("Cannot get generated item ID");
    } catch (SQLException e) {
      throw new RuntimeException("Insert Item SQL failed", e);
    }
  }

  // Cap nhat name va description trong bang items.Sau do goi updateSubData de dong bo du lieu phu tuong ung.
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

  // Xoa item khoi bang items theo id. DB tu xoa du lieu phu nho ON DELETE CASCADE 
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

  //  doc bang items truoc, sau do doc bang phu tuong ung
  // dua tren item_type de lay du lieu chi tiet tung cai
  // Dung ItemFactory de tao dung loai object 
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
        String imagePath=rs.getString("image_path");
        Map<String, String> attributes = new HashMap<>();
        // Chon bang phu tuong ung voi loai item
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
        item.setImagePath(imagePath);
        return item;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Select Item by ID failed", e);
    }
  }

  // Lay toan bo item bang cach doc id truoc, sau do goi selectById cho tung cai.
  @Override
  public List<Item> selectAll(Connection connect) {
    String sql = "SELECT id FROM items";
    List<Item> items = new ArrayList<>();
    try (PreparedStatement ps = connect.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        int id = rs.getInt("id");
        Item item = selectById(id, connect);
        if (item != null) {
          items.add(item);
        }
      }
      return items;
    } catch (SQLException e) {
      throw new RuntimeException("Select all Items failed", e);
    }
  }

 
}




