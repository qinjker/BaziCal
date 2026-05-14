/**
 * 存储工具
 */

const USER_KEY = 'bazical_user';
const BAZI_KEY = 'bazical_bazi';

export interface StoredUser {
  userId: string;
  name: string;
  birthday: string;
  hour: number;
  minute: number;
  gender: string;
}

export const storage = {
  // 保存用户信息
  setUser(user: StoredUser): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },

  // 获取用户信息
  getUser(): StoredUser | null {
    const data = localStorage.getItem(USER_KEY);
    return data ? JSON.parse(data) : null;
  },

  // 保存八字结果
  setBazi(bazi: unknown): void {
    localStorage.setItem(BAZI_KEY, JSON.stringify(bazi));
  },

  // 获取八字结果
  getBazi(): unknown {
    const data = localStorage.getItem(BAZI_KEY);
    return data ? JSON.parse(data) : null;
  },

  // 清除所有数据
  clear(): void {
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(BAZI_KEY);
  },
};