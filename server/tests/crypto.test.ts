/**
 * 加密工具单元测试
 */

import { generateUserId, sha256 } from '../src/utils/crypto';

describe('Crypto Utils', () => {
  describe('generateUserId', () => {
    it('should generate consistent userId for same inputs', () => {
      const deviceId = 'device123';
      const name = '张三';
      const birthday = '1990-01-15';

      const userId1 = generateUserId(deviceId, name, birthday);
      const userId2 = generateUserId(deviceId, name, birthday);

      expect(userId1).toBe(userId2);
    });

    it('should generate different userId for different deviceId', () => {
      const name = '张三';
      const birthday = '1990-01-15';

      const userId1 = generateUserId('device1', name, birthday);
      const userId2 = generateUserId('device2', name, birthday);

      expect(userId1).not.toBe(userId2);
    });

    it('should generate different userId for different name', () => {
      const deviceId = 'device123';
      const birthday = '1990-01-15';

      const userId1 = generateUserId(deviceId, '张三', birthday);
      const userId2 = generateUserId(deviceId, '李四', birthday);

      expect(userId1).not.toBe(userId2);
    });

    it('should generate different userId for different birthday', () => {
      const deviceId = 'device123';
      const name = '张三';

      const userId1 = generateUserId(deviceId, name, '1990-01-15');
      const userId2 = generateUserId(deviceId, name, '1990-01-16');

      expect(userId1).not.toBe(userId2);
    });

    it('should return 64 character hex string (SHA256)', () => {
      const userId = generateUserId('device', 'name', '1990-01-15');

      expect(userId).toHaveLength(64);
      expect(/^[a-f0-9]+$/.test(userId)).toBe(true);
    });
  });

  describe('sha256', () => {
    it('should return consistent hash for same input', () => {
      const input = 'test-string';
      const hash1 = sha256(input);
      const hash2 = sha256(input);

      expect(hash1).toBe(hash2);
    });

    it('should return different hash for different inputs', () => {
      const hash1 = sha256('input1');
      const hash2 = sha256('input2');

      expect(hash1).not.toBe(hash2);
    });

    it('should return 64 character hex string', () => {
      const hash = sha256('any-input');

      expect(hash).toHaveLength(64);
      expect(/^[a-f0-9]+$/.test(hash)).toBe(true);
    });
  });
});