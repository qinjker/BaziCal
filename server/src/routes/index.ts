/**
 * 路由汇总
 */

import { Router } from 'express';
import baziRoutes from './bazi';
import adminRoutes from './admin';

const router = Router();

router.use('/bazi', baziRoutes);
router.use('/admin', adminRoutes);

export default router;