CREATE DATABASE auction_system;
USE auction_system;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    user_password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) NOT NULL,
    balance DOUBLE DEFAULT 0.0,
    store_name VARCHAR(100),
    rating DOUBLE DEFAULT 0.0,
    access_level VARCHAR(50)
);
SHOW TABLES;