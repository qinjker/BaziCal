/**
 * 反馈数据模型
 */

import { query } from '../config/database';

export type FeedbackType = '功能建议' | '问题反馈' | '体验优化' | '其他';
export type FeedbackStatus = 'pending' | 'reviewed' | 'replied' | 'closed';
export type AuthorType = 'user' | 'admin';

export interface Feedback {
  id: string;
  type: FeedbackType;
  content: string;
  contact?: string;
  user_id?: string;
  device_id?: string;
  status: FeedbackStatus;
  created_at: Date;
  updated_at: Date;
}

export interface FeedbackReply {
  id: string;
  feedback_id: string;
  content: string;
  author_type: AuthorType;
  author_id: string;
  author_name: string;
  created_at: Date;
}

export interface CreateFeedbackParams {
  type: FeedbackType;
  content: string;
  contact?: string;
  user_id?: string;
  device_id?: string;
}

export interface CreateReplyParams {
  feedback_id: string;
  content: string;
  author_type: AuthorType;
  author_id: string;
  author_name: string;
}

export interface FindFeedbacksParams {
  page?: number;
  pageSize?: number;
  status?: FeedbackStatus;
  type?: FeedbackType;
}

export interface FindFeedbacksByUserIdParams {
  page?: number;
  pageSize?: number;
  user_id: string;
}

/**
 * 创建反馈
 */
export const createFeedback = async (params: CreateFeedbackParams): Promise<Feedback> => {
  const { type, content, contact, user_id, device_id } = params;

  const sql = `
    INSERT INTO feedback (type, content, contact, user_id, device_id, status)
    VALUES ($1, $2, $3, $4, $5, 'pending')
    RETURNING *
  `;

  const result = await query(sql, [type, content, contact || null, user_id || null, device_id || null]);
  return result.rows[0];
};

/**
 * 根据 ID 获取反馈
 */
export const findFeedbackById = async (id: string): Promise<Feedback | null> => {
  const sql = 'SELECT * FROM feedback WHERE id = $1';
  const result = await query(sql, [id]);
  return result.rows[0] || null;
};

/**
 * 获取反馈列表（分页，支持筛选）
 */
export const findAllFeedbacks = async (params: FindFeedbacksParams = {}): Promise<{ feedbacks: Feedback[]; total: number }> => {
  const { page = 1, pageSize = 20, status, type } = params;
  const offset = (page - 1) * pageSize;

  // 构建 WHERE 条件
  const conditions: string[] = [];
  const values: unknown[] = [];
  let paramIndex = 1;

  if (status) {
    conditions.push(`status = $${paramIndex}`);
    values.push(status);
    paramIndex++;
  }

  if (type) {
    conditions.push(`type = $${paramIndex}`);
    values.push(type);
    paramIndex++;
  }

  const whereClause = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';

  // 查询总数
  const countSql = `SELECT COUNT(*) FROM feedback ${whereClause}`;
  const countResult = await query(countSql, values);
  const total = parseInt(countResult.rows[0].count, 10);

  // 查询列表
  const listSql = `
    SELECT * FROM feedback
    ${whereClause}
    ORDER BY created_at DESC
    LIMIT $${paramIndex} OFFSET $${paramIndex + 1}
  `;

  values.push(pageSize, offset);
  const result = await query(listSql, values);

  return {
    feedbacks: result.rows,
    total,
  };
};

/**
 * 更新反馈状态
 */
export const updateFeedbackStatus = async (id: string, status: FeedbackStatus): Promise<Feedback | null> => {
  const sql = `
    UPDATE feedback
    SET status = $1, updated_at = CURRENT_TIMESTAMP
    WHERE id = $2
    RETURNING *
  `;

  const result = await query(sql, [status, id]);
  return result.rows[0] || null;
};

/**
 * 创建回复
 */
export const createReply = async (params: CreateReplyParams): Promise<FeedbackReply> => {
  const { feedback_id, content, author_type, author_id, author_name } = params;

  const sql = `
    INSERT INTO feedback_replies (feedback_id, content, author_type, author_id, author_name)
    VALUES ($1, $2, $3, $4, $5)
    RETURNING *
  `;

  const result = await query(sql, [feedback_id, content, author_type, author_id, author_name]);

  // 如果有回复，自动更新反馈状态为 replied
  await query(
    `UPDATE feedback SET status = 'replied', updated_at = CURRENT_TIMESTAMP WHERE id = $1 AND status = 'pending'`,
    [feedback_id]
  );

  return result.rows[0];
};

/**
 * 获取反馈的所有回复
 */
export const findRepliesByFeedbackId = async (feedbackId: string): Promise<FeedbackReply[]> => {
  const sql = `
    SELECT * FROM feedback_replies
    WHERE feedback_id = $1
    ORDER BY created_at ASC
  `;

  const result = await query(sql, [feedbackId]);
  return result.rows;
};

/**
 * 根据 ID 删除回复
 */
export const deleteReplyById = async (id: string): Promise<boolean> => {
  const sql = 'DELETE FROM feedback_replies WHERE id = $1';
  const result = await query(sql, [id]);
  return (result.rowCount ?? 0) > 0;
};

/**
 * 获取用户自己的反馈列表（带最新回复预览和回复数量）
 */
export const findFeedbacksByUserId = async (params: FindFeedbacksByUserIdParams): Promise<{ feedbacks: Array<{
  id: string;
  type: FeedbackType;
  content: string;
  contact?: string;
  status: FeedbackStatus;
  latestReply?: {
    author_type: AuthorType;
    author_name: string;
    content: string;
  };
  replyCount: number;
  created_at: Date;
}>; total: number }> => {
  const { page = 1, pageSize = 20, user_id } = params;
  const offset = (page - 1) * pageSize;

  // 查询该用户的反馈总数（支持 user_id 或 device_id）
  const countSql = 'SELECT COUNT(*) FROM feedback WHERE (user_id = $1 OR device_id = $1)';
  const countResult = await query(countSql, [user_id]);
  const total = parseInt(countResult.rows[0].count, 10);

  // 查询反馈列表，并关联获取最新回复和回复数量
  const sql = `
    SELECT
      f.id,
      f.type,
      f.content,
      f.contact,
      f.status,
      f.created_at,
      (
        SELECT row_to_json(r)
        FROM feedback_replies r
        WHERE r.feedback_id = f.id
        ORDER BY r.created_at DESC
        LIMIT 1
      ) as latest_reply,
      (
        SELECT COUNT(*)::int
        FROM feedback_replies
        WHERE feedback_id = f.id
      ) as reply_count
    FROM feedback f
    WHERE f.user_id = $1 OR f.device_id = $1
    ORDER BY f.created_at DESC
    LIMIT $2 OFFSET $3
  `;

  const result = await query(sql, [user_id, pageSize, offset]);

  const feedbacks = result.rows.map(row => ({
    id: row.id,
    type: row.type,
    content: row.content,
    contact: row.contact,
    status: row.status,
    created_at: row.created_at,
    replyCount: row.reply_count,
    latestReply: row.latest_reply ? {
      author_type: row.latest_reply.author_type,
      author_name: row.latest_reply.author_name,
      content: row.latest_reply.content,
    } : undefined,
  }));

  return { feedbacks, total };
};