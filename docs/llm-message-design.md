# 正能量寄语生成方案

> 文档更新时间：2026-05-21
> 状态：待实现

---

## 核心变更说明

**同一生日的用户共享每日寄语，缓存粒度从 `userId + date` 改为 `birthday + date`。**

缓存Key格式：
```
daily_message:{birthday}:{date}
例如: daily_message:1990-01-15:2026-05-21
```

### LLM Provider 顺序

```
1. MiniMax（主）
   ↓ 失败
2. Qwen 千问（备）
   ↓ 失败
3. 静态寄语库（最终降级）
```

### 静态寄语库初始化

- 项目启动时检查数据库是否有 100 条静态寄语
- 有则跳过，无则调用 LLM 生成 100 条入库

---

## 一、LLM Router 设计

### 1. 多 Provider 支持

通过配置文件切换不同的 LLM 服务商：

```typescript
// config/llm.ts
export type LLMProvider = 'minimax' | 'qwen' | 'static';

export interface LLMConfig {
  provider: LLMProvider;
  apiKey?: string;
  baseUrl?: string;
  model: string;
  temperature?: number;
}
```

### 2. Router 接口

```typescript
// services/llm/router.ts
interface LLMRequest {
  model: string;
  messages: Array<{ role: string; content: string }>;
  temperature?: number;
  maxTokens?: number;
}

interface LLMResponse {
  content: string;
  usage?: {
    promptTokens: number;
    completionTokens: number;
    totalTokens: number;
  };
}

interface ILLMProvider {
  name(): string;
  chat(request: LLMRequest): Promise<LLMResponse>;
}
```

### 3. Provider 实现

```typescript
// services/llm/providers/minimax.ts
export class MiniMaxProvider implements ILLMProvider {
  name() { return 'minimax'; }

  async chat(request: LLMRequest): Promise<LLMResponse> {
    // 调用 MiniMax API
  }
}

// services/llm/providers/qwen.ts
export class QwenProvider implements ILLMProvider {
  name() { return 'qwen'; }

  async chat(request: LLMRequest): Promise<LLMResponse> {
    // 调用千问 API
  }
}
```

### 4. Router 实现

```typescript
// services/llm/router.ts
export class LLMRouter {
  private providers: ILLMProvider[];

  constructor() {
    const config = getLLMConfig();
    this.providers = [
      new MiniMaxProvider(),
      new QwenProvider(),
    ];
  }

  async chat(request: LLMRequest): Promise<LLMResponse> {
    let lastError: Error | null = null;

    for (const provider of this.providers) {
      try {
        console.log(`Trying ${provider.name()}...`);
        return await provider.chat(request);
      } catch (error) {
        lastError = error as Error;
        console.error(`${provider.name()} failed:`, error);
        continue;
      }
    }

    // 所有 Provider 都失败，抛出错误让调用方使用静态寄语
    throw lastError || new Error('All LLM providers failed');
  }
}

export const llmRouter = new LLMRouter();
```

---

## 二、缓存策略

### 1. 多级缓存架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   前端缓存   │ ──▶ │   后端缓存   │ ──▶ │  LLM 调用   │
│  (LocalStorage) │     │   (Redis)   │     │             │
└─────────────┘     └─────────────┘     └─────────────┘
```

### 2. 缓存结构（按生日缓存）

```typescript
// 本地缓存
interface LocalCachedMessage {
  birthday: string;    // YYYY-MM-DD 生日
  date: string;         // YYYY-MM-DD 当日日期
  messages: string[];
  generatedAt: number;  // timestamp
}

// 后端缓存
interface CachedMessage {
  id: string;
  birthday: string;              // 生日日期
  date: string;                  // 当日日期
  messages: string[];
  prompt_tokens: number;
  completion_tokens: number;
  generated_at: Date;
  expires_at: Date;              // 每天 23:59:59 过期
}

// Redis Key 格式
// daily_message:{birthday}:{date}
// 例如: daily_message:1990-01-15:2026-05-21
```

### 3. 缓存逻辑流程

```typescript
async function getDailyMessage(birthday: string, date: string): Promise<string[]> {
  // 1. 检查前端本地缓存（最快）
  const localCached = getLocalCache(birthday, date);
  if (localCached && !isExpired(localCached)) {
    return localCached.messages;
  }

  // 2. 检查后端缓存（Redis/数据库）
  const backendCached = await getBackendCache(birthday, date);
  if (backendCached) {
    // 同步到前端缓存
    saveLocalCache(birthday, date, backendCached);
    return backendCached.messages;
  }

  // 3. 调用 LLM 生成
  const messages = await generateWithLLM(birthday, date);

  // 4. 保存到后端缓存（设置过期时间）
  await saveBackendCache(birthday, date, messages);

  // 5. 同步到前端缓存
  saveLocalCache(birthday, date, messages);

  return messages;
}
```

### 4. 过期时间策略

```typescript
function getExpiresAt(): Date {
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  // 设置为当天 23:59:59
  today.setHours(23, 59, 59, 999);
  return today;
}

function isExpired(cached: LocalCachedMessage): boolean {
  const now = Date.now();
  const expires = cached.generatedAt + (24 * 60 * 60 * 1000); // 24小时
  return now > expires;
}
```

---

## 三、降级方案

### 1. 多级降级策略

```
┌─────────────────────────────────────────────────────────┐
│                    LLM 调用流程                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  1. 主 Provider (MiniMax)                              │
│     ↓ 失败                                             │
│  2. 备用 Provider (Qwen 千问)                          │
│     ↓ 失败                                             │
│  3. 静态寄语库（最终降级）                             │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 2. 降级实现

```typescript
async function generateWithLLM(
  birthday: string,
  date: string,
): Promise<string[]> {
  const providers = [
    new MiniMaxProvider(),
    new QwenProvider(),
  ];

  let lastError: Error | null = null;

  for (const provider of providers) {
    try {
      console.log(`Trying ${provider.name()}...`);
      const result = await provider.chat(buildLLMRequest(birthday, date));
      return parseMessages(result.content);
    } catch (error) {
      lastError = error as Error;
      console.error(`${provider.name()} failed:`, error);
      continue;
    }
  }

  // 所有 Provider 都失败，降级到静态寄语
  console.warn('All LLM providers failed, using static fallback');
  return getStaticMessage(date, birthday);
}
```

### 3. 超时控制

```typescript
async function chatWithTimeout(
  provider: ILLMProvider,
  request: LLMRequest,
  timeoutMs: number = 10000
): Promise<LLMResponse> {
  return Promise.race([
    provider.chat(request),
    new Promise<LLMResponse>((_, reject) =>
      setTimeout(() => reject(new Error('LLM timeout')), timeoutMs)
    )
  ]);
}
```

---

## 四、静态寄语库

### 1. 静态寄语库说明

静态寄语库存放在数据库 `static_messages` 表中，包含 100 条预生成的寄语。

### 2. 静态寄语库初始化

```typescript
// scripts/init-static-messages.ts
async function initializeStaticMessages(): Promise<void> {
  // 检查是否已有 100 条
  const count = await getStaticMessageCount();

  if (count >= 100) {
    console.log(`静态寄语库已有 ${count} 条，跳过初始化`);
    return;
  }

  console.log(`开始生成 ${100 - count} 条静态寄语...`);

  // 批量生成
  const messages: string[] = [];
  for (let i = 0; i < 100; i++) {
    try {
      const birthday = getRandomBirthday(); // 随机生日
      const date = getRandomFutureDate(); // 随机未来日期
      const result = await generateWithLLM(birthday, date);
      messages.push(...result);
      console.log(`已生成 ${i + 1}/100 条`);
    } catch (error) {
      console.error(`生成失败:`, error);
    }
  }

  // 入库
  await saveStaticMessages(messages);
  console.log('静态寄语库初始化完成');
}
```

### 3. 静态寄语获取

```typescript
// 按日期和生日取余，保证同一生日每天固定
function getStaticMessage(date: string, birthday: string): string[] {
  const messages = getAllStaticMessages();

  // 用日期 + birthday 取余，保证同一生日每天固定
  const dateHash = hashString(date);
  const birthdayHash = hashString(birthday);
  const baseIndex = (dateHash + birthdayHash) % messages.length;

  return [
    messages[baseIndex],
    messages[(baseIndex + 1) % messages.length],
    messages[(baseIndex + 2) % messages.length],
  ];
}

function hashString(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash) + str.charCodeAt(i);
    hash = hash & hash;
  }
  return Math.abs(hash);
}
```

### 4. 启动时初始化

```typescript
// server/src/app.ts 或 init-db.ts
import { initializeStaticMessages } from './scripts/init-static-messages';

// 应用启动时
app.on('ready', async () => {
  // ... 其他初始化
  await initializeStaticMessages();
});
```

---

## 五、成本优化

### 1. 模型选择策略

```typescript
// 根据场景选择合适的模型
const modelStrategy = {
  // 日常寄语：使用 MiniMax
  dailyMessage: {
    provider: 'minimax',
    model: 'abab6.5s-chat',
    maxTokens: 150,
    temperature: 0.7
  },

  // 备用：使用 Qwen 千问
  qwen: {
    provider: 'qwen',
    model: 'qwen-turbo',
    maxTokens: 150,
    temperature: 0.7
  },
};
```

### 2. 批量处理

```typescript
// 按生日批量预生成，共享缓存
async function batchPreGenerate(messages: Array<{birthday: string; date: string}>): Promise<void> {
  const batches = chunkArray(messages, 10); // 每批10个

  for (const batch of batches) {
    await Promise.all(
      batch.map(({ birthday, date }) => generateWithLLM(birthday, date))
    );
    // 避免触发限流
    await sleep(1000);
  }
}
```

### 3. 缓存命中率优化（按生日）

```typescript
// 预缓存策略：同一生日的用户共享预缓存
async function preCacheForBirthday(birthday: string): Promise<void> {
  const today = new Date();

  // 生成今天和明天的寄语
  for (let i = 0; i <= 1; i++) {
    const date = new Date(today);
    date.setDate(date.getDate() + i);
    const dateStr = date.toISOString().split('T')[0];

    const cached = await getBackendCache(birthday, dateStr);
    if (!cached) {
      await generateWithLLM(birthday, dateStr);
      await sleep(500); // 避免并发过高
    }
  }
}
```

---

## 六、Prompt 设计

### 1. 生日信息提取

```typescript
interface BirthdayBaziContext {
  birthday: string;              // 生日日期 YYYY-MM-DD
  rizhu: { stem: string; branch: string };
  ganzhiYear: string;
  ganzhiMonth: string;
  shishen: { year: string; month: string; day: string; hour: string };
  wuxing: { wood: number; fire: number; earth: number; metal: number; water: number };
  personality: string[];
  todayGanzhi: string;
  todayShishen: string;
  energyLevel: number;
}
```

### 2. Prompt 模板

```markdown
你是命理老师，用温暖简短的话给人力量。用户八字{{rizhu}}，
五行{{wuxingStr}}，今日{{todayGanzhi}}，十神{{todayShishen}}。
生成3条15-20字正能量寄语，用"你"称呼，分隔。
```

### 3. 输出示例

```
今天的你思维清晰，稳重的特质让你处理问题格外得心应手。
正官星旺，今日贵人运强，多与人交流会有意想不到的收获。
保持平和心态，你的踏实和耐心正在为你积累好运。
```

---

## 七、API 设计

```
POST /api/v1/bazi/daily-message
```

Request:
```json
{
  "birthday": "1990-01-15",
  "date": "2026-05-21"
}
```

Response:
```json
{
  "code": 0,
  "data": {
    "birthday": "1990-01-15",
    "date": "2026-05-21",
    "messages": [
      "今天的你思维清晰，稳重的特质让你处理问题格外得心应手",
      "正官星旺，今日贵人运强，多与人交流会有意想不到的收获",
      "保持平和心态，你的踏实和耐心正在为你积累好运"
    ],
    "source": "ai",
    "model": "abab6.5s-chat",
    "cached": false
  }
}
```

---

## 八、配置示例

```typescript
// config/llm.ts
export const llmConfig = {
  provider: process.env.LLM_PROVIDER || 'minimax',

  minimax: {
    apiKey: process.env.MINIMAX_API_KEY,
    model: 'abab6.5s-chat',
    temperature: 0.7,
    maxTokens: 150
  },

  qwen: {
    apiKey: process.env.QWEN_API_KEY,
    model: 'qwen-turbo',
    temperature: 0.7,
    maxTokens: 150
  },

  // 成本控制
  costControl: {
    dailyLimit: 1000,        // 每日总调用上限
    birthdayDailyLimit: 5,  // 同一生日每日上限（共享缓存）
    enablePreCache: true,    // 启用预缓存
    preCacheHours: [2, 3, 4] // 预缓存时段（凌晨低峰期）
  }
};
```

---

## 九、文件结构

```
server/src/
├── services/
│   ├── llm/
│   │   ├── router.ts           # LLM 路由
│   │   ├── types.ts           # 类型定义
│   │   ├── providers/
│   │   │   ├── index.ts
│   │   │   ├── minimax.ts
│   │   │   └── qwen.ts
│   │   ├── message.service.ts # 寄语生成服务
│   │   └── cache.service.ts   # 缓存服务
│   └── bazi.service.ts
├── routes/
│   └── bazi.ts
├── controllers/
│   └── bazi.controller.ts
├── config/
│   └── llm.ts
├── models/
│   ├── daily_message.model.ts # 缓存表
│   └── static_message.model.ts # 静态寄语表
└── scripts/
    └── init-static-messages.ts # 静态寄语初始化脚本
```

---

## 十、数据库变更

### static_messages 表结构（新增）

```sql
-- 静态寄语库
CREATE TABLE static_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  content TEXT NOT NULL,                    -- 寄语内容
  category VARCHAR(50) DEFAULT 'general',    -- 分类
  created_at TIMESTAMP DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_static_messages_category ON static_messages(category);
```

### daily_messages 表结构（按生日）

```sql
-- 按生日缓存的每日寄语
CREATE TABLE daily_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  birthday DATE NOT NULL,          -- 生日日期
  date DATE NOT NULL,              -- 当日日期
  messages TEXT[] NOT NULL,
  prompt_tokens INTEGER,
  completion_tokens INTEGER,
  generated_at TIMESTAMP DEFAULT NOW(),
  expires_at TIMESTAMP NOT NULL,
  UNIQUE(birthday, date)           -- 唯一约束
);

-- 索引
CREATE INDEX idx_daily_messages_birthday_date ON daily_messages(birthday, date);
CREATE INDEX idx_daily_messages_expires ON daily_messages(expires_at);
```

### llm_usage_logs 表（新增）

```sql
-- LLM 使用日志
CREATE TABLE llm_usage_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  provider VARCHAR(50) NOT NULL,           -- minmax / qwen
  model VARCHAR(100) NOT NULL,
  prompt_tokens INTEGER,
  completion_tokens INTEGER,
  response_time INTEGER,                    -- 毫秒
  success BOOLEAN DEFAULT true,
  error_message TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_llm_usage_logs_provider ON llm_usage_logs(provider);
CREATE INDEX idx_llm_usage_logs_created ON llm_usage_logs(created_at);
```
