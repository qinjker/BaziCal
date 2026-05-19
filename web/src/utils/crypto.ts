/**
 * 加密工具
 * 用于生成请求签名
 */

const APP_KEY = import.meta.env.VITE_APP_KEY || 'dev-app-key-12345';

/**
 * SHA256 哈希
 */
export const sha256 = async (data: string): Promise<string> => {
  // 检查 crypto.subtle 是否可用 (浏览器需要 HTTPS 或 localhost)
  if (typeof crypto === 'undefined' || !crypto.subtle) {
    throw new Error('crypto.subtle is not available. Please use HTTPS or localhost.');
  }

  const msgBuffer = new TextEncoder().encode(data);
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map((b) => b.toString(16).padStart(2, '0')).join('');
};

/**
 * 生成签名和时间戳
 */
export const generateSignature = async (body: string): Promise<{ signature: string; timestamp: string }> => {
  const timestamp = Date.now().toString();
  const data = APP_KEY + timestamp + body;
  const signature = await sha256(data);
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