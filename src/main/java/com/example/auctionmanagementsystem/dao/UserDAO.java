package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;

import java.sql.*;

public class UserDAO implements DAOInterface<User> {

    // =========================
    // INSERT USER
    // =========================
    @Override
    public int insert(User user) {

        String sql = """
            INSERT INTO users
            (
                username,
                password,
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

        try (
                Connection conn =
                        DatabaseConnection.getConnection();

                PreparedStatement pstmt =
                        conn.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            // ===== COMMON FIELDS =====
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setBoolean(4, user.isActive());

            // ===== BIDDER =====
            if (user instanceof Bidder bidder) {

                pstmt.setString(5, "BIDDER");

                pstmt.setDouble(6, bidder.getBalance());

                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.DOUBLE);
                pstmt.setNull(9, Types.VARCHAR);
            }

            // ===== SELLER =====
            else if (user instanceof Seller seller) {

                pstmt.setString(5, "SELLER");

                pstmt.setNull(6, Types.DOUBLE);

                pstmt.setString(7, seller.getStoreName());

                pstmt.setDouble(8, seller.getRating());

                pstmt.setNull(9, Types.VARCHAR);
            }

            // ===== ADMIN =====
            else if (user instanceof Admin admin) {

                pstmt.setString(5, "ADMIN");

                pstmt.setNull(6, Types.DOUBLE);
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.DOUBLE);

                pstmt.setString(
                        9,
                        admin.getAccessLevel()
                );
            }

            int affectedRows = pstmt.executeUpdate();

            // ===== GET GENERATED ID =====
            if (affectedRows > 0) {

                try (
                        ResultSet rs =
                                pstmt.getGeneratedKeys()
                ) {

                    if (rs.next()) {

                        user.setId(rs.getInt(1));
                    }
                }
            }

            return affectedRows;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    // =========================
    // UPDATE USER
    // =========================
    @Override
    public int update(User user) {

        String sql = """
            UPDATE users
            SET
                username = ?,
                email = ?,
                password = ?,
                is_active = ?
            WHERE id = ?
            """;

        try (
                Connection conn =
                        DatabaseConnection.getConnection();

                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, user.getUsername());

            pstmt.setString(2, user.getEmail());

            pstmt.setString(3, user.getPassword());

            pstmt.setBoolean(4, user.isActive());

            pstmt.setInt(5, user.getId());

            return pstmt.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    // =========================
    // DELETE USER
    // =========================
    @Override
    public int delete(int id) {

        String sql =
                "DELETE FROM users WHERE id = ?";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();

                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    // =========================
    // SELECT USER BY ID
    // =========================
    @Override
    public User selectById(int id) {

        String sql =
                "SELECT * FROM users WHERE id = ?";

        try (
                Connection conn =                   
                        DatabaseConnection.getConnection();

                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setInt(1, id);

            try (
                    ResultSet rs =
                            pstmt.executeQuery()
            ) {

                if (rs.next()) {

                    String role =
                            rs.getString("role");

                    User user = null;

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

                    // ===== COMMON FIELDS =====
                    if (user != null) {

                        user.setId(
                                rs.getInt("id")
                        );

                        user.setUsername(
                                rs.getString("username")
                        );

                        user.setPassword(
                                rs.getString("password")
                        );

                        user.setEmail(
                                rs.getString("email")
                        );

                        user.setActive(
                                rs.getBoolean("is_active")
                        );
                    }

                    return user;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }
}


