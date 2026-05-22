# BaziCal 数据库设计

> 文档更新时间：2026-05-15
> 数据库类型：PostgreSQL

---

## 一、现有数据表

### 1. users（用户表）

存储用户基本信息和八字计算结果。

```sql
CREATE TABLE users (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         VARCHAR(64) UNIQUE NOT NULL,
  device_id       VARCHAR(128),
  name            VARCHAR(32) NOT NULL,
  birthday        VARCHAR(10) NOT NULL COMMENT '生日日期 YYYY-MM-DD',
  birthday_type   VARCHAR(10) NOT NULL DEFAULT 'solar' CHECK (birthday_type IN ('solar', 'lunar')) COMMENT '生日类型：solar=阳历，lunar=阴历',
  hour            INTEGER NOT NULL COMMENT '出生小时 0-23',
  minute          INTEGER NOT NULL COMMENT '出生分钟 0-59',
  gender          VARCHAR(2) NOT NULL CHECK (gender IN ('男', '女')),
  bazi            JSONB COMMENT '八字计算结果',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_user_id ON users(user_id);
CREATE INDEX idx_users_device_id ON users(device_id);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| user_id | VARCHAR(64) | 用户唯一标识（由 device_id + name + birthday 生成） |
| device_id | VARCHAR(128) | 设备ID |
| name | VARCHAR(32) | 姓名 |
| birthday | VARCHAR(10) | 生日日期 (YYYY-MM-DD) |
| birthday_type | VARCHAR(10) | 生日类型：solar=阳历（默认），lunar=阴历 |
| hour | INTEGER | 出生小时 (0-23) |
| minute | INTEGER | 出生分钟 (0-59) |
| gender | VARCHAR(2) | 性别 (男/女) |
| bazi | JSONB | 八字计算结果 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

**bazi JSONB 结构：**

```json
{
  "year": { "stem": "庚", "branch": "午" },
  "month": { "stem": "壬", "branch": "午" },
  "day": { "stem": "戊", "branch": "子" },
  "hour": { "stem": "壬", "branch": "午" },
  "wuxing": { "木": 0, "火": 2, "土": 1, "金": 1, "水": 2 },
  "shishen": {
    "year": "正官",
    "month": "偏印",
    "day": "日主",
    "hour": "偏印"
  }
}
```

---

### 2. admins（管理员表）

存储管理员账号信息。

```sql
CREATE TABLE admins (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username        VARCHAR(32) UNIQUE NOT NULL,
  password        VARCHAR(128) NOT NULL COMMENT 'bcrypt加密',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_admins_username ON admins(username);
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| username | VARCHAR(32) | 用户名 |
| password | VARCHAR(128) | 密码（bcrypt加密） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

## 二、新增数据表

### 3. gift_calendars（送礼日历表）

存储生成的送礼日历信息。

```sql
CREATE TABLE gift_calendars (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  share_code      VARCHAR(16) UNIQUE NOT NULL,
  friend_name     VARCHAR(32),
  birthday        VARCHAR(10) NOT NULL COMMENT '朋友生日日期 YYYY-MM-DD',
  birthday_type   VARCHAR(10) NOT NULL DEFAULT 'solar' CHECK (birthday_type IN ('solar', 'lunar')) COMMENT '生日类型：solar=阳历，lunar=阴历',
  hour            INTEGER DEFAULT 0 COMMENT '出生小时',
  minute          INTEGER DEFAULT 0 COMMENT '出生分钟',
  gender          VARCHAR(2) CHECK (gender IN ('男', '女')),
  blessing        VARCHAR(200) COMMENT '祝福语',
  creator_id      VARCHAR(64) COMMENT '创建者user_id',
  bazi            JSONB COMMENT '朋友的八字信息',
  view_count      INTEGER DEFAULT 0 COMMENT '浏览次数',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at      TIMESTAMP COMMENT '过期时间，NULL表示永不过期'
);

CREATE INDEX idx_gift_calendars_share_code ON gift_calendars(share_code);
CREATE INDEX idx_gift_calendars_creator_id ON gift_calendars(creator_id);
CREATE INDEX idx_gift_calendars_created_at ON gift_calendars(created_at DESC);
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| share_code | VARCHAR(16) | 分享码（8位随机字符串） |
| friend_name | VARCHAR(32) | 朋友姓名 |
| birthday | VARCHAR(10) | 朋友生日日期 (YYYY-MM-DD) |
| birthday_type | VARCHAR(10) | 生日类型：solar=阳历（默认），lunar=阴历 |
| hour | INTEGER | 出生小时 (0-23)，默认0 |
| minute | INTEGER | 出生分钟 (0-59)，默认0 |
| gender | VARCHAR(2) | 性别 (男/女) |
| blessing | VARCHAR(200) | 祝福语 |
| creator_id | VARCHAR(64) | 创建者user_id |
| bazi | JSONB | 朋友八字计算结果 |
| view_count | INTEGER | 浏览次数 |
| created_at | TIMESTAMP | 创建时间 |
| expires_at | TIMESTAMP | 过期时间，NULL表示永不过期 |

**share_code 生成规则：**
- 长度：8位
- 字符：字母 + 数字混合
- 示例：`ABC12345`、`XYZ98721`

**bazi JSONB 结构：**

```json
{
  "year": { "stem": "乙", "branch": "亥" },
  "month": { "stem": "甲", "branch": "申" },
  "day": { "stem": "辛", "branch": "未" },
  "hour": { "stem": "癸", "branch": "酉" },
  "wuxing": { "木": 2, "火": 0, "土": 1, "金": 2, "水": 2 },
  "shishen": {
    "year": "正官",
    "month": "偏印",
    "day": "偏印",
    "hour": "正印"
  }
}
```

---

### 4. feedback（反馈表）

存储用户提交的反馈信息。

```sql
CREATE TABLE feedback (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  type            VARCHAR(20) NOT NULL COMMENT '反馈类型：功能建议/问题反馈/体验优化/其他',
  content         TEXT NOT NULL COMMENT '反馈内容，最多500字符',
  contact         VARCHAR(100) COMMENT '联系方式（微信号/邮箱/手机号）',
  user_id         VARCHAR(64) COMMENT '提交者user_id（非必须，用于已登录用户）',
  device_id       VARCHAR(128) COMMENT '设备ID（用于关联用户）',
  status          VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'reviewed', 'replied', 'closed')),
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feedback_user_id ON feedback(user_id);
CREATE INDEX idx_feedback_device_id ON feedback(device_id);
CREATE INDEX idx_feedback_type ON feedback(type);
CREATE INDEX idx_feedback_status ON feedback(status);
CREATE INDEX idx_feedback_created_at ON feedback(created_at DESC);
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| type | VARCHAR(20) | 反馈类型（功能建议/问题反馈/体验优化/其他） |
| content | TEXT | 反馈内容（最多500字符） |
| contact | VARCHAR(100) | 联系方式（微信号/邮箱/手机号） |
| user_id | VARCHAR(64) | 提交者user_id（已登录用户） |
| device_id | VARCHAR(128) | 设备ID（用于关联未登录用户） |
| status | VARCHAR(20) | 处理状态 |
| created_at | TIMESTAMP | 提交时间 |
| updated_at | TIMESTAMP | 更新时间 |

**status 状态说明：**

| 状态 | 说明 |
|------|------|
| pending | 待处理（默认） |
| reviewed | 已查看 |
| replied | 已回复（当有回复时自动更新） |
| closed | 已关闭 |

---

### 4.1 feedback_replies（反馈回复表）

存储反馈的对话记录，支持盖楼功能。

```sql
CREATE TABLE feedback_replies (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  feedback_id     UUID NOT NULL REFERENCES feedback(id) ON DELETE CASCADE,
  content         TEXT NOT NULL COMMENT '回复内容',
  author_type     VARCHAR(10) NOT NULL CHECK (author_type IN ('user', 'admin')) COMMENT '回复者类型：user=用户，admin=管理员',
  author_id       VARCHAR(64) NOT NULL COMMENT '回复者ID（user_id或admin username）',
  author_name     VARCHAR(32) NOT NULL COMMENT '回复者显示名称',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feedback_replies_feedback_id ON feedback_replies(feedback_id);
CREATE INDEX idx_feedback_replies_created_at ON feedback_replies(created_at ASC);
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| feedback_id | UUID | 关联的反馈ID |
| content | TEXT | 回复内容 |
| author_type | VARCHAR(10) | 回复者类型（user/admin） |
| author_id | VARCHAR(64) | 回复者ID |
| author_name | VARCHAR(32) | 回复者显示名称 |
| created_at | TIMESTAMP | 回复时间 |

**说明：** 用户和管理员都可以回复，通过 author_type 区分。查询时按 created_at 升序排列即可形成盖楼效果。

---

### 5. static_messages（静态寄语库）

存储预生成的静态寄语，用于 LLM 不可用时的最终降级。

```sql
CREATE TABLE static_messages (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  content         TEXT NOT NULL,
  category        VARCHAR(50) DEFAULT 'general',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_static_messages_category ON static_messages(category);
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| content | TEXT | 寄语内容 |
| category | VARCHAR(50) | 分类（默认 general） |
| created_at | TIMESTAMP | 创建时间 |

**说明：** 项目启动时检查是否有 100 条记录，无则调用 LLM 生成 100 条入库。

---

### 6. daily_messages（每日寄语缓存）

按生日和日期缓存 LLM 生成的寄语。

```sql
CREATE TABLE daily_messages (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  birthday          DATE NOT NULL,
  date              DATE NOT NULL,
  messages          TEXT[] NOT NULL,
  prompt_tokens     INTEGER,
  completion_tokens INTEGER,
  generated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at        TIMESTAMP NOT NULL,
  UNIQUE(birthday, date)
);

CREATE INDEX idx_daily_messages_birthday_date ON daily_messages(birthday, date);
CREATE INDEX idx_daily_messages_expires ON daily_messages(expires_at);
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| birthday | DATE | 生日日期（共享缓存粒度） |
| date | DATE | 当日日期 |
| messages | TEXT[] | 寄语数组（3条） |
| prompt_tokens | INTEGER | 提示词 token 数 |
| completion_tokens | INTEGER | 回复 token 数 |
| generated_at | TIMESTAMP | 生成时间 |
| expires_at | TIMESTAMP | 过期时间（当天 23:59:59） |

**说明：** 同一生日的用户共享每日寄语，缓存 Key 为 `birthday + date`。

---

### 7. llm_usage_logs（LLM 使用日志）

记录 LLM 调用情况，用于成本分析和问题排查。

```sql
CREATE TABLE llm_usage_logs (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  provider          VARCHAR(50) NOT NULL,
  model             VARCHAR(100) NOT NULL,
  prompt_tokens     INTEGER,
  completion_tokens  INTEGER,
  response_time     INTEGER,
  success           BOOLEAN DEFAULT true,
  error_message     TEXT,
  created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_llm_usage_logs_provider ON llm_usage_logs(provider);
CREATE INDEX idx_llm_usage_logs_created ON llm_usage_logs(created_at);
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| provider | VARCHAR(50) | 提供商（minimax/qwen） |
| model | VARCHAR(100) | 模型名称 |
| prompt_tokens | INTEGER | 提示词 token 数 |
| completion_tokens | INTEGER | 回复 token 数 |
| response_time | INTEGER | 响应时间（毫秒） |
| success | BOOLEAN | 是否成功 |
| error_message | TEXT | 错误信息 |
| created_at | TIMESTAMP | 创建时间 |

---

## 三、ER 图

```
┌─────────────────┐      ┌─────────────────┐
│      users      │      │   admins        │
├─────────────────┤      ├─────────────────┤
│ id (PK)         │      │ id (PK)         │
│ user_id (UK)    │      │ username (UK)   │
│ device_id       │      │ password        │
│ name            │      │ created_at      │
│ birthday        │      └─────────────────┘
│ hour/minute     │
│ gender          │
│ bazi (JSONB)    │
│ created_at       │
└────────┬────────┘
         │
         │ 1:N (creator_id)
         │
         ▼
┌─────────────────┐      ┌─────────────────┐
│ gift_calendars  │      │   feedback      │
├─────────────────┤      ├─────────────────┤
│ id (PK)         │      │ id (PK)         │
│ share_code (UK) │      │ type            │
│ friend_name     │      │ content         │
│ birthday        │      │ contact         │
│ hour/minute     │      │ user_id (FK)    │
│ gender          │      │ status          │
│ blessing        │      │ reply           │
│ creator_id (FK) │      │ replied_at      │
│ bazi (JSONB)    │      │ created_at      │
│ view_count      │      └─────────────────┘
│ created_at      │
└─────────────────┘
```

---

## 四、索引设计

| 表名 | 索引名 | 字段 | 类型 | 说明 |
|------|--------|------|------|------|
| users | idx_users_user_id | user_id | UNIQUE | 用户唯一查询 |
| users | idx_users_device_id | device_id | INDEX | 设备关联 |
| users | idx_users_created_at | created_at | INDEX | 时间排序 |
| admins | idx_admins_username | username | UNIQUE | 登录查询 |
| gift_calendars | idx_gift_calendars_share_code | share_code | UNIQUE | 分享码查询 |
| gift_calendars | idx_gift_calendars_creator_id | creator_id | INDEX | 创建者查询 |
| gift_calendars | idx_gift_calendars_created_at | created_at | INDEX | 时间排序 |
| feedback | idx_feedback_user_id | user_id | INDEX | 用户反馈查询 |
| feedback | idx_feedback_type | type | INDEX | 类型筛选 |
| feedback | idx_feedback_status | status | INDEX | 状态筛选 |
| feedback | idx_feedback_created_at | created_at | INDEX | 时间排序 |

---

## 五、SQL 初始化脚本

```sql
-- 用户表
CREATE TABLE IF NOT EXISTS users (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         VARCHAR(64) UNIQUE NOT NULL,
  device_id       VARCHAR(128),
  name            VARCHAR(32) NOT NULL,
  birthday        VARCHAR(10) NOT NULL,
  birthday_type   VARCHAR(10) NOT NULL DEFAULT 'solar' CHECK (birthday_type IN ('solar', 'lunar')),
  hour            INTEGER NOT NULL,
  minute          INTEGER NOT NULL,
  gender          VARCHAR(2) NOT NULL CHECK (gender IN ('男', '女')),
  bazi            JSONB,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 管理员表
CREATE TABLE IF NOT EXISTS admins (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username        VARCHAR(32) UNIQUE NOT NULL,
  password        VARCHAR(128) NOT NULL,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 送礼日历表
CREATE TABLE IF NOT EXISTS gift_calendars (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  share_code      VARCHAR(16) UNIQUE NOT NULL,
  friend_name     VARCHAR(32),
  birthday        VARCHAR(10) NOT NULL,
  birthday_type   VARCHAR(10) NOT NULL DEFAULT 'solar' CHECK (birthday_type IN ('solar', 'lunar')),
  hour            INTEGER DEFAULT 0,
  minute          INTEGER DEFAULT 0,
  gender          VARCHAR(2) CHECK (gender IN ('男', '女')),
  blessing        VARCHAR(200),
  creator_id      VARCHAR(64),
  bazi            JSONB,
  view_count      INTEGER DEFAULT 0,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at      TIMESTAMP
);

-- 反馈表
CREATE TABLE IF NOT EXISTS feedback (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  type            VARCHAR(20) NOT NULL,
  content         TEXT NOT NULL,
  contact         VARCHAR(100),
  user_id         VARCHAR(64),
  device_id       VARCHAR(128),
  status          VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'reviewed', 'replied', 'closed')),
  reply           TEXT,
  replied_by      VARCHAR(32),
  replied_at      TIMESTAMP,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_user_id ON users(user_id);
CREATE INDEX IF NOT EXISTS idx_users_device_id ON users(device_id);
CREATE INDEX IF NOT EXISTS idx_admins_username ON admins(username);
CREATE INDEX IF NOT EXISTS idx_gift_calendars_share_code ON gift_calendars(share_code);
CREATE INDEX IF NOT EXISTS idx_feedback_user_id ON feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_feedback_device_id ON feedback(device_id);
CREATE INDEX IF NOT EXISTS idx_feedback_status ON feedback(status);

-- 插入默认管理员（密码：admin123）
INSERT INTO admins (username, password)
VALUES ('admin', '$2a$10$XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX')
ON CONFLICT (username) DO NOTHING;
```