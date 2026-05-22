/**
 * 八字控制器
 */

import { Request, Response } from 'express';
import { calculateBazi, getMonthlyCalendar, solarToLunar, getDailyDetail } from '../services/bazi.service';
import { createOrUpdateUser, findUserByUserId } from '../models/user.model';
import { submitFeedback, getReplies, addReply, getMyFeedbacks, getFeedbackWithReplies } from '../services/feedback.service';
import { getDailyMessages } from '../llm/message.service';
import { success, badRequest, notFound } from '../utils/response';

export interface CalculateBody {
  name: string;
  birthday: string;
  birthday_type: 'solar' | 'lunar';
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
    const { name, birthday, birthday_type, hour, minute, gender, device_id } = req.body as CalculateBody;

    // 参数验证
    if (!name || !birthday || hour === undefined || minute === undefined || !gender) {
      res.status(400).json(badRequest('Missing required fields: name, birthday, hour, minute, gender'));
      return;
    }

    // birthday_type 验证，默认值 'solar'
    const birthType = birthday_type || 'solar';
    if (!['solar', 'lunar'].includes(birthType)) {
      res.status(400).json(badRequest('birthday_type must be solar or lunar'));
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

    // 使用 Tyme 计算八字 (根据 birthday_type 判断是否需要转换)
    const bazi = calculateBazi(birthday, hour, minute, birthType);

    // 生成用户唯一标识 (如果没有 device_id，使用默认)
    const devId = device_id || 'anonymous';

    // 保存用户数据
    const user = await createOrUpdateUser({
      device_id: devId,
      name,
      birthday,
      birthday_type: birthType,
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

/**
 * GET /api/v1/bazi/user/:userId
 * 获取用户详细信息
 */
export const getUserById = async (req: Request, res: Response): Promise<void> => {
  try {
    const { userId } = req.params;

    if (!userId) {
      res.status(400).json(badRequest('Missing required param: userId'));
      return;
    }

    const user = await findUserByUserId(userId);
    if (!user) {
      res.status(404).json(notFound('User not found'));
      return;
    }

    res.json(success({
      userId: user.user_id,
      name: user.name,
      birthday: user.birthday,
      birthday_type: user.birthday_type,
      hour: user.hour,
      minute: user.minute,
      gender: user.gender,
      bazi: user.bazi,
    }));
  } catch (error) {
    console.error('Get user error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * GET /api/v1/bazi/daily/:date
 * 获取指定日期的详细信息
 */
export const getDaily = async (req: Request, res: Response): Promise<void> => {
  try {
    const { date } = req.params;
    const { userId } = req.query;

    if (!date) {
      res.status(400).json(badRequest('Missing required param: date'));
      return;
    }

    if (!userId || typeof userId !== 'string') {
      res.status(400).json(badRequest('Missing required query: userId'));
      return;
    }

    // 验证日期格式
    const datePattern = /^\d{4}-\d{2}-\d{2}$/;
    if (!datePattern.test(date)) {
      res.status(400).json(badRequest('Invalid date format, expected YYYY-MM-DD'));
      return;
    }

    // 查找用户
    const user = await findUserByUserId(userId);
    if (!user || !user.bazi) {
      res.status(404).json(notFound('User not found or bazi not calculated'));
      return;
    }

    // 获取每日详情
    const detail = getDailyDetail(user.bazi, date);
    res.json(success(detail));
  } catch (error) {
    console.error('Get daily error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

export interface FeedbackBody {
  type: '功能建议' | '问题反馈' | '体验优化' | '其他';
  content: string;
  contact?: string;
  device_id?: string;
}

/**
 * POST /api/v1/feedback
 * 用户提交反馈
 */
export const createFeedback = async (req: Request, res: Response): Promise<void> => {
  try {
    const { type, content, contact, device_id } = req.body as FeedbackBody;

    // 参数验证
    if (!type || !['功能建议', '问题反馈', '体验优化', '其他'].includes(type)) {
      res.status(400).json(badRequest('无效的反馈类型'));
      return;
    }

    if (!content || content.trim().length === 0) {
      res.status(400).json(badRequest('反馈内容不能为空'));
      return;
    }

    if (content.length > 500) {
      res.status(400).json(badRequest('反馈内容不能超过500字符'));
      return;
    }

    await submitFeedback({
      type,
      content: content.trim(),
      contact,
      device_id,
    });

    res.json(success(null, '反馈已提交，感谢您的意见！'));
  } catch (error) {
    console.error('Create feedback error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * GET /api/v1/feedbacks/:id/replies
 * 获取反馈的回复列表
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

export interface AddReplyBody {
  content: string;
}

/**
 * POST /api/v1/feedbacks/:id/replies
 * 用户添加回复
 */
export const createFeedbackReply = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const { content } = req.body as AddReplyBody;

    if (!content || content.trim().length === 0) {
      res.status(400).json(badRequest('回复内容不能为空'));
      return;
    }

    if (content.length > 1000) {
      res.status(400).json(badRequest('回复内容不能超过1000字符'));
      return;
    }

    await addReply({
      feedback_id: id,
      content: content.trim(),
      author_type: 'user',
      author_id: 'anonymous',
      author_name: '用户',
    });

    res.json(success(null, '回复成功'));
  } catch (error) {
    console.error('Create feedback reply error:', error);
    const message = error instanceof Error ? error.message : 'Internal server error';
    if (message === '反馈不存在') {
      res.status(404).json(notFound(message));
    } else {
      res.status(500).json({ code: 500, message: 'Internal server error' });
    }
  }
};

/**
 * GET /api/v1/feedbacks
 * 获取我的反馈列表
 */
export const getMyFeedbacksHandler = async (req: Request, res: Response): Promise<void> => {
  try {
    const page = parseInt(req.query.page as string, 10) || 1;
    const pageSize = parseInt(req.query.pageSize as string, 10) || 20;

    // 从签名验证中间件获取 user_id，或从 query/device_id 获取
    const userId = (req as any).user_id || req.query.user_id;
    const deviceId = req.query.device_id;

    if (!userId && !deviceId) {
      res.status(400).json(badRequest('Missing user_id or device_id'));
      return;
    }

    // 优先使用 user_id，如果为空则使用 device_id
    const identifier = userId || deviceId;
    const result = await getMyFeedbacks({ page, pageSize, user_id: identifier });

    res.json(success({
      feedbacks: result.feedbacks,
      total: result.total,
      page,
      pageSize,
    }));
  } catch (error) {
    console.error('Get my feedbacks error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

/**
 * GET /api/v1/feedbacks/:id
 * 获取单条反馈详情（含回复）
 */
export const getFeedbackDetail = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const result = await getFeedbackWithReplies(id);

    if (!result) {
      res.status(404).json(notFound('反馈不存在'));
      return;
    }

    res.json(success(result));
  } catch (error) {
    console.error('Get feedback detail error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};

export interface DailyMessageBody {
  birthday: string;
  date: string;
}

/**
 * POST /api/v1/bazi/daily-message
 * 获取每日寄语
 */
export const getDailyMessage = async (req: Request, res: Response): Promise<void> => {
  try {
    const { birthday, date } = req.body as DailyMessageBody;

    // 参数验证
    if (!birthday || !date) {
      res.status(400).json(badRequest('Missing required fields: birthday, date'));
      return;
    }

    // 验证日期格式
    const datePattern = /^\d{4}-\d{2}-\d{2}$/;
    if (!datePattern.test(birthday)) {
      res.status(400).json(badRequest('Invalid birthday format, expected YYYY-MM-DD'));
      return;
    }

    if (!datePattern.test(date)) {
      res.status(400).json(badRequest('Invalid date format, expected YYYY-MM-DD'));
      return;
    }

    const result = await getDailyMessages(birthday, date);

    res.json(success({
      birthday: result.birthday,
      date: result.date,
      messages: result.messages,
      source: result.source,
      model: result.model,
      cached: result.cached,
    }));
  } catch (error) {
    console.error('Get daily message error:', error);
    res.status(500).json({ code: 500, message: 'Internal server error' });
  }
};