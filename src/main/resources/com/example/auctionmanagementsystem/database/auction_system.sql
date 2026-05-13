-- ═══════════════════════════════════════════════════════════════════════════
-- auction_system.sql — Schema đầy đủ cho Auction Management System
-- ═══════════════════════════════════════════════════════════════════════════
-- Chạy lần đầu:
--   mysql -u root -p < auction_system.sql
--
-- Hoặc paste vào MySQL Workbench / DBeaver
-- ═══════════════════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS auction_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE auction_system;

-- ─────────────────────────────────────────────────────────────────────────────
-- Tạo user MySQL riêng cho app (an toàn hơn dùng root)
-- ─────────────────────────────────────────────────────────────────────────────
-- Chạy với quyền root:
-- CREATE USER IF NOT EXISTS 'auction_system'@'localhost' IDENTIFIED BY 'Huy2605@@';
-- GRANT ALL PRIVILEGES ON auction_system.* TO 'auction_system'@'localhost';
-- FLUSH PRIVILEGES;


-- ═══════════════════════════════════════════════════════════════════════════
-- BẢNG USERS
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS users (
                                     id           INT            AUTO_INCREMENT PRIMARY KEY,
                                     first_name   VARCHAR(100)   NOT NULL,
    last_name    VARCHAR(100)   NOT NULL,
    username     VARCHAR(50)    NOT NULL UNIQUE,
    email        VARCHAR(255)   NOT NULL UNIQUE,
    phone        VARCHAR(30)    NOT NULL DEFAULT '',
    password     VARCHAR(255)   NOT NULL,          -- plain text (nên dùng BCrypt ở production)
    address      VARCHAR(500)   DEFAULT '',         -- Đổi TEXT → VARCHAR để tránh lỗi 1101
    role         ENUM('BIDDER','SELLER','ADMIN')
    NOT NULL DEFAULT 'BIDDER',
    is_active    BOOLEAN        NOT NULL DEFAULT TRUE,

    -- Riêng cho BIDDER
    balance      DECIMAL(15,2)  DEFAULT 0.00,

    -- Riêng cho SELLER
    store_name   VARCHAR(255)   DEFAULT NULL,
    rating       DECIMAL(3,2)   DEFAULT 0.00,

    -- Riêng cho ADMIN
    access_level VARCHAR(50)    DEFAULT NULL,

    created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_username  (username),
    INDEX idx_email     (email),
    INDEX idx_role      (role),
    INDEX idx_is_active (is_active)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ═══════════════════════════════════════════════════════════════════════════
-- BẢNG PASSWORD_RESETS — Lưu OTP đặt lại mật khẩu
-- ═══════════════════════════════════════════════════════════════════════════
-- expires_at: Unix timestamp (milliseconds) — System.currentTimeMillis()
CREATE TABLE IF NOT EXISTS password_resets (
                                               id          INT           AUTO_INCREMENT PRIMARY KEY,
                                               email       VARCHAR(255)  NOT NULL,
    code        VARCHAR(10)   NOT NULL,
    expires_at  BIGINT        NOT NULL,            -- ms epoch
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_pr_email (email),
    INDEX idx_pr_code  (code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ═══════════════════════════════════════════════════════════════════════════
-- DỮ LIỆU MẪU (seed data để test ngay)
-- ═══════════════════════════════════════════════════════════════════════════

-- Admin mặc định
INSERT INTO users
(first_name, last_name, username, email, phone, password,
 role, access_level, is_active)
VALUES
    ('Super', 'Admin', 'admin', 'admin@auction.com', '0000000000', 'Admin@123',
     'ADMIN', 'FULL', TRUE)
    ON DUPLICATE KEY UPDATE id = id;  -- bỏ qua nếu đã tồn tại

-- Bidder mẫu
INSERT INTO users
(first_name, last_name, username, email, phone, password,
 role, balance, is_active)
VALUES
    ('Nguyen', 'An', 'nguyenan', 'nguyenan@example.com', '0901234567', 'Bidder@123',
     'BIDDER', 5000000.00, TRUE)
    ON DUPLICATE KEY UPDATE id = id;

-- Seller mẫu
INSERT INTO users
(first_name, last_name, username, email, phone, password,
 role, store_name, rating, is_active)
VALUES
    ('Tran', 'Bình', 'tranbinh', 'tranbinh@example.com', '0912345678', 'Seller@123',
     'SELLER', 'Binh Store', 4.5, TRUE)
    ON DUPLICATE KEY UPDATE id = id;