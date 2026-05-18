# 正能量寄语生成方案

> 文档更新时间：2026-05-18
> 状态：待实现

---

## 核心变更说明

**同一生日的用户共享每日寄语，缓存粒度从 `userId + date` 改为 `birthday + date`。**

缓存Key格式：
```
daily_message:{birthday}:{date}
例如: daily_message:1990-01-15:2026-05-18
```

---

## 一、LLM Router 设计

### 1. 多 Provider 支持

通过配置文件切换不同的 LLM 服务商：

```typescript
// config/llm.ts
export type LLMProvider = 'openai' | 'anthropic' | 'zhipu' | 'local';

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
// services/llm/providers/openai.ts
export class OpenAIProvider implements ILLMProvider {
  name() { return 'openai'; }

  async chat(request: LLMRequest): Promise<LLMResponse> {
    // 调用 OpenAI API
  }
}

// services/llm/providers/anthropic.ts
export class AnthropicProvider implements ILLMProvider {
  name() { return 'anthropic'; }

  async chat(request: LLMRequest): Promise<LLMResponse> {
    // 调用 Anthropic Claude API
  }
}

// services/llm/providers/zhipu.ts
export class ZhipuProvider implements ILLMProvider {
  name() { return 'zhipu'; }

  async chat(request: LLMRequest): Promise<LLMResponse> {
    // 调用智谱 GLM API
  }
}
```

### 4. Router 实现

```typescript
// services/llm/router.ts
export class LLMRouter {
  private provider: ILLMProvider;
  private fallbackProvider: ILLMProvider;

  constructor() {
    const config = getLLMConfig();
    this.provider = this.createProvider(config.provider);
    this.fallbackProvider = this.createProvider('local');
  }

  private createProvider(type: LLMProvider): ILLMProvider {
    switch (type) {
      case 'openai': return new OpenAIProvider();
      case 'anthropic': return new AnthropicProvider();
      case 'zhipu': return new ZhipuProvider();
      case 'local': return new LocalProvider();
    }
  }

  async chat(request: LLMRequest): Promise<LLMResponse> {
    try {
      return await this.provider.chat(request);
    } catch (error) {
      console.error(`LLM error, trying fallback:`, error);
      return await this.fallbackProvider.chat(request);
    }
  }

  // 切换 Provider
  switchProvider(provider: LLMProvider) {
    this.provider = this.createProvider(provider);
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
// 例如: daily_message:1990-01-15:2026-05-18
```

### 3. 缓存逻辑流程

```typescript
async function getDailyMessage(birthday: string, date: string): Promise<string[]> {
  // 1. 检查前端本地缓存（最快）
  const localCached = getLocalCache(birthday, date);
  if (localCached && !isExpired(localCached)) {
    return localCached.messages;
  }

  // 2. 检查后端缓存（Redis）
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

### 5. 缓存预热

```typescript
// 每日凌晨生成下一天的缓存（按生日批量预热）
async function prewarmCache(birthday: string): Promise<void> {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const tomorrowStr = tomorrow.toISOString().split('T')[0];

  const cached = await getBackendCache(birthday, tomorrowStr);
  if (!cached) {
    await generateWithLLM(birthday, tomorrowStr);
  }
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
│  1. 主 Provider (OpenAI)                                │
│     ↓ 失败                                             │
│  2. 备用 Provider (智谱 GLM)                             │
│     ↓ 失败                                             │
│  3. 本地模型 (Ollama/Qwen)                              │
│     ↓ 失败                                             │
│  4. 静态寄语库（完全降级）                               │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 2. 降级实现

```typescript
async function generateWithLLM(
  birthday: string,
  date: string,
  options: { tryAllProviders?: boolean } = {}
): Promise<string[]> {
  const { tryAllProviders = true } = options;

  const providers = [
    new OpenAIProvider(),
    new ZhipuProvider(),
    new LocalProvider()
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
  console.warn('All LLM providers failed, using fallback');
  return getFallbackMessage(date, birthday);
}
```

### 3. 超时控制

```typescript
async function chatWithTimeout(
  provider: ILLMProvider,
  request: LLMRequest,
  timeoutMs: number = 5000
): Promise<LLMResponse> {
  return Promise.race([
    provider.chat(request),
    new Promise<LLMResponse>((_, reject) =>
      setTimeout(() => reject(new Error('LLM timeout')), timeoutMs)
    )
  ]);
}
```

### 4. 静态寄语库（最终降级）

```typescript
// 按日期和生日取余，保证同一生日每天固定
const fallbackMessages = [
  "今日宜保持平和心态，稳中求进必有收获",
  "今日贵人运不错，多与人交流会有意外惊喜",
  "今天适合处理积压事务，你的能力会被认可",
  "保持积极心态，好事即将发生在你身上",
  "今日思维清晰，适合做重要决定",
  "你今天的能量很强，敢于尝试新事物吧",
  "今日适合团队协作，发挥你的领导力",
  "保持耐心，你正在为好运积累能量"
];

function getFallbackMessage(date: string, birthday: string): string[] {
  // 用日期 + birthday 取余，保证同一生日每天固定
  const dateHash = hashString(date);
  const birthdayHash = hashString(birthday);
  const baseIndex = (dateHash + birthdayHash) % fallbackMessages.length;

  return [
    fallbackMessages[baseIndex],
    fallbackMessages[(baseIndex + 1) % fallbackMessages.length],
    fallbackMessages[(baseIndex + 2) % fallbackMessages.length]
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

### 5. 降级日志和监控

```typescript
interface FallbackLog {
  birthday: string;
  date: string;
  failedProviders: string[];
  fallbackUsed: boolean;
  responseTime: number;
  timestamp: Date;
}

async function logFallback(log: FallbackLog) {
  // 记录到数据库，用于分析和优化
  await query(
    'INSERT INTO llm_fallback_logs (birthday, date, failed_providers, fallback_used, response_time, created_at) VALUES ($1, $2, $3, $4, $5, NOW())',
    [log.birthday, log.date, log.failedProviders.join(','), log.fallbackUsed, log.responseTime]
  );
}
```

---

## 四、成本优化

### 1. 模型选择策略

```typescript
// 根据场景选择合适的模型
const modelStrategy = {
  // 日常寄语：使用便宜快速的模型
  dailyMessage: {
    provider: 'zhipu',
    model: 'glm-4-flash',  // 智谱轻量版，便宜快速
    maxTokens: 150,
    temperature: 0.7
  },

  // 需要高质量时：使用最强模型
  highQuality: {
    provider: 'openai',
    model: 'gpt-4o-mini',
    maxTokens: 200,
    temperature: 0.8
  },

  // 本地部署：使用本地开源模型
  local: {
    provider: 'local',
    model: 'qwen2.5-7b-instruct',
    maxTokens: 150,
    temperature: 0.7
  }
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

### 3. Token 优化

```typescript
// 精简 Prompt，减少 token 消耗
const optimizedPrompt = {
  // 原始 Prompt
  original: {
    name: '李明',
    rizhu: '戊子',
    // ... 大约 500 tokens
  },

  // 优化后 Prompt
  optimized: {
    n: '李明',
    r: '戊子',
    // ... 大约 200 tokens，语义不变
  }
};
```

### 4. 缓存命中率优化（按生日）

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

### 5. 成本监控

```typescript
interface CostStats {
  date: string;
  provider: string;
  totalRequests: number;
  totalTokens: number;
  estimatedCost: number;  // 估算成本
}

async function getCostStats(date: string): Promise<CostStats[]> {
  const result = await query(`
    SELECT
      provider,
      COUNT(*) as total_requests,
      SUM(prompt_tokens + completion_tokens) as total_tokens
    FROM llm_usage_logs
    WHERE created_at::date = $1
    GROUP BY provider
  `, [date]);

  const pricing = {
    'openai': { per1MTokens: 2.0 },  // GPT-4o-mini
    'anthropic': { per1MTokens: 1.5 }, // Claude-3-haiku
    'zhipu': { per1MTokens: 0.1 }      // 智谱
  };

  return result.rows.map(row => ({
    date,
    provider: row.provider,
    totalRequests: row.total_requests,
    totalTokens: row.total_tokens,
    estimatedCost: (row.total_tokens / 1000000) * pricing[row.provider]?.per1MTokens || 0
  }));
}
```

### 6. 成本控制（按生日限流）

```typescript
// 每日调用上限（按生日维度）
const DAILY_LIMIT = 1000;

// 同一生日每日调用上限（共享缓存）
const BIRTHDAY_DAILY_LIMIT = 5;

async function checkLimit(birthday: string): Promise<boolean> {
  const today = new Date().toISOString().split('T')[0];

  const count = await query(
    'SELECT COUNT(*) FROM daily_messages WHERE birthday = $1 AND generated_at::date = $2',
    [birthday, today]
  );

  return count < BIRTHDAY_DAILY_LIMIT;
}

// 超过上限时强制使用缓存或降级
async function getMessageWithLimit(birthday: string, date: string): Promise<string[]> {
  if (!(await checkLimit(birthday))) {
    console.warn(`Birthday ${birthday} daily limit reached, using fallback`);
    return getFallbackMessage(date, birthday);
  }

  return getDailyMessage(birthday, date);
}
```

---

## 五、Prompt 设计

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

## 六、API 设计

```
POST /api/v1/bazi/daily-message
```

Request:
```json
{
  "birthday": "1990-01-15",
  "date": "2026-05-18"
}
```

Response:
```json
{
  "code": 0,
  "data": {
    "birthday": "1990-01-15",
    "date": "2026-05-18",
    "messages": [
      "今天的你思维清晰，稳重的特质让你处理问题格外得心应手",
      "正官星旺，今日贵人运强，多与人交流会有意想不到的收获",
      "保持平和心态，你的踏实和耐心正在为你积累好运"
    ],
    "source": "ai",
    "model": "glm-4-flash",
    "cached": false
  }
}
```

---

## 七、配置示例

```typescript
// config/llm.ts
export const llmConfig = {
  provider: process.env.LLM_PROVIDER || 'zhipu',

  openai: {
    apiKey: process.env.OPENAI_API_KEY,
    model: 'gpt-4o-mini',
    temperature: 0.7,
    maxTokens: 150
  },

  anthropic: {
    apiKey: process.env.ANTHROPIC_API_KEY,
    model: 'claude-3-haiku-20240307',
    temperature: 0.7,
    maxTokens: 150
  },

  zhipu: {
    apiKey: process.env.ZHIPU_API_KEY,
    model: 'glm-4-flash',
    temperature: 0.7,
    maxTokens: 150
  },

  local: {
    baseUrl: process.env.LOCAL_LLM_URL || 'http://localhost:11434/v1',
    model: 'qwen2.5-7b-instruct',
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

## 八、文件结构

```
server/src/
├── services/
│   ├── llm/
│   │   ├── router.ts           # LLM 路由
│   │   ├── types.ts           # 类型定义
│   │   ├── providers/
│   │   │   ├── index.ts
│   │   │   ├── openai.ts
│   │   │   ├── anthropic.ts
│   │   │   ├── zhipu.ts
│   │   │   └── local.ts
│   │   ├── message.service.ts # 寄语生成服务
│   │   └── cache.service.ts   # 缓存服务
│   └── bazi.service.ts
├── routes/
│   └── bazi.ts
├── controllers/
│   └── bazi.controller.ts
├── config/
│   └── llm.ts
└── models/
    └── daily_message.model.ts # 缓存表
```

---

## 九、数据库变更

### daily_messages 表结构变更

```sql
-- 变更前（按用户）
CREATE TABLE daily_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  date DATE NOT NULL,
  ...
);

-- 变更后（按生日）
CREATE TABLE daily_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  birthday DATE NOT NULL,          -- 生日日期
  date DATE NOT NULL,             -- 当日日期
  messages TEXT[] NOT NULL,
  prompt_tokens INTEGER,
  completion_tokens INTEGER,
  generated_at TIMESTAMP DEFAULT NOW(),
  expires_at TIMESTAMP NOT NULL,
  UNIQUE(birthday, date)           -- 唯一约束变更
);

-- 索引变更
CREATE INDEX idx_daily_messages_birthday_date ON daily_messages(birthday, date);
```

### llm_fallback_logs 表结构变更

```sql
-- 变更前
CREATE TABLE llm_fallback_logs (
  user_id UUID NOT NULL,
  date DATE NOT NULL,
  ...
);

-- 变更后
CREATE TABLE llm_fallback_logs (
  birthday DATE NOT NULL,          -- 生日日期
  date DATE NOT NULL,             -- 当日日期
  failed_providers TEXT,
  fallback_used BOOLEAN DEFAULT FALSE,
  response_time INTEGER,
  created_at TIMESTAMP DEFAULT NOW()
);
```