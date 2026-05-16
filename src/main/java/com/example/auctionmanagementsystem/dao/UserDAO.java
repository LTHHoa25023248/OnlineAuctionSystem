package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;

import java.sql.*;

/**
 * UserDAO — Tất cả thao tác database liên quan đến User.
 *
 * Các chức năng chính: - login() : Xác thực đăng nhập - register() : Đăng ký tài khoản Bidder mới -
 * registerSeller() : Đăng ký tài khoản Seller mới (thêm storeName) - emailExists() : Kiểm tra email
 * đã tồn tại chưa - usernameExists() : Kiểm tra username đã tồn tại chưa - saveResetCode() : Lưu mã
 * OTP reset password vào DB - verifyResetCode() : Kiểm tra mã OTP còn hợp lệ không -
 * resetPassword() : Cập nhật mật khẩu mới sau khi xác thực OTP - selectById() : Lấy User theo ID -
 * update() : Cập nhật thông tin User - delete() : Soft-delete User
 */
public class UserDAO {

  // ══════════════════════════════════════════════════════════════════════════
  // AUTH
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Đăng nhập — so sánh username + password.
   * 
   * @return User đúng loại (Bidder / Seller / Admin) nếu thành công, null nếu sai.
   */
  public static User login(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = TRUE";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username.trim());
      pstmt.setString(2, password);

      ResultSet rs = pstmt.executeQuery();
      if (rs.next())
        return mapRowToUser(rs);

    } catch (SQLException e) {
      System.err.println("[UserDAO.login] SQL Error: " + e.getMessage());
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Đăng ký tài khoản Bidder mới (role = BIDDER, balance = 0).
   * 
   * @return true nếu insert thành công.
   */
  public static boolean register(String firstName, String lastName, String username, String email,
      String phone, String password, String address) {
    if (usernameExists(username))
      return false;
    if (emailExists(email))
      return false;

    String sql = """
        INSERT INTO users
        (first_name, last_name, username, email, phone, password, address,
         role, balance, is_active)
        VALUES (?, ?, ?, ?, ?, ?, ?, 'BIDDER', 0.0, TRUE)
        """;

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setString(1, firstName.trim());
      pstmt.setString(2, lastName.trim());
      pstmt.setString(3, username.trim());
      pstmt.setString(4, email.trim());
      pstmt.setString(5, phone.trim());
      pstmt.setString(6, password);
      pstmt.setString(7, address != null ? address.trim() : "");

      return pstmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("[UserDAO.register] SQL Error: " + e.getMessage());
      e.printStackTrace();
    }

    return false;
  }


  public static boolean registerSeller(String firstName, String lastName, String username,
      String email, String phone, String password, String address) {
    if (usernameExists(username))
      return false;
    if (emailExists(email))
      return false;

    String sql = """
        INSERT INTO users
        (first_name, last_name, username, email, phone, password, address,
         role, is_active)
        VALUES (?, ?, ?, ?, ?, ?, ?, 'SELLER', TRUE)
        """;

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setString(1, firstName.trim());
      pstmt.setString(2, lastName.trim());
      pstmt.setString(3, username.trim());
      pstmt.setString(4, email.trim());
      pstmt.setString(5, phone.trim());
      pstmt.setString(6, password);
      pstmt.setString(7, address != null ? address.trim() : "");

      return pstmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("[UserDAO.registerSeller] SQL Error: " + e.getMessage());
      e.printStackTrace();
    }

    return false;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // FORGOT PASSWORD — OTP FLOW
  // ══════════════════════════════════════════════════════════════════════════

  public static boolean emailExistsForReset(String email) {
    String sql = "SELECT 1 FROM users WHERE email = ? AND is_active = TRUE";
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, email.trim());
      return pstmt.executeQuery().next();
    } catch (SQLException e) {
      System.err.println("[UserDAO.emailExistsForReset] SQL Error: " + e.getMessage());
    }
    return false;
  }

  public static String getStringField(int userId, String column) {
    String sql = "SELECT " + column + " FROM users WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next())
        return rs.getString(1);
    } catch (SQLException e) {
      System.err.println("[UserDAO.getStringField] Error: " + e.getMessage());
    }
    return "";
  }

  public static boolean saveResetCode(String email, String code, long expiresAt) {
    String deleteSql = "DELETE FROM password_resets WHERE email = ?";
    String insertSql = "INSERT INTO password_resets (email, code, expires_at) VALUES (?, ?, ?)";
    try (Connection conn = DatabaseConnection.getConnection()) {
      try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
        del.setString(1, email.trim());
        del.executeUpdate();
      }
      try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
        ins.setString(1, email.trim());
        ins.setString(2, code);
        ins.setLong(3, expiresAt);
        return ins.executeUpdate() > 0;
      }
    } catch (SQLException e) {
      System.err.println("[UserDAO.saveResetCode] SQL Error: " + e.getMessage());
    }
    return false;
  }

  public static boolean verifyResetCode(String email, String code) {
    String sql = """
        SELECT 1 FROM password_resets
        WHERE email = ? AND code = ? AND expires_at > ?
        """;
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, email.trim());
      pstmt.setString(2, code.trim());
      pstmt.setLong(3, System.currentTimeMillis());
      return pstmt.executeQuery().next();
    } catch (SQLException e) {
      System.err.println("[UserDAO.verifyResetCode] SQL Error: " + e.getMessage());
    }
    return false;
  }

  public static boolean resetPassword(String email, String code, String newPassword) {
    if (!verifyResetCode(email, code))
      return false;

    String updateSql = "UPDATE users SET password = ? WHERE email = ?";
    String deleteSql = "DELETE FROM password_resets WHERE email = ?";

    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try (PreparedStatement upd = conn.prepareStatement(updateSql);
          PreparedStatement del = conn.prepareStatement(deleteSql)) {
        upd.setString(1, newPassword);
        upd.setString(2, email.trim());
        int rows = upd.executeUpdate();
        del.setString(1, email.trim());
        del.executeUpdate();
        conn.commit();
        return rows > 0;
      } catch (SQLException e) {
        conn.rollback();
        System.err.println("[UserDAO.resetPassword] Rollback: " + e.getMessage());
      }
    } catch (SQLException e) {
      System.err.println("[UserDAO.resetPassword] SQL Error: " + e.getMessage());
    }
    return false;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // CRUD
  // ══════════════════════════════════════════════════════════════════════════

  public static User selectById(int id) {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next())
        return mapRowToUser(rs);
    } catch (SQLException e) {
      System.err.println("[UserDAO.selectById] SQL Error: " + e.getMessage());
    }
    return null;
  }

  public static boolean update(User user) {
    String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, user.getUsername());
      pstmt.setString(2, user.getEmail());
      pstmt.setInt(3, user.getId());
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("[UserDAO.update] SQL Error: " + e.getMessage());
    }
    return false;
  }

  /** Soft delete — set is_active = FALSE, không xóa dữ liệu thật */
  public static boolean delete(int id) {
    String sql = "UPDATE users SET is_active = FALSE WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("[UserDAO.delete] SQL Error: " + e.getMessage());
    }
    return false;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // VALIDATION HELPERS
  // ══════════════════════════════════════════════════════════════════════════

  public static boolean usernameExists(String username) {
    return fieldExists("username", username);
  }

  public static boolean emailExists(String email) {
    return fieldExists("email", email);
  }

  private static boolean fieldExists(String column, String value) {
    String sql = "SELECT 1 FROM users WHERE " + column + " = ?";
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, value.trim());
      return pstmt.executeQuery().next();
    } catch (SQLException e) {
      System.err.println("[UserDAO.fieldExists] SQL Error: " + e.getMessage());
    }
    return false;
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
