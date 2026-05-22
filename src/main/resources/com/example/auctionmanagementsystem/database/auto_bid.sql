CREATE TABLE auto_bid (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT NOT NULL,
    bidder_id INT NOT NULL,
     max_bid DECIMAL(15,2) NOT NULL,
    increment DECIMAL(15,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auction(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
);