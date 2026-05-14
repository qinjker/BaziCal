import dotenv from 'dotenv';
import path from 'path';

// 根据 NODE_ENV 加载对应的环境变量文件
const envFile = process.env.NODE_ENV === 'production'
  ? '.env.production'
  : '.env.development';

dotenv.config({ path: path.resolve(process.cwd(), envFile) });

export const config = {
  // 数据库
  database: {
    url: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/bazical',
  },

  // JWT
  jwt: {
    secret: process.env.JWT_SECRET || 'dev-secret-change-in-production',
    expiresIn: process.env.JWT_EXPIRES_IN || '7d',
  },

  // 应用
  app: {
    port: parseInt(process.env.PORT || '3000', 10),
    env: process.env.NODE_ENV || 'development',
  },

  // API 安全
  api: {
    appKey: process.env.APP_KEY || 'dev-app-key',
  },
};