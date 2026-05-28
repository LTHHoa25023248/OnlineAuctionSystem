package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class UserDAO implements DAOInterface<User> {

  // =====================================================
  // INSERT USER
  // =====================================================
  @Override
  public int insert(User user, Connection conn) {
    String sql = """
        INSERT INTO users (username, user_password, email, is_active, role, balance, store_name, rating, access_level)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    // Dùng conn được truyền vào thay vì tự mở connection riêng
    try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pstmt.setString(1, user.getUsername());
      pstmt.setString(2, user.getPassword()); // Nên hash
      pstmt.setString(3, user.getEmail());
      pstmt.setBoolean(4, user.isActive());

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

      if (affectedRows > 0) {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
          if (rs.next()) {
            user.setId(rs.getInt(1));
          }
        }
      }
      return affectedRows;
    } catch (SQLException e) {
      // Ném RuntimeException như AuctionDAO thay vì in ra rồi trả về 0
      throw new RuntimeException("[UserDAO.insert] SQL Error: " + e.getMessage(), e);
    }
  }

  // =====================================================
  // UPDATE USER
  // =====================================================
  @Override
  public int update(User user, Connection conn) {
    String sql = """
        UPDATE users SET username = ?, user_password = ?, email = ?, is_active = ?, 
                         balance = ?, store_name = ?, rating = ?, access_level = ?
        WHERE id = ?
        """;

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, user.getUsername());
      pstmt.setString(2, user.getPassword());
      pstmt.setString(3, user.getEmail());
      pstmt.setBoolean(4, user.isActive());

      if (user instanceof Bidder bidder) {
        pstmt.setDouble(5, bidder.getBalance());
        pstmt.setNull(6, Types.VARCHAR);
        pstmt.setNull(7, Types.DOUBLE);
        pstmt.setNull(8, Types.VARCHAR);
      } else if (user instanceof Seller seller) {
        pstmt.setNull(5, Types.DOUBLE);
        pstmt.setString(6, seller.getStoreName());
        pstmt.setDouble(7, seller.getRating());
        pstmt.setNull(8, Types.VARCHAR);
      } else if (user instanceof Admin admin) {
        pstmt.setNull(5, Types.DOUBLE);
        pstmt.setNull(6, Types.VARCHAR);
        pstmt.setNull(7, Types.DOUBLE);
        pstmt.setString(8, admin.getAccessLevel());
      }

      pstmt.setInt(9, user.getId());
      return pstmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("[UserDAO.update] SQL Error", e);
    }
  }

  // =====================================================
  // DELETE USER
  // =====================================================
  @Override
  public int delete(int id, Connection conn) {
    String sql = "DELETE FROM users WHERE id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      return pstmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("[UserDAO.delete] SQL Error", e);
    }
  }

  // =====================================================
  // SELECT USER BY ID
  // =====================================================
  @Override
  public User selectById(int id, Connection conn) {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToUser(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("[UserDAO.selectById] SQL Error", e);
    }
    return null;
  }

  // =====================================================
  // SELECT ALL USERS (Bắt buộc phải thêm để khớp Interface)
  // =====================================================
  @Override
  public List<User> selectAll(Connection conn) {
    List<User> userList = new ArrayList<>();
    String sql = "SELECT * FROM users ORDER BY id DESC";

    try (PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        userList.add(mapResultSetToUser(rs));
      }
      return userList;
    } catch (SQLException e) {
      throw new RuntimeException("[UserDAO.selectAll] SQL Error", e);
    }
  }
  // =====================================================
  // CHECK USERNAME EXISTS
  // =====================================================
  public static boolean usernameExists(String username) {

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
  public static boolean emailExists(String email) {

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
// CHECK EMAIL EXISTS FOR RESET PASSWORD
// =====================================================
  public static boolean emailExistsForReset(String email) {

    String sql = "SELECT id FROM users WHERE email = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, email);

      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next();
      }

    } catch (SQLException e) {

      System.err.println("[UserDAO.emailExistsForReset] SQL Error: " + e.getMessage());
      e.printStackTrace();
    }

    return false;
  }
// =====================================================
// SAVE RESET CODE
// =====================================================
  public static boolean saveResetCode(String email,
                                      String code,
                                      long expiresAt) {

    String sql = """
        UPDATE users
        SET reset_code = ?,
            reset_code_expiry = ?
        WHERE email = ?
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, code);
      // Dùng LocalDateTime để tránh lệch timezone với MySQL CURRENT_TIMESTAMP
      long minutesLeft = (expiresAt - System.currentTimeMillis()) / 60_000;
      java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusMinutes(Math.max(minutesLeft, 1));
      pstmt.setTimestamp(2, Timestamp.valueOf(expiry));
      pstmt.setString(3, email);

      return pstmt.executeUpdate() > 0;

    } catch (SQLException e) {

      System.err.println("[UserDAO.saveResetCode] SQL Error: " + e.getMessage());
      e.printStackTrace();
    }

    return false;
  }
// =====================================================
// VERIFY RESET CODE
// =====================================================
  public static boolean verifyResetCode(String email,
                                        String code) {

    String sql = """
        SELECT id
        FROM users
        WHERE email = ?
          AND reset_code = ?
          AND reset_code_expiry > CURRENT_TIMESTAMP
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, email);
      pstmt.setString(2, code);

      try (ResultSet rs = pstmt.executeQuery()) {

        return rs.next();
      }

    } catch (SQLException e) {

      System.err.println("[UserDAO.verifyResetCode] SQL Error: " + e.getMessage());
      e.printStackTrace();
    }

    return false;
  }
// =====================================================
// RESET PASSWORD
// =====================================================
  public static boolean resetPassword(String email,
                                      String code,
                                      String newPassword) {

    // Kiểm tra OTP trước
    if (!verifyResetCode(email, code)) {
      return false;
    }

    String sql = """
        UPDATE users
        SET user_password = ?,
            reset_code = NULL,
            reset_code_expiry = NULL
        WHERE email = ?
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, newPassword);

      pstmt.setString(2, email);

      return pstmt.executeUpdate() > 0;

    } catch (SQLException e) {

      System.err.println("[UserDAO.resetPassword] SQL Error: " + e.getMessage());
      e.printStackTrace();
    }

    return false;
  }

  // ================
  // REGISTER BIDDER
  // ================
  public static boolean register(String firstName, String lastName, String username,
                                 String email, String phone, String password, String address) {
    String sql = """
        INSERT INTO users 
        (first_name, last_name, username, email, phone, user_password, address, role, is_active, balance) 
        VALUES (?, ?, ?, ?, ?, ?, ?, 'BIDDER', true, 50000.0)
        """;
    return executeRegister(sql, firstName, lastName, username, email, phone, password, address);
  }

  // ================
  // REGISTER SELLER
  // ================
  public static boolean registerSeller(String firstName, String lastName, String username,
                                       String email, String phone, String password, String address) {
    String sql = """
        INSERT INTO users 
        (first_name, last_name, username, email, phone, user_password, address, role, is_active, rating) 
        VALUES (?, ?, ?, ?, ?, ?, ?, 'SELLER', true, 0.0)
        """;
    return executeRegister(sql, firstName, lastName, username, email, phone, password, address);
  }

  // Hàm helper dùng chung cho cả 2 loại đăng ký
  private static boolean executeRegister(String sql, String firstName, String lastName,
                                         String username, String email, String phone,
                                         String password, String address) {
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, firstName);
      pstmt.setString(2, lastName);
      pstmt.setString(3, username);
      pstmt.setString(4, email);
      pstmt.setString(5, phone);
      pstmt.setString(6, password); // Hãy hash password trước khi lưu thực tế
      pstmt.setString(7, address);

      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("[UserDAO.executeRegister] SQL Error: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
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

  // ==========================================================================
  // MAPPING HELPER
  // ==========================================================================

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
    user.setId(rs.getInt("id"));user.setUsername(rs.getString("username"));user.setPassword(rs.getString("user_password"));user.setEmail(rs.getString("email"));user.setActive(rs.getBoolean("is_active"));

    return user;
  }
  // =====================================================
  // LOGIN 
  // =====================================================
  public static User login(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND user_password = ? AND is_active = true";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      pstmt.setString(2, password); // Lưu ý: Cần xử lý Hash Password ở đây nếu lúc đăng ký đã hash

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          // Khởi tạo UserDAO để gọi hàm map (vì mapResultSetToUser không phải static)
          return new UserDAO().mapResultSetToUser(rs);
        }
      }
    } catch (SQLException e) {
      System.err.println("[UserDAO.login] SQL Error: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  // =====================================================
  // UPDATE PROFILE (phone, firstName, lastName)
  // =====================================================
  public static boolean updateProfile(int userId, String firstName, String lastName, String phone) {
    String sql = "UPDATE users SET first_name=?, last_name=?, phone=? WHERE id=?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, firstName != null ? firstName : "");
      pstmt.setString(2, lastName != null ? lastName : "");
      pstmt.setString(3, phone != null ? phone : "");
      pstmt.setInt(4, userId);
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("[UserDAO.updateProfile] SQL Error: " + e.getMessage());
      return false;
    }
  }

  // =====================================================
  // GET STRING FIELD 
  // =====================================================
  public static String getStringField(int userId, String fieldName) {
    // Chỉ cho phép các cột an toàn để tránh SQL Injection
    if (!fieldName.matches("^[a-zA-Z_]+$")) return "";

    String sql = "SELECT " + fieldName + " FROM users WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, userId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
      }
    } catch (SQLException e) {
      System.err.println("[UserDAO.getStringField] SQL Error: " + e.getMessage());
      e.printStackTrace();
    }
    return "";
  }

  // =====================================================
  // SET ACTIVE (Ban / Unban)
  // =====================================================
  public static boolean setActive(int userId, boolean active) {
    String sql = "UPDATE users SET is_active = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setBoolean(1, active);
      ps.setInt(2, userId);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("[UserDAO.setActive] SQL Error: " + e.getMessage());
      return false;
    }
  }

  // =====================================================
  // BALANCE HELPERS — dùng bởi PaymentService
  // =====================================================
  public static double getBalance(int userId, Connection conn) {
    String sql = "SELECT balance FROM users WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getDouble("balance");
      }
    } catch (SQLException e) {
      throw new RuntimeException("[UserDAO.getBalance] SQL Error", e);
    }
    return 0.0;
  }

  public static void updateBalance(int userId, double newBalance, Connection conn) {
    String sql = "UPDATE users SET balance = ? WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, newBalance);
      ps.setInt(2, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("[UserDAO.updateBalance] SQL Error", e);
    }
  }

}
