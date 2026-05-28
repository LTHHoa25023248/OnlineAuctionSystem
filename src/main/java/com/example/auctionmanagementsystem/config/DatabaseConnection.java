package com.example.auctionmanagementsystem.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection — Quản lý kết nối MySQL.
 *
 * Cấu hình qua file: src/main/resources/database.properties
 * Để chuyển sang cloud DB: chỉnh sửa database.properties, không cần sửa file này.
 */
public class DatabaseConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

  static {
    // Đọc cấu hình từ database.properties
    Properties props = new Properties();
    String url      = "jdbc:mysql://localhost:3306/auction_system?serverTimezone=Asia/Ho_Chi_Minh";
    String user     = "auction_system";
    String password = "Huy2605@@";

    try (InputStream in = DatabaseConnection.class
            .getClassLoader().getResourceAsStream("database.properties")) {
      if (in != null) {
        props.load(in);
        String cfgUrl  = props.getProperty("db.url");
        String cfgUser = props.getProperty("db.user");
        String cfgPass = props.getProperty("db.password");
        // Chỉ dùng giá trị từ file nếu chưa còn placeholder
        if (cfgUrl  != null && !cfgUrl.contains("YOUR_"))  url      = cfgUrl;
        if (cfgUser != null && !cfgUser.contains("YOUR_")) user     = cfgUser;
        if (cfgPass != null && !cfgPass.contains("YOUR_")) password = cfgPass;
        System.out.println("[DatabaseConnection] Config loaded: " + url.replaceAll("password=[^&]*", "password=***"));
      } else {
        System.out.println("[DatabaseConnection] database.properties not found, using default.");
      }
    } catch (Exception e) {
      System.err.println("[DatabaseConnection] Failed to load properties: " + e.getMessage());
    }

    URL      = url;
    USER     = user;
    PASSWORD = password;

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      System.err.println("[DatabaseConnection] MySQL Driver not found!");
      throw new RuntimeException("Cannot load MySQL JDBC Driver", e);
    }
  }

  /**
   * Lấy một Connection mới từ DriverManager. Caller phải đóng Connection sau khi dùng xong — dùng
   * try-with-resources:
   *
   * <pre>
   * try (Connection conn = DatabaseConnection.getConnection()) {
   *   // thực hiện query
   * }
   * </pre>
   *
   * @return Connection đến MySQL
   * @throws SQLException nếu không kết nối được (sai thông tin, DB chưa bật...)
   */
  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }

  /**
   * Kiểm tra kết nối có hoạt động không — dùng để debug khi start app.
   *
   * @return true nếu kết nối thành công
   */
  public static boolean testConnection() {
    try (Connection conn = getConnection()) {
      return conn != null && !conn.isClosed();
    } catch (SQLException e) {
      System.err.println("[DatabaseConnection] Test failed: " + e.getMessage());
      return false;
    }
  }
}
