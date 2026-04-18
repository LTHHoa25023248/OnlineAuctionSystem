package com.example.auctionmanagementsystem.model;


public abstract class User extends Entity {
    // Đóng gói dữ liệu bằng protected/private
    protected String username;
    protected String password;
    protected String email;
    protected boolean isActive;

    public User(String username, String password, String email) {
        super(); // Gọi constructor của Entity để sinh tự động id
        this.username = username;
        this.password = password;
        this.email = email;
        this.isActive = true; // Mặc định tài khoản được kích hoạt
    }

    // Getters và Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Các lớp con bắt buộc phải ghi đè phương thức này
    public abstract void printInfo();
}
