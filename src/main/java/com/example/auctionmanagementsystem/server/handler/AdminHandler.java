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

public class AdminHandler extends BaseHandler {
    @Override
    public void handle(HttpExchange ex) throws IOException {
        //lay duong dan URL cua request
        String path = ex.getRequestURI().getPath();
        try {
            //xem thong ke tong quan, auction, nguoi dung,
            if (path.endsWith("/admin/stats")) {
                handleStats(ex);
            } else if (path.endsWith("/admin/auctions")) {
                handleAuctions(ex);
            } else if (path.endsWith("/admin/users")) {
                handleUsers(ex);
            } else if (path.endsWith("/admin/user/ban")) {
                handleBanUser(ex);
            } else {
                //ko tim thay url dung
                sendJson(ex, 404, err("Not found"));
            }
        } catch (Exception e) {
            // moi loi ruutime deu duoc bat tap trung o day va tra ve HTTP 500
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

   // thong ke tong quan gom: totalListing, activeAuction, totalUser,revenue
    private void handleStats(HttpExchange ex) throws Exception {
       //dung mang luu du lieu
        int[] s = new int[3];
        double adminRevenue = 0;
        try (Connection connect = DatabaseConnection.getConnection()) {
            //dem so phien dau gia
            try (PreparedStatement ps = connect.prepareStatement("SELECT COUNT(*) FROM auction");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) s[0] = rs.getInt(1);
            }
            //auction hoat dong
            try (PreparedStatement ps = connect.prepareStatement(
                    "SELECT COUNT(*) FROM auction WHERE status='OPEN'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) s[1] = rs.getInt(1);
            }

            // so tai khoan hien co
            try (PreparedStatement ps = connect.prepareStatement("SELECT COUNT(*) FROM users");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) s[2] = rs.getInt(1);
            }
            //revenue
            try (PreparedStatement ps = connect.prepareStatement(
                    "SELECT COALESCE(balance, 0) FROM users WHERE role='ADMIN' LIMIT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) adminRevenue = rs.getDouble(1);
            }
        }

        // tra ve bang json
        sendJson(ex, 200, Map.of(
                "totalListings",  s[0],
                "activeAuctions", s[1],
                "totalUsers",     s[2],
                "revenue",        adminRevenue));
    }
    private void handleAuctions(HttpExchange ex) throws Exception {
        //doc tham so filter tu query string, mac dinh la All neu khong co
        String filter = queryParams(ex).getOrDefault("filter", "ALL");
        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT a.id, i.name, i.item_type, u.username, a.current_price, a.status, a.reject_reason " + "FROM auction a " + "LEFT JOIN items i ON a.item_id = i.id " );
        // whereParam la gia tri truyen vao ?
        String whereParam = null;
        switch (filter) {
            case "PENDING":
                // loc phien cho duyet
                sql.append("WHERE a.status = ? ");
                whereParam = "PENDING";
                break;
            case "ELECTRONICS":
                // loc theo vat pham dien tu
                sql.append("WHERE i.item_type = ? ");
                whereParam = "ELECTRONICS";
                break;
            case "ART":
                // loc theo vat pham nghe thuat
                sql.append("WHERE i.item_type = ? ");
                whereParam = "ART";
                break;
            case "VEHICLE":
                // loc theo phuong tien
                sql.append("WHERE i.item_type = ? ");
                whereParam = "VEHICLE";
                break;
            default:
                // ko loc - lay tat ca cac phien
                break;
        }
        // sap xep theo ID giam dan, new->old, gioi han 200 ket qua
        sql.append("ORDER BY a.id DESC LIMIT 200");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (whereParam != null) ps.setString(1, whereParam);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("item_type");
                    String cat = "VEHICLE".equals(type) ? "Vehicle" : "ART".equals(type)     ? "Art" : "Electronics";
                    // dong goi du lieu o moi hang vao Map de chuyen thanh Json
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",       rs.getInt("id"));
                    m.put("name",     rs.getString("name") != null ? rs.getString("name") : "N/A");
                    m.put("category", cat);
                    m.put("seller",   rs.getString("username") != null ? rs.getString("username") : "N/A");
                    m.put("price",    String.format("%,.2f USD", rs.getDouble("current_price")));
                    m.put("status",   rs.getString("status"));
                   // tra ve ly do tu choi
                    String rej = rs.getString("reject_reason");
                    m.put("rejectReason", rej != null ? rej : "");
                    result.add(m);
                }
            }
        }
        // tra ve mang JSON chua toan bo phien dau gia da loc
        sendJson(ex, 200, result);
    }

    private void handleUsers(HttpExchange ex) throws Exception {
        //Doc tham so filter tu query string
        String filter = queryParams(ex).getOrDefault("filter", "ALL");
        String sql;
        switch (filter) {
            case "Active":
                sql = "SELECT id,username,email,role,is_active,created_at FROM users WHERE is_active=true ORDER BY id";
                break;
            case "Admin":
                sql = "SELECT id,username,email,role,is_active,created_at FROM users WHERE role='ADMIN' ORDER BY id";
                break;
            default:
                //lay tat ca nguoi dung
                sql = "SELECT id,username,email,role,is_active,created_at FROM users ORDER BY id";
                break;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id",       rs.getInt("id"));
                m.put("username", rs.getString("username"));
                m.put("email",    rs.getString("email"));
                m.put("role",     rs.getString("role"));
                m.put("status",   rs.getBoolean("is_active") ? "Active" : "Banned");
                m.put("joinDate", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime().toLocalDate().toString() : "N/A");
                result.add(m);
            }
        }
        sendJson(ex, 200, result);
    }

    private void handleBanUser(HttpExchange ex) throws Exception {
        //doc va parse body Json tu request
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int userId = body.get("userId").getAsInt();
        // true = mo, false=khoa
        boolean active = body.get("active").getAsBoolean();
        //goi DAO de cap nhat du lieu
        boolean ok = UserDAO.setActive(userId, active);
        sendJson(ex, 200, Map.of("success", ok));
    }
}
