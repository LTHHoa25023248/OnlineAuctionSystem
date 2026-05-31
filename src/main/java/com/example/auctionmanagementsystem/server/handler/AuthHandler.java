package com.example.auctionmanagementsystem.server.handler;

import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.model.Admin;
import com.example.auctionmanagementsystem.model.Seller;
import com.example.auctionmanagementsystem.model.User;
import com.example.auctionmanagementsystem.server.JsonUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthHandler extends BaseHandler {
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();
        try {
            if (path.endsWith("/auth/login") && "POST".equals(method)) {
                handleLogin(ex);
            } else if (path.endsWith("/auth/register") && "POST".equals(method)) {
                handleRegister(ex);
            } else if (path.endsWith("/auth/check-username") && "GET".equals(method)) {
                sendJson(ex, 200, Map.of("exists", UserDAO.usernameExists(queryParams(ex).getOrDefault("username", ""))));
            } else if (path.endsWith("/auth/check-email") && "GET".equals(method)) {
                sendJson(ex, 200, Map.of("exists", UserDAO.emailExists(queryParams(ex).getOrDefault("email", ""))));
            } else {
                sendJson(ex, 404, err("Not found"));
            }
        } catch (Exception e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }

    private void handleLogin(HttpExchange ex) throws IOException {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        String username = body.get("username").getAsString();
        String password = body.get("password").getAsString();
        User user = UserDAO.login(username, password);
        if (user == null) {
            sendJson(ex, 200, Map.of("success", false));
            return;
        }
        String phone = UserDAO.getStringField(user.getId(), "phone");
        String firstName = UserDAO.getStringField(user.getId(), "first_name");
        String lastName = UserDAO.getStringField(user.getId(), "last_name");
        String role = (user instanceof Admin) ? "ADMIN"
                : (user instanceof Seller) ? "SELLER" : "BIDDER";
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("userId", user.getId());
        resp.put("username", user.getUsername());
        resp.put("email", user.getEmail());
        resp.put("phone", phone != null ? phone : "");
        resp.put("firstName", firstName != null ? firstName : "");
        resp.put("lastName", lastName != null ? lastName : "");
        resp.put("role", role);
        resp.put("isAdmin", user instanceof Admin);
        sendJson(ex, 200, resp);
    }

    private void handleRegister(HttpExchange ex) throws IOException {
        JsonObject b = JsonUtil.parseObject(readBody(ex));
        String firstName = b.get("firstName").getAsString();
        String lastName = b.get("lastName").getAsString();
        String username = b.get("username").getAsString();
        String email = b.get("email").getAsString();
        String phone = b.get("phone").getAsString();
        String password = b.get("password").getAsString();
        String address = b.get("address").getAsString();
        boolean seller = b.get("seller").getAsBoolean();
        if (UserDAO.usernameExists(username)) {
            sendJson(ex, 200, Map.of("result", "USERNAME_TAKEN"));
            return;
        }
        if (UserDAO.emailExists(email)) {
            sendJson(ex, 200, Map.of("result", "EMAIL_TAKEN"));
            return;
        }
        boolean success = seller
                ? UserDAO.registerSeller(firstName, lastName, username, email, phone, password, address)
                : UserDAO.register(firstName, lastName, username, email, phone, password, address);
        sendJson(ex, 200, Map.of("result", success ? "SUCCESS" : "DB_ERROR"));
    }
}
