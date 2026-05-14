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
} from '../types';

const APP_KEY = import.meta.env.VITE_APP_KEY || 'dev-app-key-12345';
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
}

export const apiService = new ApiService();