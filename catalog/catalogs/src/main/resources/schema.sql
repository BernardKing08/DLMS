CREATE TABLE IF NOT EXISTS books (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    author          VARCHAR(255) NOT NULL,
    isbn            VARCHAR(255) NOT NULL,
    category        VARCHAR(255),
    description     VARCHAR(2000),
    cover_image_url VARCHAR(500),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_books_isbn UNIQUE (isbn)
);

-- CREATE TABLE IF NOT EXISTS is a no-op against a table that already exists
-- from an earlier run (no migration tool in this project) - this ALTER
-- backfills the column on those pre-existing tables. No-op either way once
-- it's already present, whether from a fresh CREATE or a prior run of this
-- same ALTER.
ALTER TABLE books ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(500);
