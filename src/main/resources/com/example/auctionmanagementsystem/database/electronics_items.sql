CREATE TABLE electronics_items (
    item_id VARCHAR(50) PRIMARY KEY,
    brand VARCHAR(255),
    warranty_months INT,

    FOREIGN KEY (item_id) REFERENCES items(id)
    ON DELETE CASCADE
);