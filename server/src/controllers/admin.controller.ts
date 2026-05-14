/**
 * 管理后台控制器
 */

import { Request, Response } from 'express';
import { findAdminByUsername, verifyAdminPassword } from '../models/admin.model';
import { findAllUsers } from '../models/user.model';
import { generateAdminToken } from '../middleware/auth';
import { success, unauthorized, badRequest } from '../utils/response';

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