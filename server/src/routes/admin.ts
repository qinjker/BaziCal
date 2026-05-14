/**
 * 管理后台路由
 */

import { Router } from 'express';
import { login, getUsers } from '../controllers/admin.controller';
import { adminAuthMiddleware } from '../middleware/auth';

const router = Router();

/**
 * POST /api/v1/admin/login
 * 管理员登录 (无需认证)
 */
router.post('/login', login);

/**
 * GET /api/v1/admin/users
 * 获取所有用户列表 (需要管理员认证)
 */
router.get('/users', adminAuthMiddleware, getUsers);

export default router;