/**
 * 签名验证中间件
 * 验证请求的 X-App-Key, X-Timestamp, X-Signature
 */

import { Request, Response, NextFunction } from 'express';
import { sha256 } from '../utils/crypto';
import { config } from '../config';
import { badRequest, unauthorized } from '../utils/response';

export interface SignatureRequest extends Request {
  appKey?: string;
  timestamp?: number;
}

export const signatureMiddleware = (
  req: SignatureRequest,
  res: Response,
  next: NextFunction
): void => {
  const appKey = req.headers['x-app-key'] as string;
  const timestamp = req.headers['x-timestamp'] as string;
  const signature = req.headers['x-signature'] as string;

  // 验证必要参数
  if (!appKey || !timestamp || !signature) {
    res.status(400).json(badRequest('Missing required headers: X-App-Key, X-Timestamp, X-Signature'));
    return;
  }

  // 验证时间戳 (5分钟内有效)
  const ts = parseInt(timestamp, 10);
  const now = Date.now();
  const diff = Math.abs(now - ts);

  if (diff > 5 * 60 * 1000) {
    res.status(401).json(unauthorized('Request expired'));
    return;
  }

  // 验证 AppKey
  if (appKey !== config.api.appKey) {
    res.status(401).json(unauthorized('Invalid AppKey'));
    return;
  }

  // 验证签名
  const body = JSON.stringify(req.body || {});
  const expectedSignature = sha256(appKey + timestamp + body);

  if (signature !== expectedSignature) {
    res.status(401).json(unauthorized('Invalid signature'));
    return;
  }

  // 附加信息到请求对象
  req.appKey = appKey;
  req.timestamp = ts;

  next();
};