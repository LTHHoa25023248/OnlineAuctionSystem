CREATE TABLE items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    starting_price DOUBLE NOT NULL,
    item_type VARCHAR(20) NOT NULL
);