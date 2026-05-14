-- 初始化数据库表结构

-- 管理员表
CREATE TABLE IF NOT EXISTS admins (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(64) UNIQUE NOT NULL,
    device_id VARCHAR(64) NOT NULL,
    name VARCHAR(50) NOT NULL,
    birthday DATE NOT NULL,
    hour INT NOT NULL CHECK (hour >= 0 AND hour <= 23),
    minute INT NOT NULL DEFAULT 0 CHECK (minute >= 0 AND minute <= 59),
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('男', '女')),
    bazi JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_user_id ON users(user_id);
CREATE INDEX IF NOT EXISTS idx_users_device_id ON users(device_id);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- 插入默认管理员 (密码: admin123)
INSERT INTO admins (username, password)
VALUES ('admin', '$2a$10$rQXBE7qJv8XHX9Uq8XjH8eYw6qH5qH8eYw6qH5qH8eYw6qH5qH8e')
ON CONFLICT (username) DO NOTHING;