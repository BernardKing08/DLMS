CREATE TABLE IF NOT EXISTS inventory (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id          BIGINT NOT NULL,
    total_copies     INT NOT NULL,
    available_copies INT NOT NULL,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_inventory_book_id UNIQUE (book_id)
);
