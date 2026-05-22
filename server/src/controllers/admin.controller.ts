/**
 * 管理后台控制器
 */

import { Request, Response } from 'express';
import { findAdminByUsername, verifyAdminPassword } from '../models/admin.model';
import { findAllUsers } from '../models/user.model';
import {
  submitFeedback,
  getFeedbacksWithReplies,
  setFeedbackStatus,
  addReply,
  getReplies,
} from '../services/feedback.service';
import { generateAdminToken } from '../middleware/auth';
import { success, unauthorized, badRequest, notFound } from '../utils/response';
import { pool } from '../config/database';

/**
 * POST /api/v1/admin/login
 * 管理员登录
 */
export const login = async (req: Request, res: Response): Promise<void> => {
  try {
    const { username, password } = req.body;

    if (!username || !password) {
      res.status(400).json(badRequest('Missing username or password'));
      return;
    }

    // 查找管理员
    const admin = await findAdminByUsername(username);
    if (!admin) {
      res.status(401).json(unauthorized('Invalid username or password'));
      return;
    }

    // 验证密码
    const isValid = await verifyAdminPassword(admin, password);
    if (!isValid) {
      res.status(401).json(unauthorized('Invalid username or password'));
      return;
    }

    // 生成 Token
    const token = generateAdminToken({
      id: admin.id,
      username: admin.username,
    });

    res.json(success({ token }));
  } catch (error) {
    console.error('Admin login error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * GET /api/v1/admin/users
 * 获取所有用户列表
 */
export const getUsers = async (req: Request, res: Response): Promise<void> => {
  try {
    const page = parseInt(req.query.page as string, 10) || 1;
    const pageSize = parseInt(req.query.pageSize as string, 10) || 20;

    const result = await findAllUsers(page, pageSize);

    res.json(success({
      users: result.users,
      total: result.total,
      page,
      pageSize,
    }));
  } catch (error) {
    console.error('Get users error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * GET /api/v1/admin/feedbacks
 * 获取反馈列表（支持分页和筛选）
 */
export const getFeedbacks = async (req: Request, res: Response): Promise<void> => {
  try {
    const page = parseInt(req.query.page as string, 10) || 1;
    const pageSize = parseInt(req.query.pageSize as string, 10) || 20;
    const status = req.query.status as string | undefined;
    const type = req.query.type as string | undefined;

    const result = await getFeedbacksWithReplies({
      page,
      pageSize,
      status: status as 'pending' | 'reviewed' | 'replied' | 'closed' | undefined,
      type: type as '功能建议' | '问题反馈' | '体验优化' | '其他' | undefined,
    });

    res.json(success({
      feedbacks: result.feedbacks,
      total: result.total,
      page,
      pageSize,
    }));
  } catch (error) {
    console.error('Get feedbacks error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * PATCH /api/v1/admin/feedbacks/:id/status
 * 更新反馈状态
 */
export const updateFeedbackStatus = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    if (!status) {
      res.status(400).json(badRequest('Missing status field'));
      return;
    }

    const validStatuses = ['pending', 'reviewed', 'replied', 'closed'];
    if (!validStatuses.includes(status)) {
      res.status(400).json(badRequest('Invalid status value'));
      return;
    }

    // 获取管理员用户名
    const adminUsername = (req as AuthRequest).admin?.username || 'admin';

    await setFeedbackStatus(id, status, adminUsername);

    res.json(success(null, '状态已更新'));
  } catch (error) {
    console.error('Update feedback status error:', error);
    const message = error instanceof Error ? error.message : 'Internal server error';
    if (message === '反馈不存在') {
      res.status(404).json(notFound(message));
    } else {
      res.status(500).json({ code: 500, message: 'Internal server error' });
    }
  }
};

/**
 * POST /api/v1/admin/feedbacks/:id/reply
 * 管理员回复反馈（兼容旧版接口）
 */
export const replyFeedback = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const { content } = req.body;

    if (!content || content.trim().length === 0) {
      res.status(400).json(badRequest('回复内容不能为空'));
      return;
    }

    // 获取管理员信息
    const admin = (req as AuthRequest).admin;
    const adminUsername = admin?.username || 'admin';
    const adminName = admin?.username || '管理员';

    await addReply({
      feedback_id: id,
      content,
      author_type: 'admin',
      author_id: adminUsername,
      author_name: adminName,
    });

    res.json(success(null, '回复成功'));
  } catch (error) {
    console.error('Reply feedback error:', error);
    const message = error instanceof Error ? error.message : 'Internal server error';
    if (message === '反馈不存在') {
      res.status(404).json(notFound(message));
    } else {
      res.status(500).json({ code: 500, message: 'Internal server error' });
    }
  }
};

/**
 * GET /api/v1/admin/feedbacks/:id/replies
 * 获取反馈的所有回复
 */
export const getFeedbackReplies = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;

    const replies = await getReplies(id);

    if (replies === null) {
      res.status(404).json(notFound('反馈不存在'));
      return;
    }

    res.json(success({ replies }));
  } catch (error) {
    console.error('Get feedback replies error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

// 类型声明用于扩展 Request
interface AuthRequest {
  admin?: {
    id: string;
    username: string;
  };
}

// ==================== 寄语管理 ====================

/**
 * GET /api/v1/admin/messages
 * 获取寄语列表
 */
export const getMessages = async (req: Request, res: Response): Promise<void> => {
  try {
    const page = parseInt(req.query.page as string, 10) || 1;
    const pageSize = parseInt(req.query.pageSize as string, 10) || 20;
    const category = req.query.category as string | undefined;

    let whereClause = '';
    const params: unknown[] = [];

    if (category) {
      whereClause = 'WHERE category = $1';
      params.push(category);
    }

    const offset = (page - 1) * pageSize;

    // 获取总数
    const countResult = await pool.query(
      `SELECT COUNT(*) as total FROM static_messages ${whereClause}`,
      params
    );
    const total = parseInt(countResult.rows[0].total, 10);

    // 获取列表
    const result = await pool.query(
      `SELECT id, content, category, created_at FROM static_messages ${whereClause} ORDER BY created_at DESC LIMIT $${params.length + 1} OFFSET $${params.length + 2}`,
      [...params, pageSize, offset]
    );

    res.json(success({
      messages: result.rows,
      total,
      page,
      pageSize,
    }));
  } catch (error) {
    console.error('Get messages error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * POST /api/v1/admin/messages
 * 添加寄语
 */
export const createMessage = async (req: Request, res: Response): Promise<void> => {
  try {
    const { content, category } = req.body;

    if (!content || content.trim().length === 0) {
      res.status(400).json(badRequest('寄语内容不能为空'));
      return;
    }

    if (content.length > 200) {
      res.status(400).json(badRequest('寄语内容不能超过200字符'));
      return;
    }

    const result = await pool.query(
      'INSERT INTO static_messages (content, category) VALUES ($1, $2) RETURNING id, content, category, created_at',
      [content.trim(), category || 'general']
    );

    res.json(success(result.rows[0], '寄语添加成功'));
  } catch (error) {
    console.error('Create message error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * PUT /api/v1/admin/messages/:id
 * 更新寄语
 */
export const updateMessage = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const { content, category } = req.body;

    if (!content || content.trim().length === 0) {
      res.status(400).json(badRequest('寄语内容不能为空'));
      return;
    }

    if (content.length > 200) {
      res.status(400).json(badRequest('寄语内容不能超过200字符'));
      return;
    }

    const result = await pool.query(
      'UPDATE static_messages SET content = $1, category = $2 WHERE id = $3 RETURNING id, content, category, created_at',
      [content.trim(), category || 'general', id]
    );

    if (result.rows.length === 0) {
      res.status(404).json(notFound('寄语不存在'));
      return;
    }

    res.json(success(result.rows[0], '寄语更新成功'));
  } catch (error) {
    console.error('Update message error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * DELETE /api/v1/admin/messages/:id
 * 删除寄语
 */
export const deleteMessage = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;

    const result = await pool.query(
      'DELETE FROM static_messages WHERE id = $1 RETURNING id',
      [id]
    );

    if (result.rows.length === 0) {
      res.status(404).json(notFound('寄语不存在'));
      return;
    }

    res.json(success(null, '寄语删除成功'));
  } catch (error) {
    console.error('Delete message error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};