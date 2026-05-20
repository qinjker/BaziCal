/**
 * 加密工具
 * 用于生成请求签名
 */

import { sha256 } from 'js-sha256';

const APP_KEY = import.meta.env.VITE_APP_KEY || 'apkey20260519';

/**
 * 生成签名和时间戳
 */
export const generateSignature = async (body: string): Promise<{ signature: string; timestamp: string }> => {
  const timestamp = Date.now().toString();
  const data = APP_KEY + timestamp + body;
  const signature = sha256(data);
  return { signature, timestamp };
};

/**
 * 获取时间戳
 */
export const getTimestamp = (): string => {
  return Date.now().toString();
};

/**
 * 获取设备ID
 */
export const getDeviceId = (): string => {
  let deviceId = localStorage.getItem('device_id');
  if (!deviceId) {
    deviceId = 'device_' + Math.random().toString(36).substring(2) + Date.now().toString(36);
    localStorage.setItem('device_id', deviceId);
  }
  return deviceId;
};