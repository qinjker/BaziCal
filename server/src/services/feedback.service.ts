/**
 * 反馈服务层
 */

import {
  createFeedback,
  findFeedbackById,
  findAllFeedbacks,
  findFeedbacksByUserId,
  updateFeedbackStatus,
  createReply,
  findRepliesByFeedbackId,
  deleteReplyById,
  type FeedbackType,
  type FeedbackStatus,
  type CreateFeedbackParams,
  type CreateReplyParams,
} from '../models/feedback.model';

// 有效的反馈类型
const VALID_FEEDBACK_TYPES: FeedbackType[] = ['功能建议', '问题反馈', '体验优化', '其他'];

// 有效的状态
const VALID_STATUSES: FeedbackStatus[] = ['pending', 'reviewed', 'replied', 'closed'];

/**
 * 校验反馈类型
 */
export const isValidFeedbackType = (type: string): type is FeedbackType => {
  return VALID_FEEDBACK_TYPES.includes(type as FeedbackType);
};

/**
 * 校验状态
 */
export const isValidStatus = (status: string): status is FeedbackStatus => {
  return VALID_STATUSES.includes(status as FeedbackStatus);
};

/**
 * 创建反馈
 */
export const submitFeedback = async (params: CreateFeedbackParams): Promise<void> => {
  const { type, content } = params;

  // 参数校验
  if (!type || !isValidFeedbackType(type)) {
    throw new Error('无效的反馈类型');
  }

  if (!content || content.trim().length === 0) {
    throw new Error('反馈内容不能为空');
  }

  if (content.length > 500) {
    throw new Error('反馈内容不能超过500字符');
  }

  await createFeedback(params);
};

/**
 * 获取反馈详情（包含回复）
 */
export const getFeedbackWithReplies = async (id: string) => {
  const feedback = await findFeedbackById(id);
  if (!feedback) {
    return null;
  }

  const replies = await findRepliesByFeedbackId(id);

  return {
    ...feedback,
    replies,
  };
};

/**
 * 获取反馈列表（包含回复）
 */
export const getFeedbacksWithReplies = async (params: {
  page?: number;
  pageSize?: number;
  status?: FeedbackStatus;
  type?: FeedbackType;
}) => {
  const result = await findAllFeedbacks(params);

  // 为每个反馈获取回复
  const feedbacksWithReplies = await Promise.all(
    result.feedbacks.map(async (feedback) => {
      const replies = await findRepliesByFeedbackId(feedback.id);
      return {
        ...feedback,
        replies,
      };
    })
  );

  return {
    feedbacks: feedbacksWithReplies,
    total: result.total,
  };
};

/**
 * 更新反馈状态
 */
export const setFeedbackStatus = async (id: string, status: FeedbackStatus, adminUsername: string): Promise<void> => {
  // 验证状态值
  if (!isValidStatus(status)) {
    throw new Error('无效的状态值');
  }

  const feedback = await findFeedbackById(id);
  if (!feedback) {
    throw new Error('反馈不存在');
  }

  await updateFeedbackStatus(id, status);
};

/**
 * 添加回复
 */
export const addReply = async (params: CreateReplyParams): Promise<void> => {
  const { content, feedback_id } = params;

  // 参数校验
  if (!content || content.trim().length === 0) {
    throw new Error('回复内容不能为空');
  }

  if (content.length > 1000) {
    throw new Error('回复内容不能超过1000字符');
  }

  const feedback = await findFeedbackById(feedback_id);
  if (!feedback) {
    throw new Error('反馈不存在');
  }

  await createReply(params);
};

/**
 * 获取反馈的所有回复
 */
export const getReplies = async (feedbackId: string) => {
  const feedback = await findFeedbackById(feedbackId);
  if (!feedback) {
    return null;
  }

  return findRepliesByFeedbackId(feedbackId);
};

/**
 * 删除回复
 */
export const removeReply = async (replyId: string): Promise<boolean> => {
  return deleteReplyById(replyId);
};

/**
 * 获取用户自己的反馈列表
 */
export const getMyFeedbacks = async (params: { page?: number; pageSize?: number; user_id: string }) => {
  return findFeedbacksByUserId(params);
};