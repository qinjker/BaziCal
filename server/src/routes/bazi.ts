/**
 * 八字路由
 */

import { Router } from 'express';
import { calculate, getCalendar, convertSolarToLunar, getUserById, getDaily, createFeedback, getFeedbackReplies, createFeedbackReply, getMyFeedbacksHandler, getDailyMessage, getFeedbackDetail } from '../controllers/bazi.controller';
import { signatureMiddleware } from '../middleware/signature';

const router = Router();

// 所有路由需要签名验证
router.use(signatureMiddleware);

/**
 * POST /api/v1/bazi/calculate
 * 提交生日，获取八字结果
 */
router.post('/calculate', calculate);

/**
 * GET /api/v1/bazi/calendar
 * 获取某月日历
 */
router.get('/calendar', getCalendar);

/**
 * GET /api/v1/bazi/solar-to-lunar
 * 阳历转阴历
 */
router.get('/solar-to-lunar', convertSolarToLunar);

/**
 * GET /api/v1/bazi/user/:userId
 * 获取用户详细信息
 */
router.get('/user/:userId', getUserById);

/**
 * GET /api/v1/bazi/daily/:date
 * 获取指定日期的详细信息
 */
router.get('/daily/:date', getDaily);

/**
 * POST /api/v1/bazi/daily-message
 * 获取每日寄语
 */
router.post('/daily-message', getDailyMessage);

/**
 * POST /api/v1/feedback
 * 用户提交反馈
 */
router.post('/feedback', createFeedback);

/**
 * GET /api/v1/feedbacks
 * 获取我的反馈列表
 */
router.get('/feedbacks', getMyFeedbacksHandler);

/**
 * GET /api/v1/feedbacks/:id/replies
 * 获取反馈的回复列表
 */
router.get('/feedbacks/:id/replies', getFeedbackReplies);

/**
 * POST /api/v1/feedbacks/:id/replies
 * 用户添加回复
 */
router.post('/feedbacks/:id/replies', createFeedbackReply);

/**
 * GET /api/v1/feedbacks/:id
 * 获取单条反馈详情
 */
router.get('/feedbacks/:id', getFeedbackDetail);

export default router;