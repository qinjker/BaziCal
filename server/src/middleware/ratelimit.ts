/**
 * 限流中间件
 * 基于 IP 和 AppKey 的请求限流
 */

import rateLimit from 'express-rate-limit';
import { Request } from 'express';

// IP 限流: 100次/分钟
export const ipRateLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 分钟
  max: 100, // 100 次
  message: {
    code: 429,
    message: 'Too many requests from this IP, please try again later.',
  },
  standardHeaders: true,
  legacyHeaders: false,
  keyGenerator: (req: Request) => {
    return req.ip || req.socket.remoteAddress || 'unknown';
  },
});

// API Key 限流: 10000次/分钟
export const apiKeyRateLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 分钟
  max: 10000, // 10000 次
  message: {
    code: 429,
    message: 'API rate limit exceeded.',
  },
  standardHeaders: true,
  legacyHeaders: false,
  keyGenerator: (req: Request) => {
    return req.headers['x-app-key'] as string || 'unknown';
  },
});