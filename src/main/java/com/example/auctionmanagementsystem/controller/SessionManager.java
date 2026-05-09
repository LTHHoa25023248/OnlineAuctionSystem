package com.example.auctionmanagementsystem.controller;

/**
 * Lưu thông tin user đang đăng nhập xuyên suốt app.
 * Singleton - dùng SessionManager.getInstance() ở mọi nơi.
 */
public class SessionManager {

    private static SessionManager instance;

    private int    userId;
    private String username;
    private String email;
    private boolean isAdmin;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(int userId, String username, String email, boolean isAdmin) {
        this.userId   = userId;
        this.username = username;
        this.email    = email;
        this.isAdmin  = isAdmin;
    }

    public void logout() {
        userId   = 0;
        username = null;
        email    = null;
        isAdmin  = false;
    }

    public boolean isLoggedIn()  { return username != null; }
    public int     getUserId()   { return userId; }
    public String  getUsername() { return username; }
    public String  getEmail()    { return email; }
    public boolean isAdmin()     { return isAdmin; }
}