/**
 * 管理员认证中间件
 */

import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { config } from '../config';
import { unauthorized, forbidden } from '../utils/response';

export interface AdminPayload {
  id: string;
  username: string;
}

export interface AuthRequest extends Request {
  admin?: AdminPayload;
}

export const adminAuthMiddleware = (
  req: AuthRequest,
  res: Response,
  next: NextFunction
): void => {
  const authHeader = req.headers.authorization;

  if (!authHeader) {
    res.status(401).json(unauthorized('No authorization header'));
    return;
  }

  const parts = authHeader.split(' ');
  if (parts.length !== 2 || parts[0] !== 'Bearer') {
    res.status(401).json(unauthorized('Invalid authorization format'));
    return;
  }

  const token = parts[1];

  try {
    const decoded = jwt.verify(token, config.jwt.secret) as AdminPayload;
    req.admin = decoded;
    next();
  } catch (err) {
    res.status(401).json(unauthorized('Invalid or expired token'));
    return;
  }
};

/**
 * 生成管理员 JWT Token
 */
export const generateAdminToken = (admin: AdminPayload): string => {
  return jwt.sign(admin, config.jwt.secret, {
    expiresIn: config.jwt.expiresIn as jwt.SignOptions['expiresIn'],
  });
};