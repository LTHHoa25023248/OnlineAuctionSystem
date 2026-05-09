package com.example.auctionmanagementsystem.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL =
            "jdbc:mysql://localhost:3306/auction_system";

    private static final String USER = "auction_system";

    private static final String PASSWORD ="Huy2605@@";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

            throw new RuntimeException("Cannot load MySQL Driver");
        }
    }

    // mỗi lần gọi tạo connection mới
    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
