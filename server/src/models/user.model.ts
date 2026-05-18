import { query } from '../config/database';
import { generateUserId } from '../utils/crypto';

export interface User {
  id: string;
  user_id: string;
  device_id: string;
  name: string;
  birthday: string;
  birthday_type: 'solar' | 'lunar';
  hour: number;
  minute: number;
  gender: '男' | '女';
  bazi: BaziResult | null;
  created_at: Date;
  updated_at: Date;
}

export interface BaziResult {
  year: { stem: string; branch: string };
  month: { stem: string; branch: string };
  day: { stem: string; branch: string };
  hour: { stem: string; branch: string };
  wuxing: Record<string, number>;
  shishen: Record<string, string>;
}

export interface CreateUserParams {
  device_id: string;
  name: string;
  birthday: string;
  birthday_type: 'solar' | 'lunar';
  hour: number;
  minute: number;
  gender: '男' | '女';
  bazi: BaziResult;
}

/**
 * 创建或更新用户
 */
export const createOrUpdateUser = async (params: CreateUserParams): Promise<User> => {
  const { device_id, name, birthday, birthday_type, hour, minute, gender, bazi } = params;

  // 生成用户唯一标识
  const user_id = generateUserId(device_id, name, birthday);

  const sql = `
    INSERT INTO users (user_id, device_id, name, birthday, birthday_type, hour, minute, gender, bazi)
    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
    ON CONFLICT (user_id)
    DO UPDATE SET
      device_id = EXCLUDED.device_id,
      name = EXCLUDED.name,
      birthday = EXCLUDED.birthday,
      birthday_type = EXCLUDED.birthday_type,
      hour = EXCLUDED.hour,
      minute = EXCLUDED.minute,
      gender = EXCLUDED.gender,
      bazi = EXCLUDED.bazi,
      updated_at = CURRENT_TIMESTAMP
    RETURNING *
  `;

  const result = await query(sql, [user_id, device_id, name, birthday, birthday_type, hour, minute, gender, JSON.stringify(bazi)]);
  return result.rows[0];
};

/**
 * 根据 user_id 获取用户
 */
export const findUserByUserId = async (userId: string): Promise<User | null> => {
  const sql = 'SELECT * FROM users WHERE user_id = $1';
  const result = await query(sql, [userId]);
  return result.rows[0] || null;
};

/**
 * 获取所有用户 (分页)
 */
export const findAllUsers = async (page = 1, pageSize = 20): Promise<{ users: User[]; total: number }> => {
  const offset = (page - 1) * pageSize;

  const countSql = 'SELECT COUNT(*) FROM users';
  const countResult = await query(countSql);
  const total = parseInt(countResult.rows[0].count, 10);

  const sql = `
    SELECT * FROM users
    ORDER BY created_at DESC
    LIMIT $1 OFFSET $2
  `;
  const result = await query(sql, [pageSize, offset]);

  return {
    users: result.rows,
    total,
  };
};