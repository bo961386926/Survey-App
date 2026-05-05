-- 测试数据库Schema（H2兼容）

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100),
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status INT DEFAULT 1,
    dept_id BIGINT,
    role_type VARCHAR(50),
    first_login INT DEFAULT 1,
    login_warning INT DEFAULT 0,
    last_login_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS captcha (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    captcha_key VARCHAR(100) NOT NULL UNIQUE,
    captcha_code VARCHAR(10),
    expire_time TIMESTAMP
);
