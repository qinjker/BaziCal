# BaziCal Server

八字历后端服务

## 技术栈

- **Runtime**: Node.js 18+
- **Language**: TypeScript
- **Framework**: Express
- **Database**: PostgreSQL
- **Calendar**: Tyme4TS (八字计算)

## 项目结构

```
server/
├── src/
│   ├── config/           # 配置 (数据库、环境变量)
│   ├── routes/           # 路由定义
│   ├── controllers/      # 请求处理
│   ├── services/         # 业务逻辑 (八字计算)
│   ├── models/           # 数据模型
│   ├── middleware/        # 中间件 (签名验证、限流、认证)
│   └── utils/            # 工具函数
├── scripts/              # 脚本 (数据库初始化)
├── tests/                # 单元测试
└── package.json
```

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 配置环境变量

```bash
cp .env.example .env.development
# 编辑 .env.development 修改数据库配置
```

### 3. 启动数据库

确保 PostgreSQL 已运行，并创建数据库：

```sql
CREATE DATABASE bazical;
```

### 4. 启动开发服务器

```bash
npm run dev
```

服务将在 http://localhost:3000 启动

### 5. 运行测试

```bash
npm test
```

## API 接口

### 用户接口

#### POST /api/v1/bazi/calculate

提交生日，获取八字结果

```json
Headers:
  X-App-Key: your-app-key
  X-Timestamp: 1234567890000
  X-Signature: sha256-signature

Body:
{
  "name": "张三",
  "birthday": "1990-01-15",
  "hour": 10,
  "gender": "男",
  "device_id": "device-123"
}

Response:
{
  "code": 0,
  "message": "Success",
  "data": {
    "userId": "sha256-hash",
    "bazi": {
      "year": { "stem": "庚", "branch": "午" },
      "month": { "stem": "丁", "branch": "丑" },
      "day": { "stem": "丙", "branch": "子" },
      "hour": { "stem": "辛", "branch": "亥" }
    }
  }
}
```

#### GET /api/v1/bazi/calendar

获取某月日历

```
GET /api/v1/bazi/calendar?userId=xxx&year=2025&month=6

Response:
{
  "code": 0,
  "message": "Success",
  "data": {
    "days": [
      {
        "date": "2025-06-01",
        "ganzhi": "丙寅",
        "wuxing": "火木",
        "yi": ["祭祀", "开业"],
        "ji": ["动土"],
        "star": "角宿",
        "luck": "吉"
      }
    ]
  }
}
```

### 管理后台接口

#### POST /api/v1/admin/login

管理员登录

```json
Body:
{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "code": 0,
  "message": "Success",
  "data": {
    "token": "jwt-token"
  }
}
```

#### GET /api/v1/admin/users

获取所有用户列表 (需认证)

```
GET /api/v1/admin/users?page=1&pageSize=20
Authorization: Bearer jwt-token

Response:
{
  "code": 0,
  "message": "Success",
  "data": {
    "users": [...],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

## 安全机制

### 请求签名验证

所有 API 请求需携带以下 Header：

| Header | 说明 |
|--------|------|
| `X-App-Key` | 应用密钥 |
| `X-Timestamp` | 时间戳 (毫秒) |
| `X-Signature` | 请求签名 (SHA256) |

签名算法: `SHA256(appKey + timestamp + body)`

### 时间戳校验

请求 timestamp 与服务器时间差超过 5 分钟，视为无效请求

### 请求限流

- IP 限流: 100次/分钟
- API Key 限流: 10000次/分钟

## 部署

### 生产环境

1. 构建项目

```bash
npm run build
```

2. 配置环境变量

```bash
cp .env.example .env.production
# 编辑 .env.production
```

3. 启动服务

```bash
npm start
```

或使用 PM2:

```bash
pm2 start ecosystem.config.js
```

## PM2 管理

### 配置文件

`ecosystem.config.js` 包含完整的 PM2 配置：

```javascript
{
  name: 'bazical-server',
  script: 'dist/app.js',
  instances: 1,
  exec_mode: 'fork',
  max_memory_restart: '500M',
  error_file: './logs/pm2-error.log',
  out_file: './logs/pm2-out.log',
  autorestart: true
}
```

### 常用命令

```bash
# 启动服务
npm run pm2:start

# 停止服务
npm run pm2:stop

# 重启服务
npm run pm2:restart

# 查看日志
npm run pm2:logs

# 监控面板
npm run pm2:monit

# 开机自启
pm2 startup
pm2 save
```

### 日志

- 错误日志: `logs/pm2-error.log`
- 输出日志: `logs/pm2-out.log`
- 日志格式: `YYYY-MM-DD HH:mm:ss`

### Docker 部署

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY dist ./dist
EXPOSE 3000
CMD ["node", "dist/app.js"]
```

## 默认账号

- 用户名: admin
- 密码: admin123

## 许可证

MIT