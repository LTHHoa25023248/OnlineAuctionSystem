package com.example.auctionmanagementsystem.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — Quản lý kết nối MySQL.
 *
 * Pattern: Mỗi lần gọi getConnection() tạo một Connection mới.
 * Sử dụng try-with-resources trong DAO để đảm bảo Connection luôn được đóng.
 *
 * Cấu hình:
 *   - URL      : jdbc:mysql://localhost:3306/auction_system
 *   - USER     : auction_system
 *   - PASSWORD : Huy2605@@
 *
 * Nếu muốn thay đổi thông tin kết nối, chỉnh sửa các hằng số bên dưới.
 */
public class DatabaseConnection {

    // ── Cấu hình kết nối ─────────────────────────────────────────────────────
    private static final String URL      = "jdbc:mysql://127.0.0.1:3306/auction_system?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
    private static final String USER     = "auction_system";
    private static final String PASSWORD = "Huy2605@@";

    // ── Load MySQL Driver một lần khi class được nạp ─────────────────────────
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL Driver not found!");
            throw new RuntimeException("Cannot load MySQL JDBC Driver", e);
        }
    }

    /**
     * Lấy một Connection mới từ DriverManager.
     * Caller phải đóng Connection sau khi dùng xong — dùng try-with-resources:
     *
     * <pre>
     * try (Connection conn = DatabaseConnection.getConnection()) {
     *     // thực hiện query
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
