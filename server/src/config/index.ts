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

  // LLM 配置
  llm: {
    enabled: process.env.LLM_ENABLED === 'true',           // 是否启用 LLM
    minimax: {
      apiKey: process.env.MINIMAX_API_KEY || '',
      model: process.env.MINIMAX_MODEL || 'abab6.5s-chat',
      temperature: 0.7,
      maxTokens: 150,
      dailyLimit: parseInt(process.env.MINIMAX_DAILY_LIMIT || '100', 10), // 每日调用上限
    },
    qwen: {
      enabled: process.env.QWEN_ENABLED === 'true',       // 是否启用 Qwen
      apiKey: process.env.QWEN_API_KEY || '',
      model: process.env.QWEN_MODEL || 'qwen-turbo',
      temperature: 0.7,
      maxTokens: 150,
    },
  },
};