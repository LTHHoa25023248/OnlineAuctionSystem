package com.example.auctionmanagementsystem.server.handler;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.server.JsonUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserHandler extends BaseHandler {
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();
        try {
            if (path.endsWith("/user/balance") && "GET".equals(method)) {
                handleBalance(ex);
            } else if (path.endsWith("/user/profile") && "POST".equals(method)) {
                handleUpdateProfile(ex);
            } else if (path.endsWith("/user/wins") && "GET".equals(method)) {
                handleWins(ex);
            } else {
                sendJson(ex, 404, err("Not found"));
            }
        } catch (Exception e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }
    private void handleBalance(HttpExchange ex) throws Exception {
        int userId = Integer.parseInt(queryParams(ex).getOrDefault("userId", "0"));
        try (Connection conn = DatabaseConnection.getConnection()) {
            double balance = UserDAO.getBalance(userId, conn);
            sendJson(ex, 200, Map.of("balance", balance));
        }
    }
    private void handleUpdateProfile(HttpExchange ex) throws IOException {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int userId = body.get("userId").getAsInt();
        String firstName = body.get("firstName").getAsString();
        String lastName = body.get("lastName").getAsString();
        String phone = body.get("phone").getAsString();
        boolean success = UserDAO.updateProfile(userId, firstName, lastName, phone);
        sendJson(ex, 200, success ? ok() : err("Update failed"));
    }
    private void handleWins(HttpExchange ex) throws Exception {
        int userId = Integer.parseInt(queryParams(ex).getOrDefault("userId", "0"));
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT a.id, i.name, a.current_price FROM auction a "
                + "JOIN items i ON a.item_id = i.id "
                + "WHERE a.status = 'PAID' AND a.highest_bidder_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("auctionId", rs.getInt("id"));
                    m.put("itemName", rs.getString("name"));
                    m.put("price", rs.getDouble("current_price"));
                    result.add(m);
                }
            }
        }
        sendJson(ex, 200, result);
    }
}

