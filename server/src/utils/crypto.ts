import { createHash } from 'crypto';

/**
 * 生成用户唯一标识
 * userId = SHA256(设备ID + 姓名 + 生日)
 */
export const generateUserId = (deviceId: string, name: string, birthday: string): string => {
  const data = `${deviceId}:${name}:${birthday}`;
  return createHash('sha256').update(data).digest('hex');
};

/**
 * SHA256 哈希
 */
export const sha256 = (data: string): string => {
  return createHash('sha256').update(data).digest('hex');
};