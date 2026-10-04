CREATE TABLE IF NOT EXISTS inventory (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id          BIGINT NOT NULL,
    total_copies     INT NOT NULL,
    available_copies INT NOT NULL,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_inventory_book_id UNIQUE (book_id)
);

CREATE TABLE IF NOT EXISTS borrow_records (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    book_id          BIGINT NOT NULL,
    pickup_location  VARCHAR(255) NOT NULL,
    notes            TEXT,
    status           VARCHAR(50) NOT NULL,
    borrow_date      DATETIME NOT NULL,
    due_date         DATETIME NOT NULL,
    return_date      DATETIME NULL
);
