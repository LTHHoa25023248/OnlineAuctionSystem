package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.Art;
import com.example.auctionmanagementsystem.model.Electronics;
import com.example.auctionmanagementsystem.model.Item;
import com.example.auctionmanagementsystem.model.Vehicle;

import java.sql.*;

import static java.lang.Integer.parseInt;


public class ItemDAO implements DAOInterface<Item> {
  public int insert(Item item) {
    // Su dung commit, rollback. Commit de luu du lieu neu da insert thanh cong, neu khong thi
    // rollback de tra ve trang thai ban dau luc chua insert
    // tranh truong hop bang item co du lieu nhung bang vehicle,... ko co du lieu

    Connection connect = null;
    try {
      connect = new DatabaseConnection().getConnection();
      // luu du lieu tam thoi, ko luu vao database
      // tat auto commit, dam bao insert cha+con thanh cong hoac that bai cung luc
      connect.setAutoCommit(false);
      String sqlItem =
          "INSERT INTO items( name, description, starting_price, item_type) VALUES(?,?,?,?)";

      // Bang luu du lieu
      PreparedStatement ps = connect.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, item.getName());
      ps.setString(2, item.getDescription());
      ps.setDouble(3, item.getStartingPrice());
      ps.setString(4, item.getClass().getSimpleName().toUpperCase());
      // Thuc thi insert
      ps.executeUpdate();
      // lay id tu tang tu database
      ResultSet rs = ps.getGeneratedKeys();
      int itemId;
      if (rs.next()) {
        itemId = rs.getInt(1);
        item.setId(itemId);
      } else {
        throw new RuntimeException("Cannot get generated ID");
      }
      // Insert bang con
      // item tu biet no thuoc loai nao va tu insert
      item.insertSubData(connect, itemId);
      // commit du lieu, luu vao data
      connect.commit();
      return itemId;
    } catch (Exception e) {
      // nêú có lỗi xảy ra-> rolllback toan bo, neu luu du lieu sai thi tro ve trang thai ban dau
      try {
        if (connect != null)
          connect.rollback();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      throw new RuntimeException("Insert item failed");
    } finally {
      // tra ve trang thai tu dong luu du lieu xuong database
      try {
        if (connect != null)
          connect.setAutoCommit(true);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public int update(final Item item) {
    Connection connect = null;
    try {
      connect = new DatabaseConnection().getConnection();
      connect.setAutoCommit(false);
      // update bang cha
      String sql = "UPDATE items SET name=?, description =? WHERE id=?";
      PreparedStatement ps = connect.prepareStatement(sql);
      ps.setString(1, item.getName());
      ps.setString(2, item.getDescription());
      ps.setInt(3, item.getId());
      // so dong bi anh huong
      int affectedRows = ps.executeUpdate();
      // update bang con
      item.updateSubData(connect);
      connect.commit();
      return affectedRows;
    } catch (Exception e) {
      try {
        if (connect != null)
          connect.rollback();

      } catch (Exception ex) {
        ex.printStackTrace();
      }
      throw new RuntimeException("Update failed");
    } finally {
      try {
        if (connect != null)
          connect.setAutoCommit(true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  public int delete(int id) {
    try (Connection connect = new DatabaseConnection().getConnection()) {
      String sql = "DELETE FROM items WHERE id =?";
      PreparedStatement ps = connect.prepareStatement(sql);
      ps.setInt(1, id);
      // return lai so dong bi xoa
      return ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("Delete failed");
    }
  }


  public Item selectById(int id) throws SQLException {
    try (Connection connect = new DatabaseConnection().getConnection()) {
      String sql = "SELECT * FROM items WHERE id =?";
      PreparedStatement ps = connect.prepareStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      if (!rs.next())
        return null;
      String type = rs.getString("item_type");
      String name = rs.getString("name");
      String description = rs.getString("description");
      double starting_price = rs.getDouble("starting_price");
      int itemId = rs.getInt("id");
      if ("VEHICLE".equals(type)) {
        PreparedStatement ps2 =
            connect.prepareStatement("SELECT * FROM vehicle_items WHERE item_id=?");
        ps2.setInt(1, itemId);
        ResultSet rs2 = ps2.executeQuery();
        if (rs2.next()) {
          Vehicle vehicle = new Vehicle(name, description, starting_price, rs2.getInt("year"),
              rs2.getDouble("mileage"));
          vehicle.setId(itemId);
          return vehicle;
        }
      } else if ("ART".equals(type)) {
        PreparedStatement ps2 = connect.prepareStatement("SELECT * FROM art_items WHERE item_id=?");
        ps2.setInt(1, itemId);
        ResultSet rs2 = ps2.executeQuery();
        if (rs2.next()) {
          Art art = new Art(name, description, starting_price, rs2.getString("artist"),
              rs2.getString("theme"), rs2.getString("material"));
          art.setId(itemId);
          return art;
        }
      } else if ("ELECTRONICS".equals(type)) {
        PreparedStatement ps2 =
            connect.prepareStatement("SELECT * FROM electronics_items WHERE item_id=?");
        ps2.setInt(1, itemId);
        ResultSet rs2 = ps2.executeQuery();
        if (rs2.next()) {
          Electronics electronic = new Electronics(name, description, starting_price,
              rs2.getString("brand"), rs2.getInt("warranty_months"));
          electronic.setId(itemId);
          return electronic;
        }
      }
      return null;
    }
  }
}


