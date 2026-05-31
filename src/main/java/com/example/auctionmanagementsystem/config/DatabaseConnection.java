package com.example.auctionmanagementsystem.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối MySQL
 * Cấu hình tại: src/main/resources/database.properties
 */
public class DatabaseConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        // Cấu hình mặc định (Local)
        Properties props = new Properties();
        String url      = "jdbc:mysql://localhost:3306/auction_system?serverTimezone=Asia/Ho_Chi_Minh";
        String user     = "auction_system";
        String password = "Huy2605@@";

        // Đọc và áp dụng cấu hình từ file properties
        try (InputStream in = DatabaseConnection.class
                .getClassLoader().getResourceAsStream("database.properties")) {
            if (in != null) {
                props.load(in);
                String cfgUrl  = props.getProperty("db.url");
                String cfgUser = props.getProperty("db.user");
                String cfgPass = props.getProperty("db.password");

                // Chỉ ghi đè nếu giá trị hợp lệ (không chứa placeholder "YOUR_")
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

        // Nạp MySQL JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL Driver not found!");
            throw new RuntimeException("Cannot load MySQL JDBC Driver", e);
        }
    }

    /**
     * Lấy kết nối mới.
     * Lưu ý: Phải đóng kết nối sau khi dùng (dùng try-with-resources)
     * * @return Connection
     * @throws SQLException nếu kết nối thất bại
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Kiểm tra trạng thái kết nối (dùng để debug khi khởi động ứng dụng)
     * * @return true nếu kết nối thành công và còn hoạt động
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
