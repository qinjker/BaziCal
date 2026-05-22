/**
 * 路由汇总
 */

import { Router } from 'express';
import baziRoutes from './bazi';
import adminRoutes from './admin';
import { createFeedback, getMyFeedbacksHandler, getFeedbackReplies, createFeedbackReply } from '../controllers/bazi.controller';
import { signatureMiddleware } from '../middleware/signature';

const router = Router();

// API 路由前缀 /api/v1

// 八字相关路由
router.use('/bazi', baziRoutes);

// 反馈相关路由（直接挂载，不需要 /bazi 前缀）
router.post('/feedback', signatureMiddleware, createFeedback);
router.get('/feedbacks', signatureMiddleware, getMyFeedbacksHandler);
router.get('/feedbacks/:id/replies', signatureMiddleware, getFeedbackReplies);
router.post('/feedbacks/:id/replies', signatureMiddleware, createFeedbackReply);

// 管理后台路由
router.use('/admin', adminRoutes);

export default router;