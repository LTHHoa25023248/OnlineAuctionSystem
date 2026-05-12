package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;

import java.sql.*;

public class UserDAO implements DAOInterface<User> {

    @Override
    public boolean insert(User user) {

        String sql = """
            INSERT INTO users
            (username, password, email, is_active, role, balance, store_name, rating, access_level)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try {

            Connection conn = DatabaseConnection
                    .getInstance()
                    .getConnection();

            PreparedStatement pstmt = conn.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
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
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {

                ResultSet generatedKeys = pstmt.getGeneratedKeys();

                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }

                generatedKeys.close();
                pstmt.close();

                return true;
            }

            pstmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(User user) {

        String sql = """
            UPDATE users
            SET username = ?, email = ?
            WHERE id = ?
            """;

        try {

            Connection conn = DatabaseConnection
                    .getInstance()
                    .getConnection();

            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getId());

            int rows = pstmt.executeUpdate();

            pstmt.close();

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(int id) {

        String sql = "DELETE FROM users WHERE id = ?";

        try {

            Connection conn = DatabaseConnection
                    .getInstance()
                    .getConnection();

            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();

            pstmt.close();

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    @Override
public User selectById(int id) {

    String sql = "SELECT * FROM users WHERE id = ?";

    try {

        Connection conn = DatabaseConnection
                .getInstance()
                .getConnection();

        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setInt(1, id);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {

            String role = rs.getString("role");

            User user = null;

            // ===== BIDDER =====
            if (role.equals("BIDDER")) {

                Bidder bidder = new Bidder();

                bidder.setBalance(rs.getDouble("balance"));

                user = bidder;
            }

            // ===== SELLER =====
            else if (role.equals("SELLER")) {

                Seller seller = new Seller();

                seller.setStoreName(rs.getString("store_name"));
                seller.setRating(rs.getDouble("rating"));

                user = seller;
            }

            // ===== ADMIN =====
            else if (role.equals("ADMIN")) {

                Admin admin = new Admin();

                admin.setAccessLevel(
                        rs.getString("access_level")
                );

                user = admin;
            }

            // set common fields
            if (user != null) {

                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setActive(rs.getBoolean("is_active"));
            }

            rs.close();
            pstmt.close();

            return user;
        }

        rs.close();
        pstmt.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}
    
}

