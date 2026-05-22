/**
 * API 服务
 * 包含签名验证的 API 调用
 */

import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';
import { generateSignature, getDeviceId } from '../utils/crypto';
import type {
  ApiResponse,
  CalculateRequest,
  CalculateResponse,
  CalendarResponse,
  User,
  DailyDetailResponse,
} from '../types';

const APP_KEY = import.meta.env.VITE_APP_KEY || 'apkey20260519';
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

class ApiService {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // 请求拦截器 - 添加签名
    this.client.interceptors.request.use(
      async (config) => {
        const body = JSON.stringify(config.data || {});
        const { signature, timestamp } = await generateSignature(body);

        config.headers.set('X-App-Key', APP_KEY);
        config.headers.set('X-Timestamp', timestamp);
        config.headers.set('X-Signature', signature);

        return config;
      },
      (error) => Promise.reject(error)
    );

    // 响应拦截器
    this.client.interceptors.response.use(
      (response) => response.data,
      (error) => {
        console.error('API Error:', error);
        return Promise.reject(error);
      }
    );
  }

  /**
   * 计算八字
   */
  async calculate(data: CalculateRequest): Promise<ApiResponse<CalculateResponse>> {
    const payload = {
      ...data,
      device_id: getDeviceId(),
    };
    return this.client.post('/v1/bazi/calculate', payload);
  }

  /**
   * 获取日历
   */
  async getCalendar(userId: string, year: number, month: number): Promise<ApiResponse<CalendarResponse>> {
    return this.client.get('/v1/bazi/calendar', {
      params: { userId, year, month },
    });
  }

  /**
   * 阳历转阴历
   */
  async solarToLunar(date: string): Promise<ApiResponse<{ year: number; month: number; day: number }>> {
    return this.client.get('/v1/bazi/solar-to-lunar', {
      params: { date },
    });
  }

  /**
   * 获取用户信息
   */
  async getUserById(userId: string): Promise<ApiResponse<User>> {
    return this.client.get(`/v1/bazi/user/${userId}`);
  }

  /**
   * 获取每日详情
   */
  async getDailyDetail(userId: string, date: string): Promise<ApiResponse<DailyDetailResponse>> {
    return this.client.get(`/v1/bazi/daily/${date}`, {
      params: { userId },
    });
  }

  /**
   * 管理员登录 (无需签名)
   */
  async adminLogin(username: string, password: string): Promise<ApiResponse<{ token: string }>> {
    return this.client.post('/v1/admin/login', { username, password });
  }

  /**
   * 获取用户列表 (管理员)
   */
  async getAdminUsers(page: number, pageSize: number): Promise<ApiResponse<{ users: any[]; total: number }>> {
    const token = localStorage.getItem('admin_token');
    return this.client.get('/v1/admin/users', {
      params: { page, pageSize },
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
  }

  // ==================== 反馈相关 API ====================

  /**
   * 提交反馈
   */
  async submitFeedback(data: {
    type: string;
    content: string;
    contact?: string;
    device_id?: string;
  }): Promise<ApiResponse<null>> {
    const payload = {
      ...data,
      device_id: getDeviceId(),
    };
    return this.client.post('/v1/feedback', payload);
  }

  /**
   * 获取反馈的回复列表（盖楼）
   */
  async getFeedbackReplies(feedbackId: string): Promise<ApiResponse<{ replies: any[] }>> {
    return this.client.get(`/v1/feedbacks/${feedbackId}/replies`);
  }

  /**
   * 添加反馈回复
   */
  async addFeedbackReply(feedbackId: string, content: string): Promise<ApiResponse<null>> {
    return this.client.post(`/v1/feedbacks/${feedbackId}/replies`, { content });
  }

  /**
   * 获取我的反馈列表（用户端）
   */
  async getMyFeedbacks(
    page = 1,
    pageSize = 20,
    deviceId?: string
  ): Promise<ApiResponse<{ feedbacks: any[]; total: number; page: number; pageSize: number }>> {
    const device_id = deviceId || getDeviceId();
    return this.client.get('/v1/feedbacks', {
      params: { page, pageSize, device_id },
    });
  }

  /**
   * 获取单条反馈详情（含回复）
   */
  async getFeedbackDetail(feedbackId: string): Promise<ApiResponse<any>> {
    return this.client.get(`/v1/feedbacks/${feedbackId}`);
  }

  /**
   * 获取反馈列表（管理员）
   */
  async getAdminFeedbacks(params: {
    page?: number;
    pageSize?: number;
    status?: string;
    type?: string;
  }): Promise<ApiResponse<{ feedbacks: any[]; total: number }>> {
    const token = localStorage.getItem('admin_token');
    return this.client.get('/v1/admin/feedbacks', {
      params,
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
  }

  /**
   * 回复反馈（管理员）
   */
  async replyFeedback(feedbackId: string, content: string): Promise<ApiResponse<null>> {
    const token = localStorage.getItem('admin_token');
    return this.client.post(`/v1/admin/feedbacks/${feedbackId}/reply`, { content }, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
  }

  /**
   * 更新反馈状态（管理员）
   */
  async updateFeedbackStatus(feedbackId: string, status: string): Promise<ApiResponse<null>> {
    const token = localStorage.getItem('admin_token');
    return this.client.patch(`/v1/admin/feedbacks/${feedbackId}/status`, { status }, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
  }

  // ==================== 寄语管理 API ====================

  /**
   * 获取寄语列表（管理员）
   */
  async getAdminMessages(params: {
    page?: number;
    pageSize?: number;
    category?: string;
  }): Promise<ApiResponse<{ messages: any[]; total: number; page: number; pageSize: number }>> {
    const token = localStorage.getItem('admin_token');
    return this.client.get('/v1/admin/messages', {
      params,
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
  }

  /**
   * 添加寄语（管理员）
   */
  async createMessage(data: { content: string; category?: string }): Promise<ApiResponse<any>> {
    const token = localStorage.getItem('admin_token');
    return this.client.post('/v1/admin/messages', data, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
  }

  /**
   * 更新寄语（管理员）
   */
  async updateMessage(messageId: string, data: { content: string; category?: string }): Promise<ApiResponse<any>> {
    const token = localStorage.getItem('admin_token');
    return this.client.put(`/v1/admin/messages/${messageId}`, data, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
  }

  /**
   * 删除寄语（管理员）
   */
  async deleteMessage(messageId: string): Promise<ApiResponse<null>> {
    const token = localStorage.getItem('admin_token');
    return this.client.delete(`/v1/admin/messages/${messageId}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
  }
}

export const apiService = new ApiService();