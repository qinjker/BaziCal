/**
 * 八字计算服务单元测试
 */

import { calculateBazi } from '../src/services/bazi.service';

describe('Bazi Service', () => {
  describe('calculateBazi', () => {
    it('should calculate bazi for a given birthday and hour', () => {
      // 1990-01-15 10:00:00
      const result = calculateBazi('1990-01-15', 10);

      expect(result).toHaveProperty('year');
      expect(result).toHaveProperty('month');
      expect(result).toHaveProperty('day');
      expect(result).toHaveProperty('hour');
      expect(result).toHaveProperty('wuxing');
      expect(result).toHaveProperty('shishen');
    });

    it('should return correct structure for year pillar', () => {
      const result = calculateBazi('1990-01-15', 10);

      expect(result.year).toHaveProperty('stem');
      expect(result.year).toHaveProperty('branch');
      expect(typeof result.year.stem).toBe('string');
      expect(typeof result.year.branch).toBe('string');
    });

    it('should return correct structure for month pillar', () => {
      const result = calculateBazi('1990-01-15', 10);

      expect(result.month).toHaveProperty('stem');
      expect(result.month).toHaveProperty('branch');
    });

    it('should return correct structure for day pillar', () => {
      const result = calculateBazi('1990-01-15', 10);

      expect(result.day).toHaveProperty('stem');
      expect(result.day).toHaveProperty('branch');
    });

    it('should return correct structure for hour pillar', () => {
      const result = calculateBazi('1990-01-15', 10);

      expect(result.hour).toHaveProperty('stem');
      expect(result.hour).toHaveProperty('branch');
    });

    it('should return wuxing with 5 elements', () => {
      const result = calculateBazi('1990-01-15', 10);

      expect(result.wuxing).toHaveProperty('木');
      expect(result.wuxing).toHaveProperty('火');
      expect(result.wuxing).toHaveProperty('土');
      expect(result.wuxing).toHaveProperty('金');
      expect(result.wuxing).toHaveProperty('水');
    });

    it('should return shishen with year, month, day, hour', () => {
      const result = calculateBazi('1990-01-15', 10);

      expect(result.shishen).toHaveProperty('year');
      expect(result.shishen).toHaveProperty('month');
      expect(result.shishen).toHaveProperty('day');
      expect(result.shishen).toHaveProperty('hour');
    });

    it('should return day shishen as 日主', () => {
      const result = calculateBazi('1990-01-15', 10);

      expect(result.shishen.day).toBe('日主');
    });
  });

  describe('calculateBazi with different hours', () => {
    it('should calculate different bazi for different hours', () => {
      // 同一个日期，不同时辰
      const morning = calculateBazi('1990-01-15', 8);
      const afternoon = calculateBazi('1990-01-15', 15);
      const night = calculateBazi('1990-01-15', 22);

      // 时柱应该不同
      expect(morning.hour.stem).not.toBe(afternoon.hour.stem);
      expect(morning.hour.stem).not.toBe(night.hour.stem);
    });
  });
});