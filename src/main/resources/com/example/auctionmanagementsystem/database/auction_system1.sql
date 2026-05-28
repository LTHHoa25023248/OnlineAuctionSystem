-- ═══════════════════════════════════════════════════════════════════════════
-- auction_system1.sql — Schema đầy đủ cho Auction Management System
-- Chạy file này để tạo toàn bộ DB từ đầu (thứ tự: file này → items.sql →
-- auction.sql → art_items.sql → electronics_items.sql → vehicle_items.sql →
-- bid_transaction.sql → auto_bid.sql)
-- ═══════════════════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS auction_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE auction_system;

-- ─────────────────────────────────────────────────────────────────────────────
-- Tạo user MySQL riêng cho app (chạy với quyền root một lần):
-- CREATE USER IF NOT EXISTS 'auction_system'@'localhost' IDENTIFIED BY 'password';
-- GRANT ALL PRIVILEGES ON auction_system.* TO 'auction_system'@'localhost';
-- FLUSH PRIVILEGES;
-- ─────────────────────────────────────────────────────────────────────────────


-- ═══════════════════════════════════════════════════════════════════════════
-- BẢNG USERS
-- Cột user_password (không phải "password") để khớp với UserDAO
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS users (
    id                INT            AUTO_INCREMENT PRIMARY KEY,
    first_name        VARCHAR(100)   NOT NULL DEFAULT '',
    last_name         VARCHAR(100)   NOT NULL DEFAULT '',
    username          VARCHAR(50)    NOT NULL UNIQUE,
    email             VARCHAR(255)   NOT NULL UNIQUE,
    phone             VARCHAR(30)    NOT NULL DEFAULT '',
    user_password     VARCHAR(255)   NOT NULL,
    address           VARCHAR(500)   DEFAULT '',
    role              ENUM('BIDDER','SELLER','ADMIN') NOT NULL DEFAULT 'BIDDER',
    is_active         BOOLEAN        NOT NULL DEFAULT TRUE,

    -- Riêng cho BIDDER
    balance           DECIMAL(15,2)  DEFAULT 0.00,

    -- Riêng cho SELLER
    store_name        VARCHAR(255)   DEFAULT NULL,
    rating            DECIMAL(3,2)   DEFAULT 0.00,

    -- Riêng cho ADMIN
    access_level      VARCHAR(50)    DEFAULT NULL,

    -- OTP đặt lại mật khẩu (UserDAO dùng trực tiếp trong bảng users)
    reset_code        VARCHAR(10)    DEFAULT NULL,
    reset_code_expiry TIMESTAMP      NULL DEFAULT NULL,

    created_at        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_username  (username),
    INDEX idx_email     (email),
    INDEX idx_role      (role),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ═══════════════════════════════════════════════════════════════════════════
-- DỮ LIỆU MẪU (seed data để test ngay)
-- ═══════════════════════════════════════════════════════════════════════════

-- Admin mặc định
INSERT INTO users (first_name, last_name, username, email, phone, user_password, role, access_level, is_active)
VALUES ('Super', 'Admin', 'admin', 'admin@auction.com', '0000000000', 'Admin@123', 'ADMIN', 'FULL', TRUE)
ON DUPLICATE KEY UPDATE id = id;

-- Bidder mẫu
INSERT INTO users (first_name, last_name, username, email, phone, user_password, role, balance, is_active)
VALUES ('Nguyen', 'An', 'nguyenan', 'nguyenan@example.com', '0901234567', 'Bidder@123', 'BIDDER', 5000000.00, TRUE)
ON DUPLICATE KEY UPDATE id = id;

-- Seller mẫu
INSERT INTO users (first_name, last_name, username, email, phone, user_password, role, store_name, rating, is_active)
VALUES ('Tran', 'Binh', 'tranbinh', 'tranbinh@example.com', '0912345678', 'Seller@123', 'SELLER', 'Binh Store', 4.5, TRUE)
ON DUPLICATE KEY UPDATE id = id;
