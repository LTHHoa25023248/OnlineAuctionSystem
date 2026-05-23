CREATE TABLE art_items (
    item_id INT PRIMARY KEY,
    artist VARCHAR(255),
    theme VARCHAR(255),
    material VARCHAR(255),

    FOREIGN KEY (item_id) REFERENCES items(id)
    ON DELETE CASCADE
);