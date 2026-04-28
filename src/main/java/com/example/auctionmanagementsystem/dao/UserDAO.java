package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserDAO {
    
    // Hàm này nhận vào một User (có thể là Bidder, Seller, Admin)
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (username, password, email, is_active, role, balance, store_name, rating, access_level) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             // Statement.RETURN_GENERATED_KEYS rất quan trọng: để lấy ID tự tăng
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setBoolean(4, user.isActive());

            // Xử lý đa hình (Polymorphism) để lưu dữ liệu chuẩn xác
            if (user instanceof Bidder) {
                Bidder bidder = (Bidder) user;
                pstmt.setString(5, "BIDDER");
                pstmt.setDouble(6, bidder.getBalance());
                pstmt.setNull(7, java.sql.Types.VARCHAR);
                pstmt.setNull(8, java.sql.Types.DOUBLE);
                pstmt.setNull(9, java.sql.Types.VARCHAR);
            } else if (user instanceof Seller) {
                Seller seller = (Seller) user;
                pstmt.setString(5, "SELLER");
                pstmt.setNull(6, java.sql.Types.DOUBLE);
                pstmt.setString(7, seller.getStoreName());
                pstmt.setDouble(8, seller.getRating());
                pstmt.setNull(9, java.sql.Types.VARCHAR);
            } else if (user instanceof Admin) {
                Admin admin = (Admin) user;
                pstmt.setString(5, "ADMIN");
                pstmt.setNull(6, java.sql.Types.DOUBLE);
                pstmt.setNull(7, java.sql.Types.VARCHAR);
                pstmt.setNull(8, java.sql.Types.DOUBLE);
                pstmt.setString(9, admin.getAccessLevel());
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Lấy ID tự động tăng từ DB và gán ngược lại cho object
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1)); // Gán ID thực tế cho User
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}