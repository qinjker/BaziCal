# BaziCal 八字历

## 项目概述

基于个人生辰八字的个性化命理日历App，提供数字版下载和实体版定制服务。

**Slogan**: 每一天，都算数

## 技术栈

- **后端**: Node.js (TypeScript) + Express/NestJS
- **前端**: Vue 3 (TypeScript) - MVP 快速验证
- **Android**: Kotlin - 生产环境使用
- **第三方库**: Tyme4TS (八字计算)
- **数据库**: PostgreSQL

### Android 兼容性要求

- **最低支持**: Android 6.0 (API 23)
- **目标支持**: Android 14 (API 34)
- **设备兼容**: 需兼容但不限于以下品牌：
  - 小米 (MIUI)
  - OPPO (ColorOS)
  - vivo (FuntouchOS)
  - 华为 (HarmonyOS)
  - 三星 (OneUI)
  - 一加 (OxygenOS)
- **屏幕适配**: 支持主流屏幕尺寸，包括折叠屏设备
- **权限处理**: 兼容不同品牌系统的权限管理差异

## 项目结构

```
BaziCal/
├── docs/                      # 项目文档
│   ├── 项目简介文件.md
│   ├── 技术文档.md
│   └── design/               # 设计稿
│
├── server/                    # 后端 (Node.js)
│   ├── src/
│   │   ├── config/          # 配置文件
│   │   │   ├── index.ts    # 主配置
│   │   │   └── database.ts # 数据库配置
│   │   │
│   │   ├── routes/         # 路由定义
│   │   │   ├── index.ts    # 路由汇总
│   │   │   ├── bazi.ts     # 八字相关路由
│   │   │   └── admin.ts    # 管理后台路由
│   │   │
│   │   ├── controllers/    # 控制器 (处理请求)
│   │   │   ├── bazi.controller.ts
│   │   │   └── admin.controller.ts
│   │   │
│   │   ├── services/       # 业务逻辑
│   │   │   ├── bazi.service.ts     # 八字计算服务
│   │   │   └── user.service.ts    # 用户服务
│   │   │
│   │   ├── models/         # 数据模型
│   │   │   ├── admin.model.ts
│   │   │   └── user.model.ts
│   │   │
│   │   ├── middleware/     # 中间件
│   │   │   ├── signature.ts    # 签名验证
│   │   │   ├── ratelimit.ts    # 限流
│   │   │   └── auth.ts         # 管理员认证
│   │   │
│   │   ├── utils/          # 工具函数
│   │   │   ├── crypto.ts       # 加密相关
│   │   │   └── response.ts     # 响应封装
│   │   │
│   │   └── app.ts          # 应用入口
│   │
│   ├── tests/              # 测试文件
│   ├── package.json
│   └── tsconfig.json
│
├── web/                      # 前端 (Vue 3)
│   ├── src/
│   │   ├── api/            # API 调用
│   │   │   ├── index.ts    # API 入口
│   │   │   ├── bazi.ts     # 八字 API
│   │   │   └── auth.ts     # 认证 API
│   │   │
│   │   ├── pages/          # 页面
│   │   │   ├── index/      # 首页 - 生日输入
│   │   │   ├── calendar/   # 日历查看
│   │   │   └── daily/      # 每日详情
│   │   │
│   │   ├── components/     # 公共组件
│   │   ├── hooks/          # 自定义 Hooks
│   │   ├── stores/         # 状态管理 (Pinia)
│   │   ├── utils/          # 工具函数
│   │   └── types/          # 类型定义
│   │
│   ├── public/             # 静态资源
│   ├── tests/              # 测试文件
│   ├── package.json
│   └── vite.config.ts      # Vite 配置
│
├── android/                 # Android 原生开发 (Kotlin)
│   └── .gitkeep
│
└── CLAUDE.md
```

## 部署

### 生产环境

| 环境 | 方式 |
|------|------|
| **后端** | Docker 容器化部署 / PM2 进程管理 |
| **前端** | Nginx 静态托管 / Vercel / 独立部署 |
| **数据库** | PostgreSQL 主从集群 |

### 域名与 HTTPS

- API 域名: `api.bazical.com`
- 前端域名: `www.bazical.com`
- 管理后台: `admin.bazical.com`
- 强制 HTTPS

### 环境变量

```
# 后端 (.env)
NODE_ENV=production
PORT=3000
DATABASE_URL=postgresql://user:pass@host:5432/bazical
JWT_SECRET=xxx
APP_KEY=xxx

# 前端 (.env.production)
VITE_API_BASE_URL=https://api.bazical.com
```

## 开发规范

### TypeScript 代码规范

- 命名规范：
  - 变量/函数：camelCase
  - 类名/接口：PascalCase
  - 常量：UPPER_SNAKE_CASE
- 类型注解：必须使用
- 模块级 docstring：中文描述
- 严格模式：strict: true

### Git 规范

- 分支名: `feature/xxx`, `fix/xxx`, `docs/xxx`
- 提交信息: `feat:`, `fix:`, `docs:`, `refactor:`

## 核心模块规划

### 后端 (server/src/)

| 模块 | 职责 |
|------|------|
| `config/` | 配置文件 |
| `routes/` | 路由定义 |
| `controllers/` | 接收请求，调用 service |
| `services/` | 业务逻辑，Tyme4TS 计算 |
| `models/` | 数据模型，ORM |
| `middleware/` | 签名验证、限流、认证 |

### 前端 (web/src/)

| 模块 | 职责 |
|------|------|
| `api/` | API 调用封装 |
| `pages/` | 页面组件 |
| `components/` | 公共组件 |
| `hooks/` | 自定义 Hooks |
| `stores/` | 状态管理 |
| `utils/` | 工具函数 |

## 命名规范

- 分支名: `feature/xxx`, `fix/xxx`, `docs/xxx`
- 提交信息: `feat:`, `fix:`, `docs:`, `refactor:`


# 减少LLM常见编程错误的行为准则

按需与项目特定说明合并。

**权衡：** 这些准则偏向谨慎而非速度。对于简单任务，可自行判断。

---

## 1. 先思考，再编码

不要臆断。不要隐藏困惑。明确呈现权衡取舍。

在实现之前：

- 明确陈述你的假设。如有不确定，请提问。
- 如果存在多种解读，将其呈现出来——不要默默选择一种。
- 如果有更简单的方法存在，说出来。在合理时提出异议。
- 如果有不清楚的地方，停下来。明确指出困惑之处。提问。

## 2. 简洁优先

用最少的代码解决问题。不写推测性代码。

- 不添加超出需求的功能。
- 不为一次性使用的代码创建抽象。
- 不添加未被要求的"灵活性"或"可配置性"。
- 不为不可能发生的场景添加错误处理。
- 如果你写了 200 行代码而本可以 50 行完成，重写它。
- 问自己："资深工程师会觉得这过于复杂吗？"如果是，就简化。

## 3. 精准修改

只触碰你必须改的，只清理你自己造成的混乱。

编辑现有代码时：

- 不要"改进"相邻的代码、注释或格式。
- 不要重构那些没有问题的东西。
- 匹配现有风格，即使你会用不同的写法。
- 如果你注意到无关的死代码，指出它——但不要删除它。

当你的修改产生孤立项时：

- 移除因你的修改而不再使用的导入/变量/函数。
- 除非被要求，否则不要删除既存的死代码。

**检验标准：** 每一行改动都应能直接追溯到用户的请求。

## 4. 目标驱动执行

定义成功标准。循环执行直至验证通过。

将任务转化为可验证的目标：

- "添加验证" → "为无效输入编写测试，然后让测试通过"
- "修复这个 bug" → "编写一个能复现该 bug 的测试，然后让测试通过"
- "重构 X" → "确保测试在重构前后均能通过"

对于多步骤任务，给出简要计划：

1. [步骤] → 验证：[检查项]
2. [步骤] → 验证：[检查项]
3. [步骤] → 验证：[检查项]

有力的成功标准能让你独立循环工作。薄弱的标准（如"把它做出来"）则需要不断澄清。