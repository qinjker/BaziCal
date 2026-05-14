/**
 * 八字路由
 */

import { Router } from 'express';
import { calculate, getCalendar, convertSolarToLunar } from '../controllers/bazi.controller';
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

export default router;