CREATE TABLE IF NOT EXISTS accounts (
    account_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(255),
    address       VARCHAR(255),
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_accounts_user_id UNIQUE (user_id),
    CONSTRAINT uk_accounts_phone_number UNIQUE (phone_number)
);
