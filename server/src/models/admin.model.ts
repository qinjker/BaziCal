import { query } from '../config/database';
import bcrypt from 'bcryptjs';

export interface Admin {
  id: string;
  username: string;
  password: string;
  created_at: Date;
  updated_at: Date;
}

export interface CreateAdminParams {
  username: string;
  password: string;
}

/**
 * 根据用户名查找管理员
 */
export const findAdminByUsername = async (username: string): Promise<Admin | null> => {
  const sql = 'SELECT * FROM admins WHERE username = $1';
  const result = await query(sql, [username]);
  return result.rows[0] || null;
};

/**
 * 创建管理员
 */
export const createAdmin = async (params: CreateAdminParams): Promise<Admin> => {
  const { username, password } = params;
  const hashedPassword = await bcrypt.hash(password, 10);

  const sql = `
    INSERT INTO admins (username, password)
    VALUES ($1, $2)
    RETURNING *
  `;

  const result = await query(sql, [username, hashedPassword]);
  return result.rows[0];
};

/**
 * 验证管理员密码
 */
export const verifyAdminPassword = async (admin: Admin, password: string): Promise<boolean> => {
  return bcrypt.compare(password, admin.password);
};