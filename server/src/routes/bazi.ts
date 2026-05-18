/**
 * 八字路由
 */

import { Router } from 'express';
import { calculate, getCalendar, convertSolarToLunar, getUserById, getDaily } from '../controllers/bazi.controller';
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

export default router;