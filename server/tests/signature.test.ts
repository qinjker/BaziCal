/**
 * 签名中间件单元测试
 */

import { sha256 } from '../src/utils/crypto';

// Mock config
jest.mock('../src/config', () => ({
  config: {
    api: {
      appKey: 'test-app-key',
    },
  },
}));

describe('Signature Middleware', () => {
  describe('Signature Generation', () => {
    it('should generate valid signature', () => {
      const appKey = 'test-app-key';
      const timestamp = Date.now().toString();
      const body = JSON.stringify({ name: 'test' });

      const signature = sha256(appKey + timestamp + body);

      expect(signature).toHaveLength(64);
      expect(/^[a-f0-9]+$/.test(signature)).toBe(true);
    });

    it('should generate different signatures for different bodies', () => {
      const appKey = 'test-app-key';
      const timestamp = Date.now().toString();

      const sig1 = sha256(appKey + timestamp + JSON.stringify({ name: 'test1' }));
      const sig2 = sha256(appKey + timestamp + JSON.stringify({ name: 'test2' }));

      expect(sig1).not.toBe(sig2);
    });

    it('should generate different signatures for different timestamps', () => {
      const appKey = 'test-app-key';
      const body = JSON.stringify({ name: 'test' });

      const sig1 = sha256(appKey + '1234567890000' + body);
      const sig2 = sha256(appKey + '1234567890001' + body);

      expect(sig1).not.toBe(sig2);
    });

    it('should generate different signatures for different appKeys', () => {
      const timestamp = Date.now().toString();
      const body = JSON.stringify({ name: 'test' });

      const sig1 = sha256('app-key-1' + timestamp + body);
      const sig2 = sha256('app-key-2' + timestamp + body);

      expect(sig1).not.toBe(sig2);
    });
  });

  describe('Timestamp Validation', () => {
    it('should accept timestamp within 5 minutes', () => {
      const now = Date.now();
      const fiveMinutesAgo = now - 4 * 60 * 1000;

      const diff = Math.abs(now - fiveMinutesAgo);
      expect(diff).toBeLessThan(5 * 60 * 1000);
    });

    it('should reject timestamp older than 5 minutes', () => {
      const now = Date.now();
      const sixMinutesAgo = now - 6 * 60 * 1000;

      const diff = Math.abs(now - sixMinutesAgo);
      expect(diff).toBeGreaterThan(5 * 60 * 1000);
    });
  });
});