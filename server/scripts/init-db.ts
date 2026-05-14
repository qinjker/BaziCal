/**
 * 数据库初始化脚本
 * 运行方式: ts-node scripts/init-db.ts
 */

import { Pool } from 'pg';
import dotenv from 'dotenv';
import bcrypt from 'bcryptjs';

dotenv.config();

const pool = new Pool({
  connectionString: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/bazical',
});

async function initDatabase() {
  const client = await pool.connect();

  try {
    console.log('开始初始化数据库...');

    // 启用 UUID 扩展
    await client.query('CREATE EXTENSION IF NOT EXISTS "pgcrypto"');
    console.log('✓ UUID 扩展已启用');

    // 创建管理员表
    await client.query(`
      CREATE TABLE IF NOT EXISTS admins (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        username VARCHAR(50) UNIQUE NOT NULL,
        password VARCHAR(255) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);
    console.log('✓ 管理员表已创建');

    // 创建用户表
    await client.query(`
      CREATE TABLE IF NOT EXISTS users (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id VARCHAR(64) UNIQUE NOT NULL,
        device_id VARCHAR(64) NOT NULL,
        name VARCHAR(50) NOT NULL,
        birthday DATE NOT NULL,
        hour INT NOT NULL CHECK (hour >= 0 AND hour <= 23),
        gender VARCHAR(10) NOT NULL CHECK (gender IN ('男', '女')),
        bazi JSONB,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);
    console.log('✓ 用户表已创建');

    // 创建索引
    await client.query('CREATE INDEX IF NOT EXISTS idx_users_user_id ON users(user_id)');
    await client.query('CREATE INDEX IF NOT EXISTS idx_users_device_id ON users(device_id)');
    await client.query('CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at)');
    await client.query('CREATE INDEX IF NOT EXISTS idx_admins_username ON admins(username)');
    console.log('✓ 索引已创建');

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
    await client.query(`
      DROP TRIGGER IF EXISTS update_users_updated_at ON users
    `);
    await client.query(`
      CREATE TRIGGER update_users_updated_at
        BEFORE UPDATE ON users
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column()
    `);
    await client.query(`
      DROP TRIGGER IF EXISTS update_admins_updated_at ON admins
    `);
    await client.query(`
      CREATE TRIGGER update_admins_updated_at
        BEFORE UPDATE ON admins
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column()
    `);
    console.log('✓ 触发器已创建');

    // 初始化管理员账号
    const hashedPassword = await bcrypt.hash('admin123', 10);
    await client.query(`
      INSERT INTO admins (username, password)
      VALUES ('admin', $1)
      ON CONFLICT (username) DO NOTHING
    `, [hashedPassword]);
    console.log('✓ 管理员账号已创建 (admin / admin123)');

    console.log('\n数据库初始化完成！');
  } catch (error) {
    console.error('初始化失败:', error);
    throw error;
  } finally {
    client.release();
    await pool.end();
  }
}

initDatabase().catch((err) => {
  console.error(err);
  process.exit(1);
});