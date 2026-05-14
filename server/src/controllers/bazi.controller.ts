/**
 * 八字控制器
 */

import { Request, Response } from 'express';
import { calculateBazi, getMonthlyCalendar, solarToLunar } from '../services/bazi.service';
import { createOrUpdateUser, findUserByUserId } from '../models/user.model';
import { success, badRequest, notFound } from '../utils/response';

export interface CalculateBody {
  name: string;
  birthday: string;
  hour: number;
  minute: number;
  gender: '男' | '女';
  device_id?: string;
}

/**
 * POST /api/v1/bazi/calculate
 * 提交生日，获取八字结果
 */
export const calculate = async (req: Request, res: Response): Promise<void> => {
  try {
    const { name, birthday, hour, minute, gender, device_id } = req.body as CalculateBody;

    // 参数验证
    if (!name || !birthday || hour === undefined || minute === undefined || !gender) {
      res.status(400).json(badRequest('Missing required fields: name, birthday, hour, minute, gender'));
      return;
    }

    if (!['男', '女'].includes(gender)) {
      res.status(400).json(badRequest('Gender must be 男 or 女'));
      return;
    }

    if (hour < 0 || hour > 23) {
      res.status(400).json(badRequest('Hour must be between 0 and 23'));
      return;
    }

    if (minute < 0 || minute > 59) {
      res.status(400).json(badRequest('Minute must be between 0 and 59'));
      return;
    }

    // 使用 Tyme 计算八字 (传入阴历日期)
    const bazi = calculateBazi(birthday, hour, minute);

    // 生成用户唯一标识 (如果没有 device_id，使用默认)
    const devId = device_id || 'anonymous';

    // 保存用户数据
    const user = await createOrUpdateUser({
      device_id: devId,
      name,
      birthday,
      hour,
      minute,
      gender,
      bazi,
    });

    res.json(success({
      userId: user.user_id,
      bazi: user.bazi,
    }));
  } catch (error) {
    console.error('Calculate bazi error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * GET /api/v1/bazi/calendar
 * 获取某月日历
 */
export const getCalendar = async (req: Request, res: Response): Promise<void> => {
  try {
    const { userId, year, month } = req.query;

    if (!userId || !year || !month) {
      res.status(400).json(badRequest('Missing required query: userId, year, month'));
      return;
    }

    const y = parseInt(year as string, 10);
    const m = parseInt(month as string, 10);

    if (isNaN(y) || isNaN(m) || m < 1 || m > 12) {
      res.status(400).json(badRequest('Invalid year or month'));
      return;
    }

    // 查找用户
    const user = await findUserByUserId(userId as string);
    if (!user || !user.bazi) {
      res.status(404).json(notFound('User not found or bazi not calculated'));
      return;
    }

    // 获取日历数据
    const days = getMonthlyCalendar(user.bazi, y, m);

    res.json(success({ days }));
  } catch (error) {
    console.error('Get calendar error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * GET /api/v1/bazi/solar-to-lunar
 * 阳历转阴历
 */
export const convertSolarToLunar = async (req: Request, res: Response): Promise<void> => {
  try {
    const { date } = req.query;

    if (!date || typeof date !== 'string') {
      res.status(400).json(badRequest('Missing required query: date'));
      return;
    }

    // 验证日期格式
    const datePattern = /^\d{4}-\d{2}-\d{2}$/;
    if (!datePattern.test(date)) {
      res.status(400).json(badRequest('Invalid date format, expected YYYY-MM-DD'));
      return;
    }

    const lunar = solarToLunar(date);
    res.json(success(lunar));
  } catch (error) {
    console.error('Solar to lunar error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};