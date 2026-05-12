package com.example.auctionmanagementsystem.controller;

/**
 * SessionManager — Singleton lưu thông tin user đang đăng nhập.
 *
 * Cách dùng:
 *   SessionManager.getInstance().login(...)   → sau khi đăng nhập thành công
 *   SessionManager.getInstance().getUsername() → lấy tên hiển thị
 *   SessionManager.getInstance().isAdmin()     → kiểm tra quyền admin
 *   SessionManager.getInstance().logout()      → xóa session khi đăng xuất
 *
 * Vì là Singleton, dữ liệu tồn tại xuyên suốt vòng đời app,
 * không mất khi chuyển màn hình.
 */
public class SessionManager {

    // Instance duy nhất của class (Singleton pattern)
    private static SessionManager instance;

    private int     userId;
    private String  username;
    private String  email;
    private boolean isAdmin;

    // Constructor private — không cho tạo instance từ bên ngoài
    private SessionManager() {}

    /**
     * Lấy instance duy nhất. Tạo mới nếu chưa có.
     */
    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    /**
     * Gọi sau khi xác thực đăng nhập thành công.
     *
     * @param userId   ID user trong database
     * @param username Tên đăng nhập
     * @param email    Email của user
     * @param isAdmin  true nếu user có quyền admin
     */
    public void login(int userId, String username, String email, boolean isAdmin) {
        this.userId   = userId;
        this.username = username;
        this.email    = email;
        this.isAdmin  = isAdmin;
    }

    /**
     * Xóa toàn bộ thông tin session — gọi khi user đăng xuất.
     */
    public void logout() {
        userId   = 0;
        username = null;
        email    = null;
        isAdmin  = false;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    /** @return true nếu có user đang đăng nhập */
    public boolean isLoggedIn()  { return username != null; }

    public int     getUserId()   { return userId; }
    public String  getUsername() { return username; }
    public String  getEmail()    { return email; }

    /** @return true nếu user hiện tại có quyền admin */
    public boolean isAdmin()     { return isAdmin; }
}