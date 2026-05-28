package com.example.auctionmanagementsystem.observer;

public interface Observer {
    /**
     * Phương thức được gọi khi Subject có sự thay đổi.
     * @param message Thông điệp chứa thông tin cập nhật (có thể đổi thành Object nếu muốn truyền dữ liệu phức tạp hơn)
     */
    void update(String message);
}
