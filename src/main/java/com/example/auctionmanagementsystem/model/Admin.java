package com.example.auctionmanagementsystem.model;

public class Admin extends User {
    private String accessLevel;

    public Admin(String username, String password, String email, String accessLevel) {
        super(username, password, email);
        this.accessLevel = accessLevel;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public void printInfo() {
        System.out.printf("[Admin] ID: %d | Username: %s | Access Level: %s%n", 
                getId(), username, accessLevel);
    }
}