/**
 * API 响应封装
 */

export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data?: T;
}

export const success = <T>(data?: T, message = 'Success'): ApiResponse<T> => ({
  code: 0,
  message,
  data,
});

export const error = (message: string, code = 1): ApiResponse => ({
  code,
  message,
});

export const unauthorized = (message = 'Unauthorized'): ApiResponse => ({
  code: 401,
  message,
});

export const forbidden = (message = 'Forbidden'): ApiResponse => ({
  code: 403,
  message,
});

export const notFound = (message = 'Not Found'): ApiResponse => ({
  code: 404,
  message,
});

export const badRequest = (message = 'Bad Request'): ApiResponse => ({
  code: 400,
  message,
});