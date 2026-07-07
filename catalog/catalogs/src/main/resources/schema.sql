CREATE TABLE IF NOT EXISTS books (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    author      VARCHAR(255) NOT NULL,
    isbn        VARCHAR(255) NOT NULL,
    category    VARCHAR(255),
    description VARCHAR(2000),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_books_isbn UNIQUE (isbn)
);
