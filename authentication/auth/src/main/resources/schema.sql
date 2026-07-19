CREATE TABLE IF NOT EXISTS roles (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_roles_role_name UNIQUE (role_name)
);

CREATE TABLE IF NOT EXISTS users (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    email   VARCHAR(100) NOT NULL,
    pwd     VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);
