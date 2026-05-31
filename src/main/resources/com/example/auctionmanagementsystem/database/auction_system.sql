CREATE DATABASE IF NOT EXISTS auction_system
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE auction_system;


CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    user_password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    balance DECIMAL(15,2) DEFAULT 0.00,
    store_name VARCHAR(100),
    rating DOUBLE DEFAULT 0.0,
    access_level VARCHAR(50),
    reset_code VARCHAR(10),
    reset_code_expiry TIMESTAMP NULL DEFAULT NULL
);
SHOW TABLES;
