/**
 * 用户状态管理
 */

import { defineStore } from 'pinia';
import { apiService } from '../api';
import type { BaziResult, StoredUser } from '../types';
import { storage } from '../utils/storage';

interface UserState {
  user: StoredUser | null;
  bazi: BaziResult | null;
  loading: boolean;
  error: string | null;
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    user: null,
    bazi: null,
    loading: false,
    error: null,
  }),

  actions: {
    // 从本地存储恢复用户
    restore() {
      const user = storage.getUser();
      const bazi = storage.getBazi();
      if (user && bazi) {
        this.user = user;
        this.bazi = bazi as BaziResult;
      }
    },

    // 计算八字
    async calculate(name: string, birthday: string, birthday_type: 'solar' | 'lunar', hour: number, minute: number, gender: '男' | '女') {
      this.loading = true;
      this.error = null;

      try {
        const response = await apiService.calculate({ name, birthday, birthday_type, hour, minute, gender });

        if (response.code === 0 && response.data) {
          this.user = {
            userId: response.data.userId,
            name,
            birthday,
            birthday_type,
            hour,
            minute,
            gender,
          };
          this.bazi = response.data.bazi;

          // 保存到本地
          storage.setUser(this.user);
          storage.setBazi(this.bazi);
        } else {
          this.error = response.message || '计算失败';
        }
      } catch (err) {
        this.error = '网络错误，请重试';
        console.error('Calculate error:', err);
      } finally {
        this.loading = false;
      }
    },

    // 登出
    logout() {
      this.user = null;
      this.bazi = null;
      storage.clear();
    },
  },
});