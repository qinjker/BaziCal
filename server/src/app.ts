/**
 * 应用入口
 */

import express from 'express';
import cors from 'cors';
import { config } from './config';
import { initDatabase } from './config/init-db';
import routes from './routes';
import { ipRateLimiter, apiKeyRateLimiter } from './middleware/ratelimit';

const app = express();

// 中间件
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 限流
app.use(ipRateLimiter);
app.use('/api/', apiKeyRateLimiter);

// 健康检查
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// API 路由
app.use('/api/v1', routes);

// 404 处理
app.use((req, res) => {
  res.status(404).json({ code: 404, message: 'Not Found' });
});

// 错误处理
app.use((err: Error, req: express.Request, res: express.Response, next: express.NextFunction) => {
  console.error('Unhandled error:', err);
  res.status(500).json({ code: 500, message: 'Internal Server Error' });
});

// 启动服务器
const PORT = config.app.port;

const startServer = async () => {
  try {
    // 初始化数据库
    await initDatabase();

    app.listen(PORT, () => {
      console.log(`Server is running on port ${PORT}`);
      console.log(`Environment: ${config.app.env}`);
    });
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
};

startServer();

export default app;