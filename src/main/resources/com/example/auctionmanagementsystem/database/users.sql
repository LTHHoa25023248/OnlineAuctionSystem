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

    balance           DECIMAL(15,2)  DEFAULT 0.00,
    store_name        VARCHAR(255)   DEFAULT NULL,
    rating            DECIMAL(3,2)   DEFAULT 0.00,
    access_level      VARCHAR(50)    DEFAULT NULL,

    reset_code        VARCHAR(10)    DEFAULT NULL,
    reset_code_expiry TIMESTAMP      NULL DEFAULT NULL,

    created_at        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_username  (username),
    INDEX idx_email     (email),
    INDEX idx_role      (role),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
