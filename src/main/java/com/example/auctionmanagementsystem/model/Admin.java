package com.example.auctionmanagementsystem.model;

public class Admin extends User {
    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRoleName() {
        return "ADMIN";
    }

    public void manageSystem() {
        System.out.println(username + " đang quản lý hệ thống...");
    }
}