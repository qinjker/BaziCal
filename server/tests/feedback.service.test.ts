/**
 * 反馈服务单元测试
 */

import { isValidFeedbackType, isValidStatus } from '../src/services/feedback.service';

describe('Feedback Service', () => {
  describe('isValidFeedbackType', () => {
    it('should return true for valid feedback types', () => {
      expect(isValidFeedbackType('功能建议')).toBe(true);
      expect(isValidFeedbackType('问题反馈')).toBe(true);
      expect(isValidFeedbackType('体验优化')).toBe(true);
      expect(isValidFeedbackType('其他')).toBe(true);
    });

    it('should return false for invalid feedback types', () => {
      expect(isValidFeedbackType('无效类型')).toBe(false);
      expect(isValidFeedbackType('')).toBe(false);
      expect(isValidFeedbackType('功能建议2')).toBe(false);
    });
  });

  describe('isValidStatus', () => {
    it('should return true for valid statuses', () => {
      expect(isValidStatus('pending')).toBe(true);
      expect(isValidStatus('reviewed')).toBe(true);
      expect(isValidStatus('replied')).toBe(true);
      expect(isValidStatus('closed')).toBe(true);
    });

    it('should return false for invalid statuses', () => {
      expect(isValidStatus('invalid')).toBe(false);
      expect(isValidStatus('')).toBe(false);
      expect(isValidStatus('PENDING')).toBe(false);
    });
  });
});

describe('Feedback Model Types', () => {
  it('should have correct FeedbackStatus values', () => {
    const validStatuses = ['pending', 'reviewed', 'replied', 'closed'];
    validStatuses.forEach(status => {
      expect(isValidStatus(status)).toBe(true);
    });
  });

  it('should have correct FeedbackType values', () => {
    const validTypes = ['功能建议', '问题反馈', '体验优化', '其他'];
    validTypes.forEach(type => {
      expect(isValidFeedbackType(type)).toBe(true);
    });
  });
});