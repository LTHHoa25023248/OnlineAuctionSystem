package com.example.auctionmanagementsystem.model;

import java.util.UUID;

public abstract class User {
    protected String id;
    protected String username;
    protected String password;

    public User(String username, String password) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // Phương thức trừu tượng để lấy quyền
    public abstract String getRoleName();
}
