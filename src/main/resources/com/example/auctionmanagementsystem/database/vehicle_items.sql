CREATE TABLE vehicle_items (
    item_id VARCHAR(50) PRIMARY KEY,
    year INT,
    mileage DOUBLE,

    FOREIGN KEY (item_id) REFERENCES items(id)
    ON DELETE CASCADE
);