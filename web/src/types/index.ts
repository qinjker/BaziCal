/**
 * 类型定义
 */

// 八字四柱
export interface BaziPillar {
  stem: string;
  branch: string;
}

// 八字结果
export interface BaziResult {
  year: BaziPillar;
  month: BaziPillar;
  day: BaziPillar;
  hour: BaziPillar;
  wuxing: Record<string, number>;
  shishen: Record<string, string>;
}

// 每日日历数据
export interface DailyData {
  date: string;
  ganzhi: string;
  wuxing: string;
  yi: string[];
  ji: string[];
  star: string;
  luck: string;
  shishen: string;
  jieqi: string;
  lunarDate: string;
  holiday: string;
  branchShishen: string;
}

// 用户信息
export interface User {
  userId: string;
  name: string;
  birthday: string;
  hour: number;
  minute: number;
  gender: string;
  bazi: BaziResult;
}

// 用户信息 (本地存储)
export interface StoredUser {
  userId: string;
  name: string;
  birthday: string;
  hour: number;
  minute: number;
  gender: string;
}

// API 响应
export interface ApiResponse<T> {
  code: number;
  message: string;
  data?: T;
}

// 计算八字请求
export interface CalculateRequest {
  name: string;
  birthday: string;
  hour: number;
  minute: number;
  gender: '男' | '女';
  device_id?: string;
}

// 计算八字响应
export interface CalculateResponse {
  userId: string;
  bazi: BaziResult;
}

// 日历响应
export interface CalendarResponse {
  days: DailyData[];
}