CREATE TABLE IF NOT EXISTS auction (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    item_id           INT             NOT NULL,
    seller_id         INT             NOT NULL,
    current_price     DECIMAL(15,2)   NOT NULL,
    status            ENUM('PENDING','OPEN','RUNNING','FINISHED','PAID','CANCELED','REJECTED')
                      NOT NULL DEFAULT 'PENDING',
    start_time        DATETIME        NOT NULL,
    end_time          DATETIME        NOT NULL,
    highest_bidder_id INT             DEFAULT NULL,
    reject_reason     VARCHAR(500)    DEFAULT NULL,

    FOREIGN KEY (item_id)           REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id)         REFERENCES users(id),
    FOREIGN KEY (highest_bidder_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
