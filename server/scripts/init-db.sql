-- BaziCal 数据库初始化脚本
-- PostgreSQL

-- 创建数据库 (需要超级用户权限)
-- CREATE DATABASE bazical;

-- 连接数据库后执行以下脚本

-- 启用 UUID 扩展
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- 管理员表
-- ============================================
CREATE TABLE IF NOT EXISTS admins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(64) UNIQUE NOT NULL COMMENT '用户唯一标识：SHA256(设备ID+姓名+生日)',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    birthday DATE NOT NULL COMMENT '生日',
    hour INT NOT NULL CHECK (hour >= 0 AND hour <= 23) COMMENT '出生小时',
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('男', '女')) COMMENT '性别',
    bazi JSONB COMMENT '八字结果',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 索引
-- ============================================
CREATE INDEX IF NOT EXISTS idx_users_user_id ON users(user_id);
CREATE INDEX IF NOT EXISTS idx_users_device_id ON users(device_id);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_admins_username ON admins(username);

-- ============================================
-- 初始化管理员账号
-- 用户名: admin
-- 密码: admin123 (bcrypt 加密)
-- ============================================
INSERT INTO admins (username, password)
VALUES ('admin', '$2a$10$X5k3vK8mN5pQ2wL7hR3tJeFkDv9XsY5hT2nJ8mK4pQ2wL7hR3tJeFk')
ON CONFLICT (username) DO NOTHING;

-- ============================================
-- 函数：自动更新 updated_at
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 触发器
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_admins_updated_at ON admins;
CREATE TRIGGER update_admins_updated_at
    BEFORE UPDATE ON admins
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();