/**
 * 管理后台路由
 */

import { Router } from 'express';
import { login, getUsers, getFeedbacks, updateFeedbackStatus, replyFeedback, getFeedbackReplies, getMessages, createMessage, updateMessage, deleteMessage } from '../controllers/admin.controller';
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

/**
 * GET /api/v1/admin/feedbacks
 * 获取反馈列表 (需要管理员认证)
 */
router.get('/feedbacks', adminAuthMiddleware, getFeedbacks);

/**
 * PATCH /api/v1/admin/feedbacks/:id/status
 * 更新反馈状态 (需要管理员认证)
 */
router.patch('/feedbacks/:id/status', adminAuthMiddleware, updateFeedbackStatus);

/**
 * POST /api/v1/admin/feedbacks/:id/reply
 * 回复反馈 (需要管理员认证)
 */
router.post('/feedbacks/:id/reply', adminAuthMiddleware, replyFeedback);

/**
 * GET /api/v1/admin/feedbacks/:id/replies
 * 获取反馈的所有回复 (需要管理员认证)
 */
router.get('/feedbacks/:id/replies', adminAuthMiddleware, getFeedbackReplies);

/**
 * GET /api/v1/admin/messages
 * 获取寄语列表 (需要管理员认证)
 */
router.get('/messages', adminAuthMiddleware, getMessages);

/**
 * POST /api/v1/admin/messages
 * 添加寄语 (需要管理员认证)
 */
router.post('/messages', adminAuthMiddleware, createMessage);

/**
 * PUT /api/v1/admin/messages/:id
 * 更新寄语 (需要管理员认证)
 */
router.put('/messages/:id', adminAuthMiddleware, updateMessage);

/**
 * DELETE /api/v1/admin/messages/:id
 * 删除寄语 (需要管理员认证)
 */
router.delete('/messages/:id', adminAuthMiddleware, deleteMessage);

export default router;