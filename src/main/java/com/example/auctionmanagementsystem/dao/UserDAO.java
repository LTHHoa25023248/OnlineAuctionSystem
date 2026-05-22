package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;
import java.sql.*;

public class UserDAO implements DAOInterface<User> {

  // =====================================================
  // INSERT USER
  // =====================================================
  @Override
  public int insert(User user) {

    String sql = """
        INSERT INTO users
        (
            username,
            user_password,
            email,
            is_active,
            role,
            balance,
            store_name,
            rating,
            access_level
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    try (Connection conn = DatabaseConnection.getConnection();

        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      // ===== COMMON FIELDS =====
      pstmt.setString(1, user.getUsername());

      // NOTE:
      // Nên hash password trước khi lưu DB
      pstmt.setString(2, user.getPassword());

      pstmt.setString(3, user.getEmail());

      pstmt.setBoolean(4, user.isActive());

      // ===== ROLE-SPECIFIC =====
      if (user instanceof Bidder bidder) {

        pstmt.setString(5, "BIDDER");

        pstmt.setDouble(6, bidder.getBalance());

        pstmt.setNull(7, Types.VARCHAR);
        pstmt.setNull(8, Types.DOUBLE);
        pstmt.setNull(9, Types.VARCHAR);

      } else if (user instanceof Seller seller) {

        pstmt.setString(5, "SELLER");

        pstmt.setNull(6, Types.DOUBLE);

        pstmt.setString(7, seller.getStoreName());

        pstmt.setDouble(8, seller.getRating());

        pstmt.setNull(9, Types.VARCHAR);

      } else if (user instanceof Admin admin) {

        pstmt.setString(5, "ADMIN");

        pstmt.setNull(6, Types.DOUBLE);
        pstmt.setNull(7, Types.VARCHAR);
        pstmt.setNull(8, Types.DOUBLE);

        pstmt.setString(9, admin.getAccessLevel());

      } else {

        throw new SQLException("Unsupported user type.");
      }

      int affectedRows = pstmt.executeUpdate();

      // ===== GET GENERATED ID =====
      if (affectedRows > 0) {

        try (ResultSet rs = pstmt.getGeneratedKeys()) {

          if (rs.next()) {

            user.setId(rs.getInt(1));
          }
        }
      }

      return affectedRows;

    } catch (SQLException e) {

      System.err.println("[UserDAO.insert] SQL Error: " + e.getMessage());

      e.printStackTrace();
    }

    return 0;
  }

  // =====================================================
  // UPDATE USER
  // =====================================================
  @Override
  public int update(User user) {

    String sql = """
        UPDATE users
        SET
            username = ?,
            user_password = ?,
            email = ?,
            is_active = ?,
            balance = ?,
            store_name = ?,
            rating = ?,
            access_level = ?
        WHERE id = ?
        """;

    try (Connection conn = DatabaseConnection.getConnection();

        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // ===== COMMON =====
      pstmt.setString(1, user.getUsername());

      pstmt.setString(2, user.getPassword());

      pstmt.setString(3, user.getEmail());

      pstmt.setBoolean(4, user.isActive());

      // ===== BIDDER =====
      if (user instanceof Bidder bidder) {

        pstmt.setDouble(5, bidder.getBalance());

        pstmt.setNull(6, Types.VARCHAR);
        pstmt.setNull(7, Types.DOUBLE);
        pstmt.setNull(8, Types.VARCHAR);
      }

      // ===== SELLER =====
      else if (user instanceof Seller seller) {

        pstmt.setNull(5, Types.DOUBLE);

        pstmt.setString(6, seller.getStoreName());

        pstmt.setDouble(7, seller.getRating());

        pstmt.setNull(8, Types.VARCHAR);
      }

      // ===== ADMIN =====
      else if (user instanceof Admin admin) {

        pstmt.setNull(5, Types.DOUBLE);
        pstmt.setNull(6, Types.VARCHAR);
        pstmt.setNull(7, Types.DOUBLE);

        pstmt.setString(8, admin.getAccessLevel());
      }

      pstmt.setInt(9, user.getId());

      return pstmt.executeUpdate();

    } catch (SQLException e) {

      System.err.println("[UserDAO.update] SQL Error: " + e.getMessage());

      e.printStackTrace();
    }

    return 0;
  }

  // =====================================================
  // DELETE USER
  // =====================================================
  @Override
  public int delete(int id) {

    String sql = "DELETE FROM users WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();

        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, id);

      return pstmt.executeUpdate();

    } catch (SQLException e) {

      System.err.println("[UserDAO.delete] SQL Error: " + e.getMessage());

      e.printStackTrace();
    }

    return 0;
  }

  // =====================================================
  // SELECT USER BY ID
  // =====================================================
  @Override
  public User selectById(int id) {

    String sql = "SELECT * FROM users WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();

        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, id);

      try (ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {

          return mapResultSetToUser(rs);
        }
      }

    } catch (SQLException e) {

      System.err.println("[UserDAO.selectById] SQL Error: " + e.getMessage());

      e.printStackTrace();
    }

    return null;
  }

  // =====================================================
  // CHECK USERNAME EXISTS
  // =====================================================
  public boolean usernameExists(String username) {

    String sql = "SELECT id FROM users WHERE username = ?";

    try (Connection conn = DatabaseConnection.getConnection();

        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);

      try (ResultSet rs = pstmt.executeQuery()) {

        return rs.next();
      }

    } catch (SQLException e) {

      System.err.println("[UserDAO.usernameExists] SQL Error: " + e.getMessage());

      e.printStackTrace();
    }

    return false;
  }

  // =====================================================
  // CHECK EMAIL EXISTS
  // =====================================================
  public boolean emailExists(String email) {

    String sql = "SELECT id FROM users WHERE email = ?";

    try (Connection conn = DatabaseConnection.getConnection();

        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, email);

      try (ResultSet rs = pstmt.executeQuery()) {

        return rs.next();
      }

    } catch (SQLException e) {

      System.err.println("[UserDAO.emailExists] SQL Error: " + e.getMessage());

      e.printStackTrace();
    }

    return false;
  }

  // =====================================================
  // REGISTER BIDDER
  // =====================================================
  public int register(String username, String email, String password) {

    if (usernameExists(username)) {

      System.out.println("Username already exists.");

      return 0;
    }

    if (emailExists(email)) {

      System.out.println("Email already exists.");

      return 0;
    }

    Bidder bidder = new Bidder();

    bidder.setUsername(username);
    bidder.setEmail(email);

    // NOTE:
    // Nên hash password bằng BCrypt
    bidder.setPassword(password);

    bidder.setActive(true);

    bidder.setBalance(0.0);

    return insert(bidder);
  }

  // =====================================================
  // MAP RESULTSET TO USER OBJECT
  // =====================================================
  private User mapResultSetToUser(ResultSet rs)
            throws SQLException {

        String role =
                rs.getString("role");

        User user;

        // ===== BIDDER =====
        if ("BIDDER".equals(role)) {

            Bidder bidder =
                    new Bidder();

            bidder.setBalance(
                    rs.getDouble("balance")
            );

            user = bidder;
        }

        // ===== SELLER =====
        else if ("SELLER".equals(role)) {

            Seller seller =
                    new Seller();

            seller.setStoreName(
                    rs.getString("store_name")
            );

            seller.setRating(
                    rs.getDouble("rating")
            );

            user = seller;
        }

        // ===== ADMIN =====
        else if ("ADMIN".equals(role)) {

            Admin admin =
                    new Admin();

            admin.setAccessLevel(
                    rs.getString("access_level")
            );

            user = admin;
        }

        else {

            throw new SQLException(
                    "Invalid role: " + role
            );
        }

        // ===== COMMON FIELDS =====
        user.setId(
                rs.getInt("id")
        );

        user.setUsername(
                rs.getString("username")
        );

        user.setPassword(
                rs.getString("user_password")
        );

        user.setEmail(
                rs.getString("email")
        );

        user.setActive(
                rs.getBoolean("is_active")
        );

        return user;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // MAPPING HELPER
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Map một ResultSet row thành đúng loại User (Bidder / Seller / Admin). Gọi khi rs.next() đã trả
   * về true.
   */
  private static User mapRowToUser(ResultSet rs) throws SQLException {
    String role=rs.getString("role");User user;

    switch(role){case"SELLER"->{Seller seller=new Seller();seller.setStoreName(rs.getString("store_name"));seller.setRating(rs.getDouble("rating"));user=seller;}case"ADMIN"->{Admin admin=new Admin();admin.setAccessLevel(rs.getString("access_level"));user=admin;}default->{ // "BIDDER"
                                                                                                                                                                                                                                                                                 // +
                                                                                                                                                                                                                                                                                 // fallback
    Bidder bidder=new Bidder();bidder.setBalance(rs.getDouble("balance"));user=bidder;}}

    // Gán các field chung
    user.setId(rs.getInt("id"));user.setUsername(rs.getString("username"));user.setPassword(rs.getString("password"));user.setEmail(rs.getString("email"));user.setActive(rs.getBoolean("is_active"));

    return user;
  }
}
