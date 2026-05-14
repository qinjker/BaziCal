/**
 * 数据库初始化模块
 * 应用启动时自动检测并初始化数据库表
 */

import { pool } from './database';
import bcrypt from 'bcryptjs';
import crypto from 'crypto';

export const initDatabase = async (): Promise<void> => {
  const client = await pool.connect();

  try {
    console.log('检查数据库连接...');
    await client.query('SELECT 1');
    console.log('✓ 数据库连接正常');

    // 创建管理员表（使用自增序列代替 UUID）
    await client.query(`
      CREATE TABLE IF NOT EXISTS admins (
        id SERIAL PRIMARY KEY,
        username VARCHAR(50) UNIQUE NOT NULL,
        password VARCHAR(255) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);
    console.log('✓ 管理员表已创建/已存在');

    // 创建用户表
    await client.query(`
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
      )
    `);
    console.log('✓ 用户表已创建/已存在');

    // 迁移：添加 minute 列 (如果不存在)
    try {
      await client.query(`
        ALTER TABLE users ADD COLUMN IF NOT EXISTS minute INT NOT NULL DEFAULT 0
      `);
      console.log('✓ minute 列已添加/已存在');
    } catch (e) {
      // 忽略错误，列可能已存在
    }

    // 创建索引 (使用 IF NOT EXISTS 避免报错)
    await client.query('CREATE INDEX IF NOT EXISTS idx_users_user_id ON users(user_id)');
    await client.query('CREATE INDEX IF NOT EXISTS idx_users_device_id ON users(device_id)');
    await client.query('CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at)');
    await client.query('CREATE INDEX IF NOT EXISTS idx_admins_username ON admins(username)');
    console.log('✓ 索引已创建/已存在');

    // 创建触发器函数
    await client.query(`
      CREATE OR REPLACE FUNCTION update_updated_at_column()
      RETURNS TRIGGER AS $$
      BEGIN
        NEW.updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
      END;
      $$ language 'plpgsql'
    `);
    console.log('✓ 触发器函数已创建');

    // 创建触发器
    await client.query('DROP TRIGGER IF EXISTS update_users_updated_at ON users');
    await client.query(`
      CREATE TRIGGER update_users_updated_at
        BEFORE UPDATE ON users
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column()
    `);
    await client.query('DROP TRIGGER IF EXISTS update_admins_updated_at ON admins');
    await client.query(`
      CREATE TRIGGER update_admins_updated_at
        BEFORE UPDATE ON admins
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column()
    `);
    console.log('✓ 触发器已创建');

    // 初始化管理员账号 (只在不存在时创建)
    const hashedPassword = await bcrypt.hash('admin123', 10);
    const result = await client.query(`
      INSERT INTO admins (username, password)
      VALUES ('admin', $1)
      ON CONFLICT (username) DO NOTHING
      RETURNING id
    `, [hashedPassword]);

    if (result.rowCount && result.rowCount > 0) {
      console.log('✓ 管理员账号已创建 (admin / admin123)');
    } else {
      console.log('✓ 管理员账号已存在');
    }

    console.log('数据库初始化完成！\n');
  } catch (error) {
    console.error('数据库初始化失败:', error);
    throw error;
  } finally {
    client.release();
  }
};