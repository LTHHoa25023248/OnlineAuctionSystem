package com.example.auctionmanagementsystem.controller;

public class SessionManager {

  private static SessionManager instance;

  private int userId;
  private String username;
  private String email;
  private String phone;
  private String firstName;
  private String lastName;
  private boolean isAdmin;
  private String role; // "BIDDER" / "SELLER" / "ADMIN"

  private SessionManager() {}

  public static SessionManager getInstance() {
    if (instance == null)
      instance = new SessionManager();
    return instance;
  }

  // Hàm login đầy đủ — dùng từ LoginController
  public void login(int userId, String username, String email, String phone, String firstName,
      String lastName, boolean isAdmin, String role) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.phone = phone != null ? phone : "";
    this.firstName = firstName != null ? firstName : "";
    this.lastName = lastName != null ? lastName : "";
    this.isAdmin = isAdmin;
    this.role = role != null ? role : "BIDDER";
  }

  // Overload cũ — giữ để không vỡ code khác đang dùng
  public void login(int userId, String username, String email, boolean isAdmin) {
    this.login(userId, username, email, "", "", "", isAdmin, isAdmin ? "ADMIN" : "BIDDER");
  }

  public void logout() {
    userId = 0;
    username = null;
    email = null;
    phone = null;
    firstName = null;
    lastName = null;
    isAdmin = false;
    role = null;
  }

  public boolean isLoggedIn() {
    return username != null;
  }

  public int getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone != null ? phone : "";
  }

  public String getFirstName() {
    return firstName != null ? firstName : "";
  }

  public String getLastName() {
    return lastName != null ? lastName : "";
  }

  public String getFullName() {
    return (getFirstName() + " " + getLastName()).trim();
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public boolean isSeller() {
    return "SELLER".equals(role);
  }

  public boolean isBidder() {
    return "BIDDER".equals(role);
  }

  public String getRole() {
    return role != null ? role : "BIDDER";
  }
}
